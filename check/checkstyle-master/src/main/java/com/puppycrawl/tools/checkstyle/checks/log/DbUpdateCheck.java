package com.puppycrawl.tools.checkstyle.checks.log;

import java.io.File;
import java.util.regex.Pattern;

import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.AbstractFileSetCheck;
import com.puppycrawl.tools.checkstyle.api.FileText;

@StatelessCheck
public class DbUpdateCheck extends AbstractFileSetCheck {
	/**
     * A key is pointing to the warning message text in "messages.properties"
     * file.
     */
	public static final String MSG_KEY= "no-log-update-Operate";
 
    /** Patterns matching include log.info([DB].....)  static statements. */
    private Pattern TEST_PATTERN= Pattern.compile("^(log|logger).*([DB]).*");	
    
    private Pattern ignorePattern = Pattern.compile("^$");	
    String str = "executeupdate";
   //private Pattern update_PATTERN = Pattern.compile(".*executeUpdate");
    
    //private Pattern update= Pattern.compile("[\s]");
    
    
	
	@Override
    protected void processFiltered(File file, FileText fileText) {
    	for (int i = 0; i < fileText.size()-1; i++) {
            final String line = fileText.get(i);                     
            if(  !ignorePattern.matcher(line).find() && line.contains(str)
            		//&& !update_PATTERN.matcher(line).find() 
            		//&& !TEST_PATTERN.matcher(fileText.get(i+1)).find()
            		){
            	
            	log(i + 1,MSG_KEY);  	
            }
            
    	}    	    	
    }
	
	/**
     * Setter to specify pattern for lines to ignore.
     *
     * @param pattern a pattern.
     */
    public final void setIgnorePattern(Pattern pattern) {
    	ignorePattern = pattern;
    }
}
