package edu.buffalo.gsda;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.Out;
import edu.princeton.cs.algs4.StdOut;

public final class DBpediaParser 
{
	private final String USER_AGENT = "Mozilla/5.0";
	private final String ACCEPT_VALUE = "application/json";
	

	// main function of using DBpedia Spotlight to process selected corpus
	public void parse(String corpus_name,boolean if_backup) 
	{
		try 
		{
			// a utils class used for getting the current path of a Java class
			Utils utils = new Utils();
			
			// get the paths of the corpus and geoparsing output
			String parsedResultPath = null;
			String corpusFolderPath = utils.getPath()+File.separator+"corpora"+File.separator+corpus_name;			
			// save the output file under backup folder when updating
			if(if_backup==true)
			{
				parsedResultPath = utils.getPath()+File.separator+"output_backup"+File.separator+corpus_name+"_DBpedia.txt";
			}
			else 
			{
				parsedResultPath = utils.getPath()+File.separator+"geoparser_output"+File.separator+corpus_name+"_DBpedia.txt";
			}
			File folder = new File(corpusFolderPath);
			File[] corpusFiles = folder.listFiles();
			int fileCount = corpusFiles.length;
			
			// create the output file
			Out geoparsedResult = new Out(parsedResultPath);
			
			// start message and recording time
			StdOut.println("Starting DBpedia Spotlight on "+corpus_name+" ...");		
			long startTime = System.currentTimeMillis(); // start time
		
					
			// parse each file using DBpedia Spotlight
			for(int fileIndex=0;fileIndex<fileCount;fileIndex++)
			{
				StdOut.println("Processing file "+fileIndex+" ...");
				
				In input = new In(corpusFolderPath+File.separator+fileIndex);
				String text = input.readAll();
				
				Vector<String> toponymVector = new Vector<>();
				String response = getDBpediaResponse(text);
				
				// if no response comes from the server
				if(response == null || response.length()==0) 
				{
					geoparsedResult.println("");
					continue;
				}
				
				//StdOut.println(response);
				Thread.sleep(1000);
				toponymVector = extractToponymFromResponse(response, toponymVector);
				
				// write the output into the parsing result file
				String resultLine = "";
				for(String toponymRecord : toponymVector)
					resultLine += toponymRecord+"||";
				
				geoparsedResult.println(resultLine);
		
			}
			geoparsedResult.close();
			
			// end message and recording time
			long stopTime = System.currentTimeMillis(); //stop time
		    double elapsedTime = (stopTime - startTime)*1.0 / 60000.0; // elapsed time
		    DecimalFormat df = new DecimalFormat();
		    df.setMaximumFractionDigits(2);
			StdOut.println("DBpedia Spotlight has finished processing "+corpus_name+". It took "+df.format(elapsedTime)+" minutes.");	
				
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	// use DBpedia Spotlight extract all toponyms from the text  
	public String getDBpediaResponse(String text) 
	{
		try 
		{
			String url = "https://gsda.geography.buffalo.edu/DBpediaSpotlight/annotate?text=";
			String url_text = URLEncoder.encode(text, "UTF-8");
			url = url + url_text + "&confidence=0.2&support=20&spotter=Default&disambiguator=Default&policy=whitelist&types=Place";
			
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			
			// optional default is GET
			con.setRequestMethod("GET");

			// add request header
			con.setRequestProperty("User-Agent", USER_AGENT);
			con.setRequestProperty("Accept", ACCEPT_VALUE);

			int responseCode = con.getResponseCode();
			StringBuffer response = new StringBuffer();
			
			if(responseCode == 200)
			{
			
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();

				return response.toString();
			}
			else 
			{
				StdOut.println("Response Code : " + responseCode);
				StdOut.println(response.toString());
				return null;
			}
			
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	// access the DBpedia page of extracted toponyms
	private Vector<String> extractToponymFromResponse(String response, Vector<String> toponymVector)
	{
		JSONObject jsonObject = new JSONObject(response);
		if(!jsonObject.has("Resources"))
			return toponymVector;
		
		
		JSONArray resultsArrays = jsonObject.getJSONArray("Resources");
		
		for(int i=0;i<resultsArrays.length();i++)
		{
			JSONObject thisToponymObject = resultsArrays.getJSONObject(i);
			if(thisToponymObject.has("@surfaceForm") && (!thisToponymObject.get("@surfaceForm").toString().equals("null")))
			{
				String locationWord = thisToponymObject.get("@surfaceForm").toString();
				String topoURL = thisToponymObject.get("@URI").toString();
				Integer startPosition = Integer.valueOf(thisToponymObject.get("@offset").toString());
				Integer endPosition  = startPosition + locationWord.length();
				String topoURL2 = topoURL.replaceFirst("resource", "data");
				topoURL2 = topoURL2+".json";
				Vector<String> coordinates = new Vector<>();
				coordinates = getCoordinate(topoURL,topoURL2,coordinates);
				if(coordinates!= null && coordinates.size()>0)
				{
					String record = locationWord + ",,"+locationWord+",," + coordinates.get(0) + ",," + coordinates.get(1)+ ",,"+startPosition.toString()+",,"+ endPosition.toString();
					toponymVector.add(record);
				}
			}
		}
		
		return toponymVector;
		
	}
	
	// extract spatial information of annotated toponyms from the DBpedia page
	private Vector<String> getCoordinate(String key,String url, Vector<String> coordinates) 
	{
		try 
		{
			Thread.sleep(1000);
			//StdOut.println(url);
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			
			// optional default is GET
			con.setRequestMethod("GET");
			con.setConnectTimeout(60000); // time out after one minute
	
			//add request header
			con.setRequestProperty("User-Agent", USER_AGENT);
			con.setRequestProperty("Accept", ACCEPT_VALUE);
	
			int responseCode = con.getResponseCode();
			
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
	
			while ((inputLine = in.readLine()) != null) 
			{
				response.append(inputLine);
			}
			in.close();
	
			if(responseCode == 200)
			{
				JSONObject jsonObject = new JSONObject(response.toString());
				jsonObject = jsonObject.getJSONObject(key);
				if(jsonObject.has("http://www.w3.org/2003/01/geo/wgs84_pos#lat"))
				{
					JSONArray resultsLatArrays = jsonObject.getJSONArray("http://www.w3.org/2003/01/geo/wgs84_pos#lat");
					JSONArray resultsLongArrays = jsonObject.getJSONArray("http://www.w3.org/2003/01/geo/wgs84_pos#long");
					String lat = String.valueOf(resultsLatArrays.getJSONObject(0).getFloat("value"));
					String lon = String.valueOf(resultsLongArrays.getJSONObject(0).getFloat("value"));
					coordinates.add(lat);
					coordinates.add(lon);
					return coordinates;
				}
				else if(jsonObject.has("http://dbpedia.org/property/latitude"))
				{
					
					JSONArray resultsLatArrays = jsonObject.getJSONArray("http://dbpedia.org/property/latitude");
					JSONArray resultsLongArrays = jsonObject.getJSONArray("http://dbpedia.org/property/longitude");
					
					String lat = String.valueOf(resultsLatArrays.getJSONObject(0).getFloat("value"));
					String lon = String.valueOf(resultsLongArrays.getJSONObject(0).getFloat("value"));
					coordinates.add(lat);
					coordinates.add(lon);
					return coordinates;
				}
			
			}
			else 
			{
				StdOut.println("Response Code : " + responseCode);
				StdOut.println(response.toString());
			}
			
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	
		return null;
	}
	

}
