package com.cyclone.agent.monitor;

import com.cyclone.agent.bean.InputStatus;
import com.cyclone.agent.bean.property.AgentProperty;
import com.cyclone.agent.bean.property.InputProperty;
import com.cyclone.agent.constant.Constants;
import com.cyclone.agent.exception.AgentRuntimeException;
import com.cyclone.agent.input.BizLogFileInput;
import com.cyclone.agent.input.ExecuteLogFileInput;
import com.cyclone.agent.input.FileInput;
import com.cyclone.agent.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 输入管理器。
 *
 * @author le.xu
 * @version 2020-04-09 11:13:40
 */
public class InputManager implements Runnable {
    private static Logger logger = LoggerFactory.getLogger(InputManager.class);

    // 输入线程池核心线程数
    public static final int INPUT_THREAD_POOL_CORE_POOL_SIZE = 0;

    // 输入线程池的线程最大闲置时间，单位：秒
    public static final long INPUT_THREAD_POOL_KEEP_ALIVE_TIME = 60L;

    // 文件路径分隔符：/ 或 \
    public static final String FILE_PATH_SEPARATOR_1 = "/";
    public static final String FILE_PATH_SEPARATOR_2 = "\\";
    public static final String FILE_PATH_SEPARATOR_REGEX = "\\\\";

    // 输入线程管理（启动或销毁）程序执行时间间隔
    public static final int INPUT_THREADS_MANAGE_INTERVAL_MILLIS = 3000;

    // 加载输入状态的重试次数
    int LOAD_INPUTS_STATUS_RETRY_TIMES = 5;

    // 输入注册文件所在文件夹
    public static final String INPUT_REGISTRY_FILE_DIR = "data\\registry\\input\\";

    // 输入注册文件名
    public static final String INPUT_REGISTRY_FILE_NAME = "status.json";

    // 输入注册文件目录（全路径）
    public static final String ABSOLUTE_INPUT_REGISTRY_FILE_DIR = System.getProperty(Constants.PROJECT_ROOT_PATH_SYSTEM_PROPERTY_KEY)
            + (FILE_PATH_SEPARATOR_2 + INPUT_REGISTRY_FILE_DIR);

    // 输入注册文件全路径
    public static final String INPUT_REGISTRY_FILEPATH = ABSOLUTE_INPUT_REGISTRY_FILE_DIR + INPUT_REGISTRY_FILE_NAME;

    // 输入状态管理器线程池
    private ExecutorService inputStatusManagerThreadPool = Executors.newSingleThreadExecutor();

    // 输入源线程池
    private ExecutorService inputThreadPool;

    // 输入线程结果
    private Map<String, Future<?>> inputFutures = new HashMap<>();

    public InputManager(int maxInputThread) {
        inputThreadPool = new ThreadPoolExecutor(INPUT_THREAD_POOL_CORE_POOL_SIZE, maxInputThread, INPUT_THREAD_POOL_KEEP_ALIVE_TIME,
                TimeUnit.SECONDS, new SynchronousQueue<>());
    }

