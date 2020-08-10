package com.puppycrawl.tools.checkstyle.checks.log.dbupdate;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.puppycrawl.tools.checkstyle.checks.log.logcharacter.InputLogCharSimple;

public class InputDbUpdateSimple {
	// after DB update  yes/no exist log/logger.("[DB]..")
	static Logger logger = LoggerFactory.getLogger(InputLogCharSimple.class);
	
	/** Invalid format 
	 * @throws SQLException **/
	private static void dbUpdate(String configFilePath) throws SQLException {
		String sql ="update users set age = 20 where id =1"; 
		String url = "jadb:mysql://localhost:3306/test";
		String user = "root";
		String pass = "root";	
		Connection conn = DriverManager.getConnection(url,user,pass);
		Statement stmt = conn.createStatement();
		stmt.executeUpdate(sql);
		logger.info("Update a file");	
		
	}
	
	/** valid format 
	 * @throws SQLException **/
	
	private static void dbUpdate1(String configFilePath) throws SQLException {
		String sql ="update users set age = 20 where id =1"; 
		String url = "jadb:mysql://localhost:3306/test";
		String user = "root";
		String pass = "root";
		
		Connection conn = DriverManager.getConnection(url,user,pass);
		Statement stmt = conn.createStatement();
		stmt.executeUpdate(sql);
		logger.info("[DB] Update a file");	
		
	}
	

}
