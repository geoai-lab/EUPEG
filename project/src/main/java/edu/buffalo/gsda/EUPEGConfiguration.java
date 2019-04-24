package edu.buffalo.gsda;

import java.util.HashMap;
import java.util.Map;

public class EUPEGConfiguration 
{
	/*
	 * This file contains the necessary configuration of EUPEG hosted dataset, geoparsers, and result table.
	 * Please change all path information according to how you configure the resources on your machine
	 */
	
	public static boolean if_first_update = false;

    public static String exp_database_Path = "Your Path of SQLite database .db file";
	
	public static String CLAVIN_Path = "Your Path of CLAVIN/IndexDirectory folder";

	public static String Edinburgh_Path = "Your Path of Edinburgh Geoparser /scripts/run folder";
	
	public static String Topo_Path = "Your Path of TopoCluster main folder";
	public static String Topo_Virtual_Env = "Your Path of Python conda environment or virtual environment";
	
	public static String CamCoder_Path = "Your Path of main.py";
	public static String CamCoder_Virtual_Env = "Your Path of Python conda environment or virtual environment";
	
	public static String spacyNER_Path = "Your Path of main.py";
	public static String spacyNER_Virtual_Env = "Your Path of Python conda environment or virtual environment";


	// version information of geoparsers on the date of source codes publishing
	public static Map<String,String> geoparser_version_dic = new HashMap<String,String>();
	static {
		geoparser_version_dic.put("CamCoder", "updated Sept. 2018");
		geoparser_version_dic.put("CLAVIN", "version 2.1.0");
		geoparser_version_dic.put("DBpedia", "version 1.0.0");
		geoparser_version_dic.put("Edinburgh", "version 1.1");
		geoparser_version_dic.put("GeoTxt", "version 1.0");
		geoparser_version_dic.put("StanfordNER", "version 3.9.2");
		geoparser_version_dic.put("SpaCyNER", "version 2.0.18");
		geoparser_version_dic.put("TopoCluster", "updated Nov. 2016");
		geoparser_version_dic.put("Yahoo", "----");
	}
	
	// version information of gazetteers on the date of source codes publishing
	public static Map<String,String> gazetteer_version_dic = new HashMap<String,String>();
	static {
		gazetteer_version_dic.put("CamCoder", "updated July 2018");
		gazetteer_version_dic.put("CLAVIN", "updated Apr. 2019");
		gazetteer_version_dic.put("DBpedia", "2019-04");
		gazetteer_version_dic.put("Edinburgh", "R201904");
		gazetteer_version_dic.put("GeoTxt", "updated July 2017");
		gazetteer_version_dic.put("StanfordNER", "R201904");
		gazetteer_version_dic.put("SpaCyNER", "R201904");
		gazetteer_version_dic.put("TopoCluster", "updated Nov. 2016");
		gazetteer_version_dic.put("Yahoo", "----");
	}
}
