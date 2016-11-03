package com.parking.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {
	public void closeConn(Connection conn){
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
public Connection openConnection() {
		
		String driver = null;
		String url = null;
		String username = null;
		String password = null;

		try {
			
			driver = "com.mysql.jdbc.Driver";
			url = "jdbc:mysql://localhost:3306/park";
			username = "root";
			password = "ywb";
			
			Class.forName(driver);
			return DriverManager.getConnection(url, username, password);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
