package com.puppycrawl.tools.checkstyle.checks.log.codediscomment;
import java.util.ArrayList;
//import request
public class Inputcdcsimple {
	
	/**
	 *  wherever there is a blank line between code and comment 
	 */	
	int num = 100;
	// there is a flag
	boolean flag = true;
	
	/*
	 * vaild example
	 */
	public void CCD() {			
		try {
			Class.forName("com.mysql.jdbc.Driver");	
			// class load
		}catch(Exception e){ 
			e.printStackTrace();			
		}
	}
}
