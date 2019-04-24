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


public class GeoTxtParser 
{
	private final String USER_AGENT = "Mozilla/5.0";
	
	// test the connection of version 2.0 of GeoTxt
	public boolean test_url() 
	{
		String url_v2 = "http://geotxt.org/v2/api/geotxt.json?m=stanfords&q=I+live+in+London";
		
		int temp_code = -1;
		try 
		{
			// try GeoTxt V2 as the default version
			URL obj = new URL(url_v2);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		
			// optional default is GET
			con.setRequestMethod("GET");
			con.setConnectTimeout(60000); // time out after one minute
		

			//add request header
			con.setRequestProperty("User-Agent", USER_AGENT);
			
			temp_code = con.getResponseCode();
		} 
		catch (Exception e) 
		{
			// TODO: handle exception
			return false;
		}
		
		if(temp_code==200)
		{
			return true;
		}
		else 
		{
			return false;
		}
		
	}
	

	// main function of using GeoTxt to process selected corpus
	public void parse(String corpus_name, Boolean if_backup)
	{
		try 
		{
			// A utils class used for getting the current path of a Java class
			Utils utils = new Utils();
			
			// Get the paths of the corpus and parsing output
			String parsedResultPath = null;
			String corpusFolderPath = utils.getPath()+File.separator+"corpora"+File.separator+corpus_name;	
			
			//save the output file under backup folder when updating
			if(if_backup==true)
			{
				parsedResultPath = utils.getPath()+File.separator+"output_backup"+File.separator+corpus_name+"_GeoTxt.txt";
			}
			else 
			{
				parsedResultPath = utils.getPath()+File.separator+"geoparser_output"+File.separator+corpus_name+"_GeoTxt.txt";
			}			

			File folder = new File(corpusFolderPath);
			File[] corpusFiles = folder.listFiles();
			int fileCount = corpusFiles.length;
			
			// create the output file
			Out geoparsedResult = new Out(parsedResultPath);
			
			// decide which version to use
			StdOut.println("Testing the default GeoTxt version 2 service ...");
			String geotxt_url = "http://geotxt.org/v2/api/geotxt.json?m=stanfords&q=";
			if(test_url()==false)
			{
				StdOut.println("Version 2 is not working, Change to GeoTxt version 1.0 service ...");
				geotxt_url = "http://geotxt.org/api/geotxt.json?m=stanfords&q=";	
			}
			else 
			{
				StdOut.println("Version 2 is working, use GeoTxt version 2.0 service ...");
				EUPEGConfiguration.geoparser_version_dic.replace("GeoTxt", "version 2.0" );
			}
			Thread.sleep(1000);
			
			// start message and recording time
			StdOut.println("Starting GeoTxt on "+corpus_name+" ...");		
			long startTime = System.currentTimeMillis(); // start time

			// parse each file using GeoTxt
			for(int fileIndex=0;fileIndex<fileCount;fileIndex++)
			{
				StdOut.println("Processing file "+fileIndex+" ...");
				
				// get input geo-corpora file contents 
				In input = new In(corpusFolderPath+File.separator+fileIndex);
				String text = input.readAll();

				Vector<String> toponymVector = new Vector<>();
				
				text = URLEncoder.encode(text, "UTF-8");
				int startIndex = 0;
				do
				{
					int lastIndex = -1;				
					if(text.length()>3000)
					{
						lastIndex = text.lastIndexOf("+", 3000);
						
						if(lastIndex < 2000) // if a space cannot be found; applies to non-English words
							lastIndex = 3000;
					}
					else {
						lastIndex = text.length();
					}
					 
					String response = getGeoTxtResponse(geotxt_url, text.substring(0, lastIndex));

					Thread.sleep(1000);
					toponymVector = extractToponymFromResponse(response, toponymVector, startIndex);
					
					text = text.substring(lastIndex);
					startIndex = lastIndex;
					
				} while (text.length()>0);
				
				
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
			StdOut.println("GeoTxt has finished processing "+corpus_name+". It took "+df.format(elapsedTime)+" minutes.");		

		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
	}
	
	// extract and save information of GeoTxt annotated toponyms
	private Vector<String> extractToponymFromResponse(String response, Vector<String> toponymVector, int startIndex)
	{
		JSONObject jsonObject = new JSONObject(response);
		JSONArray toponymArray = jsonObject.getJSONArray("features");
		for(int i=0;i<toponymArray.length();i++)
		{
			JSONObject thisToponymObject = toponymArray.getJSONObject(i);
			if(thisToponymObject.has("geometry") && (!thisToponymObject.get("geometry").toString().equals("null")))
			{
				JSONObject thisGeometryObject = thisToponymObject.getJSONObject("geometry");
				double latitude = thisGeometryObject.getJSONArray("coordinates").getDouble(1);
				double longitude = thisGeometryObject.getJSONArray("coordinates").getDouble(0);
				
				JSONObject thisPropertyObject = thisToponymObject.getJSONObject("properties");
				JSONArray thisPositionArray = thisPropertyObject.getJSONArray("positions");
				for(int j = 0;j<thisPositionArray.length();j++)
				{
					int position = thisPositionArray.getInt(j);
					String name = thisPropertyObject.getString("name");
					String toponym = thisPropertyObject.getString("toponym");
					toponymVector.add(toponym+",,"+name+",,"+latitude+",,"+longitude+",,"+ (startIndex+position)+",,"+(startIndex+position+name.length()));
				}
			}
		}
		
		return toponymVector;
	}
	
	// connect with the constructed GeoTxt geoparsing request
	private String getGeoTxtResponse(String url, String text)
	{
		try 
		{
			url = url + text;
			
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			
			// optional default is GET
			con.setRequestMethod("GET");
			con.setConnectTimeout(60000); // time out after one minute
			

			//add request header
			con.setRequestProperty("User-Agent", USER_AGENT);

			int responseCode = -1; 
			BufferedReader in = null;
			try
			{
				responseCode = con.getResponseCode();
				in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			}
			catch (Exception ee) {
				StdOut.println("Unsuccessful result returned by GeoTxt...");
				return "{\"features\":[]}";
			}
			
			
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
				return "{\"features\":[]}";
			}
			
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		return null;
	}

}
