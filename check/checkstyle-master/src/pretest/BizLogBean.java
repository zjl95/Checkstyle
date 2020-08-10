package com.cyclone.agent.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.List;

/**
 * 业务日志类。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BizLogBean {
    // 流程id
    @JsonProperty("flow_id")
    private String flowId;

    // 流程的某次执行的唯一编号
    @JsonProperty("process_id")
    private String processId;

    // 日志打印时间戳
    private Long timestamp;

    // 业务实体，最终对应表名
    private String entity;

    // 业务个体（对应表的某一行）唯一编码，用于区分业务变量属于哪个个体
    private String instanceId;

    // 自定义业务变量
    private List<CycloneFlowVariable> variables;

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public List<CycloneFlowVariable> getVariables() {
        return variables;
    }

    public void setVariables(List<CycloneFlowVariable> variables) {
        this.variables = variables;
    }

    @JsonGetter("entity")
    public String getEntity() {
        return entity;
    }

    @JsonSetter("entity name")
    public void setEntity(String entity) {
        this.entity = entity;
    }

    @JsonGetter("instance_id")
    public String getInstanceId() {
        return instanceId;
    }

    @JsonSetter("instanceid")
    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BizLogBean{");
        sb.append("flowId='").append(flowId).append('\'');
        sb.append(", processId='").append(processId).append('\'');
        sb.append(", timestamp=").append(timestamp);
        sb.append(", entity='").append(entity).append('\'');
        sb.append(", instanceId='").append(instanceId).append('\'');
        sb.append(", variables=").append(variables);
        sb.append('}');
        return sb.toString();
    }
}
