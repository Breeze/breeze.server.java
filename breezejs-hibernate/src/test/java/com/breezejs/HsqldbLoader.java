package com.breezejs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class HsqldbLoader {
	
	public static void fillNorthwind() {
		HsqldbLoader.fillDatabase("northwind", "src/test/resources/northwind_insert.sql");
//		HsqldbLoader.fillDatabase("northwind", "src/test/resources/hsqldb-dump-northwindib.sql");
	}
	
	public static void fillDatabase(String namespace, String pathToSqlFile) {
		try {
			Class.forName("org.hsqldb.jdbcDriver");
		} catch (Exception ex) {
			throw new RuntimeException("ERROR: failed to load HSQLDB JDBC driver.", ex);
		}

		Connection conn = null;
		String line = "";
		try {
			conn = DriverManager.getConnection("jdbc:hsqldb:mem:" + namespace + ";shutdown=true", "sa", "");

			Statement statement = conn.createStatement();

			File file = new File(pathToSqlFile);
			InputStream script = new FileInputStream(file.getCanonicalPath()); 
					//HsqldbLoader.class.getResourceAsStream(pathToSqlFile);
			
			BufferedReader br = new BufferedReader(new InputStreamReader(script, "UTF-8"));
//			StringBuffer sb = new StringBuffer(1024);

//			boolean endOfCommand;
			while ((line = br.readLine()) != null) {
//				line = line.trim();
//				endOfCommand = line.endsWith(";");
				line = line.replace("`", "");
				line = line.replace(");", ")");
				line = line.replace("'0x", "'");
				
				if (line.length() > 5) {
					statement.executeUpdate(line);
				}

//				sb.append(' ');
//				sb.append(line);
//
//				if (endOfCommand) {
//					String cmd = sb.toString();
//					System.out.println(cmd);
//					statement.executeUpdate(cmd);
//					System.out.println("---------- executed -------------");
//					endOfCommand = false;
//					sb.setLength(0);
//				}
			}

			br.close();
			statement.close();

		} catch (Exception ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException ex) {
					throw new RuntimeException(ex.getMessage(), ex);
				}
			}
		}
	}

}
