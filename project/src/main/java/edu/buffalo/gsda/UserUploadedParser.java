package edu.buffalo.gsda;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.Out;
import edu.princeton.cs.algs4.StdOut;

public class UserUploadedParser 
{
	private final String USER_AGENT = "Mozilla/5.0";
	private String parserAPI;
	private String serverName;
	private String firstArticle;
	private Boolean serviceStatusCode;
	private Boolean ifTestCode;
	
	public UserUploadedParser(String api,String name,Boolean code)
	{
		this.ifTestCode = code;
		this.parserAPI = api;
		if(ifTestCode) 
		{
			this.serverName = getFileNameByTime()+ name;
		}
		else
		{
			this.serverName = name;
		}
		this.serviceStatusCode = false;
	}
	
	public String getFirstArticle() 
	{
		return firstArticle;
	}
	
	public String getFinalName() 
	{
		return serverName;
	}
	
	public String getFileNameByTime() 
	{
		 LocalDateTime currentTime = LocalDateTime.now(); 	
		 String temp = currentTime.toLocalDate().toString()+"_" + String.valueOf(currentTime.getHour())+ String.valueOf(currentTime.getMinute());
		 return temp;
	}
	public Boolean getServiceCode() 
	{
		return serviceStatusCode;
	}
	
	// main function of using user uploaded geoparser to process the corpus (no text length limitation)
	public void parseWithoutLimit(String corpus_name) 
	{
		try 
		{
			// A utils class used for getting the current path of a Java class
			Utils utils = new Utils();
			
			// Get the path of corpus and geoparsing output
			String parsedResultPath = utils.getPath()+File.separator+"geoparser_output"+File.separator+corpus_name+"_" + serverName + ".txt";
			String corpusFolderPath = utils.getPath()+File.separator+"corpora"+File.separator+corpus_name;
			//StdOut.println(corpusFolderPath);
			File folder = new File(corpusFolderPath);
			File[] corpusFiles = folder.listFiles();
			int fileCount = corpusFiles.length;
			//StdOut.println(fileCount);
			// create the output file
			Out geoparsedResult = new Out(parsedResultPath);
			
			StdOut.println("Starting User's Geoparser on "+corpus_name+" ...");
			// parse each file 
			for(int fileIndex=0;fileIndex<fileCount;fileIndex++)
			{
				// get input geo-corpora file contents 
				In input = new In(corpusFolderPath+File.separator+fileIndex);
				String text = input.readAll();
				if (fileIndex == 0) 
				{
					firstArticle = text;
				}
				StdOut.println("Processing file "+fileIndex+" ...");
				
				Vector<String> toponymVector = new Vector<>();
				
				text = URLEncoder.encode(text, "UTF-8");
				
				
				String response = getGeoparserResponse(text);
				toponymVector = extractToponymFromResponse(response, toponymVector, 0);
			
				
				// write the output into the parsing result file
				String resultLine = "";
				for(String toponymRecord : toponymVector)
					resultLine += toponymRecord+"||";
				
				//StdOut.println(resultLine);
				geoparsedResult.println(resultLine);
		
			}
			geoparsedResult.close();
			StdOut.println("Finished User's Geoparser on "+corpus_name+".");
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	// main function of using user uploaded geoparser to process the corpus (text length limitation of 3000)
	public void parse(String corpus_name)
	{
		try 
		{
			// A utils class used for getting the current path of a Java class
			Utils utils = new Utils();
			
			// Get the path of corpus and geoparsing output
			String parsedResultPath = utils.getPath()+File.separator+"geoparser_output"+File.separator+corpus_name+"_" + serverName + ".txt";
			String corpusFolderPath = utils.getPath()+File.separator+"corpora"+File.separator+corpus_name;
			//StdOut.println(corpusFolderPath);
			File folder = new File(corpusFolderPath);
			File[] corpusFiles = folder.listFiles();
			int fileCount = corpusFiles.length;
			//StdOut.println(fileCount);
			// create the output file
			Out geoparsedResult = new Out(parsedResultPath);
			
			StdOut.println("Starting User's Geoparser on "+corpus_name+" ...");
			// parse each file 
			for(int fileIndex=0;fileIndex<fileCount;fileIndex++)
			{
				StdOut.println("Processing file "+fileIndex+" ...");
				
				// get input geo-corpora file contents 
				In input = new In(corpusFolderPath+File.separator+fileIndex);
				String text = input.readAll();
				if (fileIndex == 0) 
				{
					firstArticle = text;
				}

				Vector<String> toponymVector = new Vector<>();
				
				text = URLEncoder.encode(text, "UTF-8");
				int startIndex = 0;
				do
				{
					int lastIndex = -1;
					// What the purpose of using index? 
					if(text.length()>3000)
					{
						lastIndex = text.lastIndexOf("+", 3000);
						
						if(lastIndex < 2000) // if a space cannot be found; applies to non-English words
							lastIndex = 3000;
					}
					else {
						lastIndex = text.length();
					}
					 
					String response = getGeoparserResponse(text.substring(0, lastIndex));
					//StdOut.println(response);
					Thread.sleep(1000);
					if(serviceStatusCode !=true)
					{
						break;
					}
					toponymVector = extractToponymFromResponse(response, toponymVector, startIndex);
					
					text = text.substring(lastIndex);
					startIndex = lastIndex;
					
				} while (text.length()>0);
				
				
				// write the output into the parsing result file
				String resultLine = "";
				for(String toponymRecord : toponymVector)
					resultLine += toponymRecord+"||";
				
				//StdOut.println(resultLine);
				geoparsedResult.println(resultLine);
		
			}
			geoparsedResult.close();
			StdOut.println("Finished User's Geoparser on "+corpus_name+".");
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
	}
	
	// extract information of annotated toponyms based on EUPEG unified format
	private Vector<String> extractToponymFromResponse(String response, Vector<String> toponymVector, int textArticleIndex)
	{
		JSONObject jsonObject = new JSONObject(response);
		JSONArray toponymArray = jsonObject.getJSONArray("toponyms");
		for(int i=0;i<toponymArray.length();i++)
		{
			JSONObject thisToponymObject = toponymArray.getJSONObject(i);
			
			String phraseName = thisToponymObject.getString("phrase");
			int startIndex = thisToponymObject.getInt("start");
			int endIndex = thisToponymObject.getInt("end");
			
			JSONObject thisGeometryObject = thisToponymObject.getJSONObject("place");
			
			JSONArray coordinatesArray = thisGeometryObject.getJSONArray("footprint");
			double latitude = 0;
			double longitude = 0;
			
			// POINT TYPE
			for(int j=0;j<coordinatesArray.length();j++)
			{
				latitude = coordinatesArray.getJSONArray(j).getDouble(1);
				longitude = coordinatesArray.getJSONArray(j).getDouble(0);
			}
			
			// Other geometry
			
			String officialName = "null";
			if(thisGeometryObject.has("placename"))
				officialName = thisGeometryObject.getString("placename");
			
			toponymVector.add(officialName+",,"+phraseName+",,"+latitude+",,"+longitude+",,"+ String.valueOf(textArticleIndex+startIndex) + ",,"+ String.valueOf(textArticleIndex + endIndex));
		}
		
		return toponymVector;
	}
	
	// process the geoparser response in EUPEG unified format
	private String getGeoparserResponse(String text)
	{
		try 
		{
			String url = parserAPI + text;
			//System.out.println(url);
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
				StdOut.println("Unsuccessful result returned by uploaded parser...");
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
				serviceStatusCode = true;
				return response.toString();
			}
			else 
			{
				StdOut.println("Response Code : " + responseCode);
				StdOut.println(response.toString());
				serviceStatusCode = false;
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