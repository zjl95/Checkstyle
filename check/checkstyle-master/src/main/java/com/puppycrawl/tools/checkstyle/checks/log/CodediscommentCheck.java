package com.puppycrawl.tools.checkstyle.checks.log;

import java.io.File;
import java.util.regex.Pattern;
//
import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.AbstractFileSetCheck;
import com.puppycrawl.tools.checkstyle.api.FileText;
//
@StatelessCheck
public class CodediscommentCheck extends AbstractFileSetCheck {
	
	/**
     * A key is pointing to the warning message text in "messages.properties"
     * file.
     * 
     * zhushi qianyihang bunneg wei daima  ,houyihang keyi 
     */
    public static final String MSG_KEY = "Not.blank.lines";
    //public static final String MSG = "Attention";
    
    /** Specify pattern for lines to ignore. */
    private Pattern ignorePattern = Pattern.compile("^(package|import) .*");
    
    private Pattern targetPattern = Pattern.compile("\\/\\/(.*)");
    
   // private Pattern codePattern = Pattern.compile("^([a-z]|[A-Z]).*");      
    //private Pattern nullPattern = Pattern.compile("^(\\s*)\\n");   //blank line's regex
    
    private Pattern stringPattern = Pattern.compile("[a-zA-Z].*");
      
    @Override
    protected void processFiltered(File file, FileText fileText) {
        for (int i = 0; i < fileText.size(); i++) {
            final String line = fileText.get(i);   
           
            if(i > 0 && !ignorePattern.matcher(line).find() && stringPattern.matcher(fileText.get(i-1)).find()    
            		&&  targetPattern.matcher(line ).find()){
            	//if(!stringPattern.matcher(fileText.get(i-1)).find()){
            		log(i + 1, MSG_KEY);
            	//}
            	
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
