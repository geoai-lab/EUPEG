package edu.buffalo.gsda;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.Out;
import edu.princeton.cs.algs4.StdOut;

public class SpaCyNERParser 
{
	private String py_path = EUPEGConfiguration.spacyNER_Path;
	private String py_venv = EUPEGConfiguration.spacyNER_Virtual_Env;
	
	private final String USER_AGENT = "Mozilla/5.0";
	private String latitude;
	private String longitude;
	private String toponym;

	public boolean setPy_Path(String path) 
	{
		this.py_path = path;
		this.latitude = "";
		this.longitude = "";
		this.toponym = "";
		return true;
	}
	
	// main function of using SpaCyNER and GeoNames Web Service to process selected corpus 
	public void parse(String corpus_name, Boolean if_backup) {
		try {
			// a utils class used for getting the current path of a Java class
			Utils utils = new Utils();

			// get the paths of the corpus and parsing output
			String parsedResultPath = null;
			String corpusFolderPath = utils.getPath()+File.separator+"corpora"+File.separator+corpus_name;			
			// save the output file under backup folder when updating
			if(if_backup==true)
			{
				parsedResultPath = utils.getPath()+File.separator+"output_backup"+File.separator+corpus_name+"_SpaCyNER.txt";
			}
			else 
			{
				parsedResultPath = utils.getPath()+File.separator+"geoparser_output"+File.separator+corpus_name+"_SpaCyNER.txt";
			}
			
			File folder = new File(corpusFolderPath);
			File[] corpusFiles = folder.listFiles();
			int fileCount = corpusFiles.length;

			// create the output file
			Out geoparsedResult = new Out(parsedResultPath);

			// start message and recording time
			StdOut.println("Starting SpaCyNER on " + corpus_name + " ...");
			long startTime = System.currentTimeMillis(); // start time

			// parse each file using SpacyNER
			for (int fileIndex = 0; fileIndex < fileCount; fileIndex++) 
			{
				StdOut.println("Processing file "+fileIndex+" ...");
				String resultLine = "";
				
				In input = new In(corpusFolderPath + File.separator + fileIndex);
				String text = input.readAll();

				Vector<String> toponymVector = new Vector<>();
				toponymVector = runScriptProcess(text);

				// write the output into the parsing result file
				for (String toponymRecord : toponymVector)
					resultLine += toponymRecord + "||";

				geoparsedResult.println(resultLine);

			}
			geoparsedResult.close();
			
			
			// end message and recording time
			long stopTime = System.currentTimeMillis(); //stop time
		    double elapsedTime = (stopTime - startTime)*1.0 / 60000.0; // elapsed time
		    DecimalFormat df = new DecimalFormat();
		    df.setMaximumFractionDigits(2);
			StdOut.println("SpaCyNER has finished processing "+corpus_name+". It took "+df.format(elapsedTime)+" minutes.");	

		} 
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	// run the local SpaCyNER tool using command builder
	public Vector<String> runScriptProcess(String article) 
	{
		Vector<String> toponymVector = new Vector<>();
		String result = "[]";
		try {
			
			String command = this.py_venv+" "+ this.py_path+" \""+article+"\"";
			
			ProcessBuilder pb = new ProcessBuilder("sh","-c", command);
			pb.redirectError();
			String temp = null;	
			
			Process p = pb.start();
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

	        // read the output from the command
	        while ((temp = stdInput.readLine()) != null)
	        {
	        	result = temp.trim();
	        }
	        
	        // in case the output is empty, make the output []
	        if(!result.startsWith("[") || !result.endsWith("]"))
	        	result = "[]";
		} 
		catch (IOException e) 
		{
			System.out.println("exception happened - here's what I know: ");
	        e.printStackTrace();
	        result = "[]";
		}
		
		toponymVector = parseResult(result,toponymVector);
		return toponymVector;
	}
	
	// extract the tagged location by SpaCyNER and link with actual spatial entity (geocoding)
	public Vector<String> parseResult(String jsonResults,Vector<String> toponymVector) 
	{
		JSONArray results_array = new JSONArray(jsonResults);
		JSONArray record_array = new JSONArray();
		
		Hashtable<String, String> existingCoordTable = new Hashtable<>();  // a hashtable used for storing the location of place names in order to save GeoNames queries
		Hashtable<String, String> existingTrueNameTable = new Hashtable<>();  // a hashtable used for storing the location of place names in order to save GeoNames queries

		for(int i=0;i<results_array.length();i++)
		{
			record_array = results_array.getJSONArray(i);
			String word = record_array.getString(0);
			String startPos = String.valueOf(record_array.getInt(1));
			String endPos = String.valueOf(record_array.getInt(2));
			
			String existingInfo = existingCoordTable.get(word);
			if(existingInfo != null)
			{
				String trueName = existingTrueNameTable.get(word);
				String record = null;
				if(trueName != null)
				   record = trueName+",,"+word +",,"+existingInfo+",,"+startPos+",,"+endPos;
				else
				   record = word+",,"+word +",,"+existingInfo+",,"+startPos+",,"+endPos;
				toponymVector.add(record);
			}
			else
			{
				boolean c = getGazeTopoByPops(word);
				if(c == true)
				{
					String record = this.toponym+",,"+word +",,"+this.latitude+",,"+this.longitude+",,"+startPos+",,"+endPos;
					toponymVector.add(record);
					
					existingCoordTable.put(word, this.latitude+",,"+this.longitude);
					existingTrueNameTable.put(word, this.toponym);
				}
			}
		}
		return toponymVector;
	}
	
	// search for the name of annotated location in the GeoNames DB and choose the location with most population 
	public boolean getGazeTopoByPops(String location) 
	{
		try 
		{
			Thread.sleep(3000);
			String[] geoNamesUser = {"add your GeoNames Account here (More than three to avoid errors"};
			String currentUser = null;
			Random rand = new Random();
			int max = (geoNamesUser.length-1), min = 0;
			int randomIndex = rand.nextInt((max - min) + 1) + min;
			currentUser = geoNamesUser[randomIndex];
			
			String url = "http://api.geonames.org/searchJSON?q="+URLEncoder.encode(location,"UTF-8")+"&orderby=population&maxRows=1&username="+currentUser;
			
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			// optional default is GET
			con.setRequestMethod("GET");
			con.setConnectTimeout(60000); // time out after one minute
			// add request header
			con.setRequestProperty("User-Agent", USER_AGENT);

			int responseCode = con.getResponseCode();
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			if(responseCode == 200)
			{
				return extractToponym(location, response.toString());
				
			}
			else 
			{
				StdOut.println("Response Code : " + responseCode);
				StdOut.println(response.toString());
				return false;
			}
			
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		return false;
		
		
	}
	
	// parse the json response of GeoNames Web Service 
	public boolean extractToponym(String toponym,String response) 
	{
		try
		{
			JSONObject jsonObject = new JSONObject(response);
			JSONArray toponymArray = jsonObject.getJSONArray("geonames");
			if(toponymArray.length()>=1) 
			{
				JSONObject thisToponymObject = toponymArray.getJSONObject(0);
				this.latitude = thisToponymObject.getString("lat");
				this.longitude = thisToponymObject.getString("lng");
				this.toponym = thisToponymObject.getString("toponymName");		
				return true;
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return false;
	}
}
