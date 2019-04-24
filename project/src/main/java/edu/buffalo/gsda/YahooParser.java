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

public class YahooParser 
{
	private final String USER_AGENT = "Mozilla/5.0";
	
	// main function of using Yahoo!PlaceSpotter to process the selected corpus
	public void parse(String corpus_name)
	{
		try 
		{
			// A utils class used for getting the current path of a Java class
			Utils utils = new Utils();
			
			// Get the paths of the corpus and parsing output
			String parsedResultPath = utils.getPath()+File.separator+"geoparser_output"+File.separator+corpus_name+"_Yahoo.txt";
			String corpusFolderPath = utils.getPath()+File.separator+"corpora"+File.separator+corpus_name;
			File folder = new File(corpusFolderPath);
			File[] corpusFiles = folder.listFiles();
			int fileCount = corpusFiles.length;
			
			// create the output file
			Out geoparsedResult = new Out(parsedResultPath);
			
			// start message and recording time
			StdOut.println("Starting Yahoo on "+corpus_name+" ...");		
			long startTime = System.currentTimeMillis(); // start time
					
			// parse each file using Yahoo
			for(int fileIndex=0;fileIndex<fileCount;fileIndex++)
			{
				StdOut.println("Processing file "+fileIndex+" ...");
				
				In input = new In(corpusFolderPath+File.separator+fileIndex);
				String text = input.readAll();
				
				Vector<String> toponymVector = new Vector<>();
				String response = getYahooResponse(text);
				Thread.sleep(1500); // For accommodating the request rate limitation
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
			StdOut.println("Yahoo has finished processing "+corpus_name+". It took "+df.format(elapsedTime)+" minutes.");	
			
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
	}
	
	// extract the information of annotated toponyms by PlaceSpotter
	private Vector<String> extractToponymFromResponse(String response, Vector<String> toponymVector)
	{
		JSONObject jsonObject = new JSONObject(response);
		JSONObject resultsObject = jsonObject.getJSONObject("query").getJSONObject("results");
		if(resultsObject.has("matches") && resultsObject.get("matches") != null && (!resultsObject.get("matches").toString().equals("null")))
		{
			JSONObject matchesObject = resultsObject.getJSONObject("matches");
			Object matchObject = matchesObject.get("match");
			JSONArray matchArray = null; 
			if(matchObject instanceof JSONObject)
			{
				matchArray = new JSONArray();
				matchArray.put((JSONObject)matchObject);
			}
			else
				matchArray = matchesObject.getJSONArray("match");
			
			for(int i=0;i<matchArray.length();i++)
			{
				JSONObject thisPlaceObject = matchArray.getJSONObject(i);
				String toponym = thisPlaceObject.getJSONObject("place").getString("name");
				double latitude = Double.parseDouble(thisPlaceObject.getJSONObject("place").getJSONObject("centroid").getString("latitude"));
				double longitude = Double.parseDouble(thisPlaceObject.getJSONObject("place").getJSONObject("centroid").getString("longitude"));
				
				Object referenceObject = thisPlaceObject.get("reference");
				
				if(referenceObject instanceof JSONObject)
				{
					JSONObject referenceJsonObject = (JSONObject)referenceObject;
					
					String surfaceName =  referenceJsonObject.getString("text").replace("\n", "");  // some place names in text contain \n in their middle
					int start = Integer.parseInt(referenceJsonObject.getString("start")); 
					int end = Integer.parseInt(referenceJsonObject.getString("end")); 
					
					toponymVector.add(toponym+",,"+surfaceName+",,"+latitude+",,"+longitude+",,"+ start+",,"+end);
		             
				}
				else if(referenceObject instanceof JSONArray)
				{
					JSONArray referenceJsonArray = (JSONArray)referenceObject;
					for(int j=0;j<referenceJsonArray.length();j++)
					{
						JSONObject referenceJsonObject = referenceJsonArray.getJSONObject(j);
						
						String surfaceName =  referenceJsonObject.getString("text");
						int start = Integer.parseInt(referenceJsonObject.getString("start")); 
						int end = Integer.parseInt(referenceJsonObject.getString("end")); 
						
						toponymVector.add(toponym+",,"+surfaceName+",,"+latitude+",,"+longitude+",,"+ start+",,"+end);
					}
				}   
			}
		}
		
		return toponymVector;
		
	}
	
	// connect with the PlaceSpotter and process the response
	private String getYahooResponse(String text)
	{
		try 
		{
			String url = "https://query.yahooapis.com/v1/public/yql?format=json&q="+URLEncoder.encode("SELECT * FROM geo.placemaker WHERE documentContent=\""+text.replaceAll("\"", "'")+"\" AND documentType=\"text/plain\"", "UTF-8");
			StdOut.println(url);
			
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			
			// optional default is GET
			con.setRequestMethod("GET");
			con.setConnectTimeout(60000); // time out after one minute

			//add request header
			con.setRequestProperty("User-Agent", USER_AGENT);

			int responseCode = con.getResponseCode();
			
			BufferedReader in = new BufferedReader(
			        new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			if(responseCode == 200)
			{
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

}
