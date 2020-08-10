package com.puppycrawl.tools.checkstyle.checks.log;
import static com.puppycrawl.tools.checkstyle.checks.log.CodediscommentCheck.MSG_KEY ;
//import static com.puppycrawl.tools.checkstyle.checks.log.CodediscommentCheck.MSG_KEY;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
//import com.puppycrawl.tools.checkstyle.checks.sizes.LineLengthCheck;

public class CodediscommentChecktest extends AbstractModuleTestSupport {
	    @Override
	    protected String getPackageLocation() {
	        return "com/puppycrawl/tools/checkstyle/checks/log/codediscomment";
	    }
	    
	    @Test
	    public void testSimple() throws Exception {
	        final DefaultConfiguration checkConfig =
	            createModuleConfig(CodediscommentCheck.class);
	        checkConfig.addAttribute("ignorePattern", "^(package|import) .*");
	        final String[] expected = {
	        	"3: " + getCheckMessage(MSG_KEY),
	            "19: " + getCheckMessage(MSG_KEY),	
	            "10: " + getCheckMessage(MSG_KEY),
	        };
	        verify(checkConfig, getPath("Inputcdcsimple.java"), expected);
	    }
	
	

}
