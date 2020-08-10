package com.puppycrawl.tools.checkstyle.checks.log;

import static com.puppycrawl.tools.checkstyle.checks.log.LogCharacterCheck.MSG_KEY;
//import static com.puppycrawl.tools.checkstyle.checks.sizes.LineLengthCheck.MSG_KEY;

import org.junit.jupiter.api.Test;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;

public class LogCharacterChecktest extends AbstractModuleTestSupport{
	
	@Override
    protected String getPackageLocation() {
        return "com/puppycrawl/tools/checkstyle/checks/log/logcharacter";
    }
// test for ensure source code right
	@Test
    public void testSimple()
            throws Exception {
        final DefaultConfiguration checkConfig =
            createModuleConfig(LogCharacterCheck.class);       
        //checkConfig.addAttribute("Test_PATTERN", "^(log|logger).*\\(*\\)");  //Test_PATTERN1 not exist
       // checkConfig.addAttribute("Test_PATTERN1", "^(log|logger).*\\\\(*\\\\)");
        checkConfig.addAttribute("ignorePattern", "^package.*|^import.*|^\\/\\/(.*)");    
        //System.out.println("checkConfig: "+checkConfig);
        final String[] expected = {
        	
        	"48: " + getCheckMessage(MSG_KEY),  
        };
        // 使用给定的文件名对文件执行验证。
        verify(checkConfig, getPath("InputLogCharSimple.java"), expected);
    }
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
