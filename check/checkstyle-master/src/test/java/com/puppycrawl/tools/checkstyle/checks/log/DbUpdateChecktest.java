package com.puppycrawl.tools.checkstyle.checks.log;
import static com.puppycrawl.tools.checkstyle.checks.log.DbUpdateCheck.MSG_KEY ;

import org.junit.jupiter.api.Test;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;

public class DbUpdateChecktest extends AbstractModuleTestSupport{
	@Override
    protected String getPackageLocation() {
        return "com/puppycrawl/tools/checkstyle/checks/log/dbupdate";
    }
	
	@Test
    public void testSimple()
            throws Exception {
        final DefaultConfiguration checkConfig =
            createModuleConfig(DbUpdateCheck.class);
        checkConfig.addAttribute("ignorePattern", "^(package|import) .*");
        final String[] expected = {
            "26: " + getCheckMessage(MSG_KEY),
            //"42: " + getCheckMessage(MSG_KEY),
        };
        verify(checkConfig, getPath("InputDbUpdateSimple.java"), expected);
        
    }


}
