package com.puppycrawl.tools.checkstyle.checks.log.logcharacter;

public class InputLogCharImportStatement {
	@Override
    public String toString() {
        return "This is very long line that should be logged because it is not import";
    }

}
