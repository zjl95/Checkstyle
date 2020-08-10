package com.puppycrawl.tools.checkstyle.checks.log.logcharacter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* Contains simple mistakes:
* - Long lines
* - Tabs
*/
public class InputLogCharSimple {
	
// Long line ----------------------------------------------------------------
// Contains a tab ->	<-
// Contains trailing whitespace ->
// Name format tests
//static Logger logger = LoggerFactory.getLogger(InputLogCharSimple.class);

static Logger log = LoggerFactory.getLogger(InputLogCharSimple.class);

/** Invalid format **/
public static final int badConstant = 2;
/** Valid format **/
public static final int MAX_ROWS = 2;

/** Invalid format **/
private static int badStatic = 2;
/** Valid format **/
private static int sNumCreated = 0;

/** Invalid format **/
private int badMember = 2;
/** Valid format **/
private int mNumCreated1 = 0;
/** Valid format **/
protected int mNumCreated2 = 0;

/** commas are wrong **/
private int[] mInts = new int[] {1,2, 3,
4};

/** Invalid format **/
private static void loadConfig(String configFilePath) {
	log.info("Input a file");	
	//logger.error("Faile to execute");
	//logger.debug("finished starting input status management");

	log.debug("it is a bug");
}
}



