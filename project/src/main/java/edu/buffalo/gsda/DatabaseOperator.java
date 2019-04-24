package edu.buffalo.gsda;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.json.JSONObject;

public class DatabaseOperator 
{
	private String databasePath = EUPEGConfiguration.exp_database_Path; 

	// search for the experiment records by experiment ID 
	public JSONObject searchRecord(String index) 
	{
		Connection c = null;
		Statement stmt = null;
		JSONObject resultListObejct = new JSONObject();
		try 
		{
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:" + this.databasePath);
			c.setAutoCommit(false);

			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM experimentRecords Where experimentID = '" + index + "';");

			int nRow = 1;
			ArrayList<String> metricsList = new ArrayList<String>();
			while (rs.next()) 
			{
				JSONObject metricsObject = new JSONObject();

				String parserName = rs.getString("parserName");
				String corpusName = rs.getString("corpusName");
				

				metricsObject.put("precision", rs.getFloat("precision"));
				metricsObject.put("recall", rs.getFloat("recall"));
				metricsObject.put("f_score", rs.getFloat("f_score"));
				metricsObject.put("accuracy", rs.getFloat("accuracy"));
				metricsObject.put("mean", rs.getFloat("mean"));
				metricsObject.put("median", rs.getFloat("median"));
				metricsObject.put("AUC", rs.getFloat("AUC"));
				metricsObject.put("accuracy_161", rs.getFloat("accuracy_161"));
				metricsObject.put("parser_version", rs.getString("parser_version"));
				metricsObject.put("gaze_version", rs.getString("gaze_version"));
				
				// extract the name of all searched columns  
				if (nRow == 1) 
				{
					for (Object key : metricsObject.keySet())
					{
						// based on you key types
						String keyStr = (String) key;
						if(!keyStr.equals("parser_version")&&!keyStr.equals("gaze_version")) 
						{
							metricsList.add(keyStr);
						
					}
				}
					resultListObejct.put("getMetricsList", metricsList);
					resultListObejct.put("getExpTime", rs.getString("timestamp"));
				}
				resultListObejct.put(corpusName + "|" + parserName, metricsObject);
				nRow = nRow + 1;
			}

			rs.close();
			stmt.close();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}

		return resultListObejct;

	}
	
	// insert an experiment record into the database
	public boolean insertRecord(String experimentID, String corpusName, String parserName,String expTime,
			String version1,String version2, HashMap<String, Double> resultMap) {

		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:" + this.databasePath);
			c.setAutoCommit(false);

			stmt = c.createStatement();
			String sql1 = "INSERT INTO experimentRecords(experimentID,parserName,corpusName,timestamp,parser_version,gaze_version,";
			String sql2 = "VALUES('" + experimentID + "','" + parserName + "','" + corpusName + "','" + expTime + "','" + version1+ "','"+version2+"',";
			Collection<String> keys = resultMap.keySet();
			for (Object key : keys) {
				sql1 = sql1 + key + ",";
				sql2 = sql2 + (resultMap.get(key)).toString() + ",";
			}
			sql1 = sql1.substring(0, sql1.length() - 1) + ")";
			sql2 = sql2.substring(0, sql2.length() - 1) + ");";

			String sql = sql1 + sql2;
			stmt.executeUpdate(sql);

			stmt.close();
			c.commit();
			c.close();

		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}

		return true;
	}

	// insert a record of user uploaded geoparser into database
	public boolean addParserMetadata(String experimentID, String url, String parserName, String parserNameOff) {

		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
			c.setAutoCommit(false);

			stmt = c.createStatement();

			String sql1 = "INSERT INTO parserList(experimentID,parserName,url,officialName)";
			String sql2 = "VALUES('" + experimentID + "','" + parserName + "','" + url + "','" + parserNameOff + "');";

			stmt.executeUpdate(sql1 + sql2);

			stmt.close();
			c.commit();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		
		return true;
	}

	// insert a record of user uploaded corpus into database
	public boolean addDataSetMetadata(String orgName, String finalName, String experimentID) {

		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
			c.setAutoCommit(false);
		
			stmt = c.createStatement();

			String sql1 = "INSERT INTO corpusList(experimentID,userName,serverName)";
			String sql2 = "VALUES('" + experimentID + "','" + orgName + "','" + finalName + "');";

			stmt.executeUpdate(sql1 + sql2);

			stmt.close();
			c.commit();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		
		return true;
	}

	// search for the information of user uploaded corpus by corpus name and experiment ID
	public String searchDataSetName(String experimentID, String name) {

		Connection c = null;
		Statement stmt = null;
		String userName = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:" + this.databasePath);
			c.setAutoCommit(false);
			

			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM corpusList Where serverName ='" + name + "';");

			while (rs.next()) {

				userName = rs.getString("userName");

			}
			rs.close();
			stmt.close();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}

		return userName;

	}
	
	// search for the information of user uploaded geoparser by geoparser name and experiment ID
	public String searchParserName(String experimentID, String name) {

		Connection c = null;
		Statement stmt = null;
		String userName = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:" + this.databasePath);
			c.setAutoCommit(false);

			stmt = c.createStatement();
		
			ResultSet rs = stmt.executeQuery("SELECT * FROM parserList Where officialName ='" + name + "';");

			while (rs.next()) {

				userName = rs.getString("parserName");

			}
			rs.close();
			stmt.close();
			c.close();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}

		return userName;

	}

}