    public void run() {
        // 启动输入状态管理器
        launchInputStatusManager();

        // 获得配置管理器实例
        ConfigManager configManager = ConfigManager.getInstance();

        // 验证输入源配置
        validateInputProperties();

        // 循环执行输入管理过程
        while (true) {
            try {
                // 根据输入源的"paths"配置，更新匹配的输入源绝对路径（目前只考虑本地日志文件）
                executeupdateAbsoluteInputPaths();

                // 遍历输入文件列表
                for (InputProperty input : configManager.getAgentProperty().getInputs()) {
                    for (String absolutePath : input.getAbsolutePaths()) {
                        // 判断当前输入源的输入线程是否允许启动
                        if (!isInputThreadAllowedStart(absolutePath, input)) {
                            continue;
                        }

                        // 创建文件输入源实例，
                        //根据不同的文件类型创建不同的实例
                        FileInput fileInput = null;
                        switch (input.getType()) {
                            // 业务日志文件
                            case biz_log:
                                fileInput = new BizLogFileInput(input.getType(), absolutePath, input.getInputInterval(), input.getCloseInactive(), input.getMaxReadRows());
                                break;
                            // 机器人执行文件
                            case execute_log:
                                fileInput = new ExecuteLogFileInput(input.getType(), absolutePath, input.getInputInterval(), input.getCloseInactive(), input.getMaxReadRows());
                                break;
                            default:
                                logger.error("Unknown input type '{}'.", input.getType().name());
                                SystemUtils.exit();
                        }

                        try {
                            // 启动输入线程
                            inputFutures.put(absolutePath, inputThreadPool.submit(fileInput));
                            logger.info("Start new input thread, input type : {}, input source : {}", input.getType().name(), absolutePath);
                        } catch (RejectedExecutionException e) {
                            logger.info("Input thread of source '{}' can not be started, reason : {}.", absolutePath, e.toString());
                        }
                    }
                }

                logger.info("Executed input management program.");
            } catch (Throwable e) {
                logger.error("Failed to execute input management progress. error : {}", e.toString());
            }

            // 休眠3秒
            SystemUtils.sleep(INPUT_THREADS_MANAGE_INTERVAL_MILLIS);
        }
    }

    /**
     * 判断当前输入源的输入线程是否允许启动。
     * 判断方法：
     * 1、判断输入源是否有对应存活的输入线程，如果有，则返回，否则进入下一步。
     * 2、判断重新启动输入源的时间间隔是否达到，达到则重新启动输入线程，否则不启动。
     *
     * @param filepath
     * @param inputProperty
     * @return
     */
    private boolean isInputThreadAllowedStart(String filepath, InputProperty inputProperty) {
        // 判断输入源是否有对应存活的输入线程
        if (isInputAlive(filepath)) {
            return false;
        }

        // 获取输入源状态
        InputStatusManager inputStatusManager = InputStatusManager.getInstance();
        InputStatus inputStatus = inputStatusManager.getInputStatus(filepath);

        // 输入源状态对象为空，说明该输入源的状态没有被持久化过（要么没有启动过该输入线程，要么启动过，但是没有来得及持久化状态，线程就被意外关闭）
        if (inputStatus == null) {
            return true;
        }

        // 输入源状态对象非空，说明该输入源对应的输入线程被启动过。但如果该输入线程的最后一次关闭时间为空，说明该输入线程被异常关闭过，则启动当前输入源对应的输入线程
        if (inputStatus.getLastCloseTime() == null) {
            logger.warn("Sign of abnormal shutdown of input thread appears. input file path : {}.", filepath);
            return true;
        }

        // 计算"重新启动输入源的时间间隔"，单位：秒
        long timeInterval = inputProperty.getBackoff() + inputProperty.getBackoffFactor() * inputStatus.getBackoffCount();
        if (timeInterval > inputProperty.getMaxBackoff()) {
            timeInterval = inputProperty.getMaxBackoff();
        }

        // 判断"当前时间与输入源线程最后一次关闭时间的时间间隔"是否大于或等于"重新启动输入源的时间间隔"
        return (System.currentTimeMillis() - inputStatus.getLastCloseTime()) / 1000 > timeInterval;
    }

    /**
     * 输入状态管理器实例。
     *
     * @return
     */
    private void launchInputStatusManager() {
        // 加载各输入源状态，最多重试 LOAD_INPUTS_STATUS_RETRY_TIMES 次
        Map<String, InputStatus> inputStatusMap = null;
        for (int i = 1; i <= LOAD_INPUTS_STATUS_RETRY_TIMES; i++) {
            try {
                inputStatusMap = loadInputsStatus();
            } catch (AgentRuntimeException e) {
                logger.error("Failed to load inputs status. error : {}", e.toString());
                if (i == LOAD_INPUTS_STATUS_RETRY_TIMES) {
                    SystemUtils.exit();
                }
            }
        }

        // 获得输入状态管理器实例
        InputStatusManager inputStatusManager = InputStatusManager.getInstance();
        inputStatusManager.setInputStatusMap(inputStatusMap == null ? new ConcurrentHashMap<>() : inputStatusMap);

        // 启动输入状态管理器实例
        inputStatusManagerThreadPool.execute(inputStatusManager);

        logger.info("Finished starting input status management.");
    }

