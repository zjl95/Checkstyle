package com.puppycrawl.tools.checkstyle.checks.log;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.AbstractFileSetCheck;
import com.puppycrawl.tools.checkstyle.api.FileText;

@StatelessCheck
public  class LogCharacterCheck extends AbstractFileSetCheck {
	/**
     * A key is pointing to the warning message text in "messages.properties"
     * file.
     */
    public static final String MSG_KEY= "nocapitalized";
    
   /** Patterns matching log.info, log.debug...static statements. */
   //static String regex = "\\(.*?)";
    //private static final Pattern Test_PATTERN = Pattern.compile(regex);
   
   //static String regex = "^(log|logger).*\\(*\\)";   ^(log|logger).[a-zA-Z]*\\(.*\\)
    private static final Pattern Test_PATTERN= Pattern.compile("^(log|logger).*");
    
    /** Patterns matching package, import, and import static statements. */
    /**fuzhi zhihou buneng xiugai*/
    private static final Pattern IGNORE_PATTERN = Pattern.compile("^(package|import) .*");
    
    //static String regex1 = "^[A-Z].*";
    //private  Pattern Test_PATTERN1 = Pattern.compile(regex1);
    
    /** Specify pattern for lines to ignore. */
    
    private Pattern ignorePattern = Pattern.compile("^$");
    //private static final Pattern ignorePattern1 = Pattern.compile("[^\\/\\/](.*)");
       
    @Override
    protected void processFiltered(File file, FileText fileText) {
    	for (int i = 0; i < fileText.size(); i++) {
            final String line = fileText.get(i);   
            //if(line.equals("")) {
            //	continue;
            //}else if(!IGNORE_PATTERN.matcher(line).find() && !ignorePattern.matcher(line).find() 
            //		) {
            //	log(i+1,MSG_KEY,MSG_KEY);	
            	
            //}
           char c = getChar(line).charAt(1);
           if(!Test_PATTERN.matcher(line).find() && Character.isLowerCase(c) && !IGNORE_PATTERN.matcher(line).find()
                 && !ignorePattern.matcher(line).find()){
           	log(i+1,MSG_KEY,MSG_KEY);	                   	
            }
            /**
            if(Test_PATTERN.matcher(line).find()) {
            	String subline = line.substring(line.indexOf("(")+1,line.indexOf(")"));
            	
            	// find "(" get "content" after (.
            	// judg first character is or not  upper            	           	           		
        		if(!Test_PATTERN1.matcher(subline).find()) {
        			log(i+1,MSG_KEY_LOG_NO_UPPER,MSG_KEY_LOG_NO_UPPER);	
        		}           	      	           	
            } 
            */ 
    	}    	    	
    }
   
    /**
     * Setter to specify pattern for lines to test.
     *
     * @param pattern a pattern.
     */
    /**
    public final void setTestPattern(Pattern pattern) {
    	Test_PATTERN1 = pattern;
    }
    */
    
    
    
    /**
     * Setter to specify pattern for lines to ignore.
     *
     * @param pattern a pattern.
     */
    public final void setIgnorePattern(Pattern pattern) {
        ignorePattern = pattern;
    }
   
    /**
     * get character after ( 
     */
    public String getChar(String msg) {
    	Pattern p1 =  Pattern.compile("\"(.*?)\"");
    	Matcher m = p1.matcher(msg);
    	ArrayList<String> list =new ArrayList<String>();
    	while(m.find()) {
    		list.add(m.group().trim().replace("\"","")+" ");
    	}
    	return list.toString();
    	
    }
}