    /**
     * 加载输入源状态。
     *
     * @return
     */
    private Map<String, InputStatus> loadInputsStatus() throws AgentRuntimeException {
        // 判断输入注册文件目录是否存在，如果不存在则创建
        File dir = new File(ABSOLUTE_INPUT_REGISTRY_FILE_DIR);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                logger.error("Failed to create input registry directory '{}'.", ABSOLUTE_INPUT_REGISTRY_FILE_DIR);
                throw new AgentRuntimeException("Failed to create input registry directory '%s'.", ABSOLUTE_INPUT_REGISTRY_FILE_DIR);
            }
        }

        // 判断输入源状态文件是否存在， 如果不存在则创建
        File file = new File(INPUT_REGISTRY_FILEPATH);
        if (!file.exists()) {
            try {
                // 创建输入源状态文件
                if (file.createNewFile()) {
                    logger.info("Created input registry fle '{}'.", INPUT_REGISTRY_FILEPATH);
                } else {
                    logger.warn("Input registry file '{}' already exists.", INPUT_REGISTRY_FILEPATH);
                }
            } catch (IOException e) {
                logger.error("Failed to create input registry fle '{}'. error : {}", INPUT_REGISTRY_FILEPATH, e.toString());
                throw new AgentRuntimeException("Failed to create input registry fle '%s'. error : %s", INPUT_REGISTRY_FILEPATH, e.toString());
            }
        }

        // 读取输入源状态文件内容
        String statusJsonStr = FileUtils.readFile(INPUT_REGISTRY_FILEPATH, Constants.DEFAULT_FILE_CHARSET);

        // 文件读取失败，抛出异常
        if (statusJsonStr == null) {
            logger.error("Failed to read input status file, file path : {}.", INPUT_REGISTRY_FILEPATH);
            throw new AgentRuntimeException("Failed to read input status file, file path : %s.", INPUT_REGISTRY_FILEPATH);
        }

        // 文件内容为空，说明此前没有更新过输入源状态文件
        if (statusJsonStr.length() == 0) {
            return null;
        }

        // 将输入源状态字符串转换为状态对象
        try {
            return JSONUtils.toObjs(statusJsonStr, InputStatus.class).stream().collect(Collectors.toMap(InputStatus::getSource, status -> status));
        } catch (IOException e) {
            logger.error("Failed to convert status json string to status map. error : {}", e.toString());
            throw new AgentRuntimeException("Failed to convert status json string to status map. error : %s", e.toString());
        }
    }

    /**
     * 验证输入源配置。包括：
     * 1、输入源配置（inputs）是否为空
     * 2、每个输入源的paths配置是否为空，如果不为空，则同时进行规范化
     * 3、输入源类型是否支持
     * 4、输入源路径（模板）是否为绝对路径（不支持相对路径）
     */
    private void validateInputProperties() {
        logger.debug("Validating input properties...");

        AgentProperty agentProperty = ConfigManager.getInstance().getAgentProperty();

        if (agentProperty == null) {
            logger.error("Unexpected null agent property.");
            SystemUtils.exit();
        }

        List<InputProperty> inputProperties = agentProperty.getInputs();
        if (CollectionUtils.isEmpty(inputProperties)) {
            logger.error("Required inputs config is not present.");
            SystemUtils.exit();
        }

        // 遍历输入源配置列表，判断"paths"配置是否缺失，如果"paths"配置存在，则规范化"paths"配置
        Iterator<InputProperty> iterator = inputProperties.iterator();
        while (iterator.hasNext()) {
            InputProperty inputProperty = iterator.next();

            // 判断输入源是否启用，如果没有启用，则忽略该输入源配置
            if (inputProperty.getEnabled() == null || !inputProperty.getEnabled()) {
                iterator.remove();
                continue;
            }

            // 判断输入源类型是否支持
            if (inputProperty.getType() == null) {
                logger.error("Required type config of inputs config is not present.");
                SystemUtils.exit();
            }

            if (!inputProperty.getType().isSupport()) {
                logger.error("Input type '{}' is not supported at present.", inputProperty.getType().name());
                SystemUtils.exit();
            }

            // 判断输入源的"paths"配置是否为空
            String[] paths = inputProperty.getPaths();
            if (ArrayUtils.isEmpty(paths)) {
                logger.error("Required paths config of inputs config is not present.");
                SystemUtils.exit();
            }

            // 规范化"paths"配置
            for (int i = 0, len = paths.length; i < len; i++) {
                String filePathTemplate = paths[i];

                // 规范化文件路径（模板）
                if (filePathTemplate.contains(FILE_PATH_SEPARATOR_1)) {
                    filePathTemplate = filePathTemplate.replace(FILE_PATH_SEPARATOR_2, FILE_PATH_SEPARATOR_1);
                    filePathTemplate = filePathTemplate.replaceAll(FILE_PATH_SEPARATOR_1 + "+", FILE_PATH_SEPARATOR_1);
                } else if (filePathTemplate.contains(FILE_PATH_SEPARATOR_2)) {
                    filePathTemplate = filePathTemplate.replace(FILE_PATH_SEPARATOR_1, FILE_PATH_SEPARATOR_2);
                    filePathTemplate = filePathTemplate.replaceAll(FILE_PATH_SEPARATOR_2 + "+", FILE_PATH_SEPARATOR_2);
                } else {
                    // 不包含/或\，则说明该路径（模板）不是全路径（模板）
                    logger.error("No such directory or file : {}", filePathTemplate);
                    SystemUtils.exit();
                }

                paths[i] = filePathTemplate;
            }
        }
    }

    /**
     * 更新完整的输入源路径。
     *
     * @return
     */
    private void updateAbsoluteInputPaths() {
        // 获取路径配置（模板）（路径配置允许使用通配符"*"），并解析路径配置（模板），获得完整的路径配置
        for (InputProperty input : ConfigManager.getInstance().getAgentProperty().getInputs()) {
            Set<String> absolutePaths = input.getAbsolutePaths();
            if (CollectionUtils.isEmpty(absolutePaths)) {
                absolutePaths = new HashSet<>();
                input.setAbsolutePaths(absolutePaths);
            }

            // 根据paths配置的每一项查找匹配的完整路径
            for (String filePathTemplate : input.getPaths()) {
                Set<String> subFilePaths = getFilePathsByFilePathTemplate(filePathTemplate);
                if (CollectionUtils.isEmpty(subFilePaths)) {
                    if (!filePathTemplate.contains(Constants.FILE_PATH_WILDCARD)) {
                        // 如果指定了输入文件全路径（不含通配符），且找不到相应的文件，则退出进程
                        logger.error("No such directory or file : {}", filePathTemplate);
                        SystemUtils.exit();
                    }
                } else {
                    absolutePaths.addAll(subFilePaths);
                }
            }
        }
    }

    /**
     * 解析路径配置（模板），获得完整的路径配置。
     *
     * @param filePathTemplate 文件路径模板，例如："/Users/ * /log-ingestion/ * / *.clog" （*与前后/之间没有空格）
     * @return
     */
    private Set<String> getFilePathsByFilePathTemplate(String filePathTemplate) {
        logger.debug("Searching file paths by template '{}' ...", filePathTemplate);

        // 判断文件路径（模板）分割符
        String separator = filePathTemplate.contains("/") ? FILE_PATH_SEPARATOR_1 : FILE_PATH_SEPARATOR_REGEX;

        String[] pathSections = filePathTemplate.split(separator);

        File rootDir;
        String rootPath = pathSections[0] + separator;
        logger.debug("root path : {}", rootPath);

        // 判断根路径是否包含通配符
        if (StringUtils.isNotBlank(rootPath)) {
            if (rootPath.contains(Constants.FILE_PATH_WILDCARD)) {
                logger.error("Invalid root path '{}' of input, expect no '*' in root path.", filePathTemplate);
                SystemUtils.exit();
            }

            rootDir = new File(rootPath);
        } else {
            rootDir = new File(separator);
        }

        if (!rootDir.exists()) {
            logger.error("No such directory : {}", filePathTemplate);
            SystemUtils.exit();
        }

        Set<String> result = new HashSet<>();
        travelFiles(rootDir, pathSections, 1, separator, result);
        return result;
    }

    /**
     * 扫描根路径下所有满足条件的文件的全路径。
     *
     * @param dir
     * @param pathSections
     * @param pathSectionsOffset
     * @param separator
     * @param result
     */
    private void travelFiles(File dir, String[] pathSections, final int pathSectionsOffset, final String separator, Set<String> result) {
        if (pathSectionsOffset >= pathSections.length) {
            return;
        }

        // 获得文件夹下的文件（夹）名称列表
        String[] fileNames = dir.list();
        if (fileNames == null || fileNames.length == 0) {
            return;
        }

        // 获取路径片段（文件/文件夹名称）
        String pathSection = pathSections[pathSectionsOffset];

        if (pathSection.equals(Constants.FILE_PATH_WILDCARD)) {
            for (String fileName : fileNames) {
                travelFiles(dir, pathSections, pathSectionsOffset, separator, result, fileName);
            }
        } else if (pathSection.contains(Constants.FILE_PATH_WILDCARD)) {
            // 将路径片段转换为文件（夹）正则表达式
            String fileRegex = pathSection.replace(".", "\\.").replace(Constants.FILE_PATH_WILDCARD, "." + Constants.FILE_PATH_WILDCARD);
            Pattern pattern = Pattern.compile(fileRegex);

            // 遍历当前路径下的所有文件（夹）
            for (String fileName : fileNames) {
                // 判断文件（夹）名称是否与正则表达式匹配
                if (pattern.matcher(fileName).matches()) {
                    travelFiles(dir, pathSections, pathSectionsOffset, separator, result, fileName);
                }
            }
        } else {
            travelFiles(dir, pathSections, pathSectionsOffset, separator, result, pathSection);
        }
    }

    private void travelFiles(File dir, String[] pathSections, int pathSectionsOffset, String separator, Set<String> result, String fileName) {
        logger.debug("Travelling files, dir : {}, separator : {}, fileName : {}", dir.getAbsolutePath(), separator, fileName);

        String path = dir.getAbsolutePath() + separator + fileName;
        File file = new File(path);

        if (file.exists()) {
            if (file.isDirectory()) {
                // 如果是文件夹，则继续遍历
                travelFiles(file, pathSections, pathSectionsOffset + 1, separator, result);
            } else if (pathSectionsOffset == pathSections.length - 1) {
                // 如果是文件，则添加到结果集中
                result.add(file.getAbsolutePath());
            }
        } else {
            logger.error("no such directory or file : {}", path);
            throw new AgentRuntimeException("No such directory or file : %s", path);
        }
    }

    /**
     * 判断输入源是否有对应存活的输入线程。
     *
     * @param inputFilePath
     * @return
     */
    public boolean isInputAlive(String inputFilePath) {
        Future<?> future = inputFutures.get(inputFilePath);
        return future != null && !future.isDone() && !future.isCancelled();
    }
}
