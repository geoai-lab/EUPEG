package edu.buffalo.gsda;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.Out;
import edu.princeton.cs.algs4.StdOut;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;



public class StanfordNERParser 
{
	private final String USER_AGENT = "Mozilla/5.0";
	private String latitude;
	private String longitude;
	private String toponym;
	
	// main function of using StanfordNER and GeoNames Web Service to process selected corpus
	public void parse(String corpus_name,Boolean if_backup) 
	{
		try 
		{
			// A utils class used for getting the current path of a Java class
			Utils utils = new Utils();
						
			// Get the path of the output
			String parsedResultPath = null;
			String corpusFolderPath = utils.getPath()+File.separator+"corpora"+File.separator+corpus_name;
			
			//save the output file under backup folder when updating
			if(if_backup==true)
			{
				parsedResultPath = utils.getPath()+File.separator+"output_backup"+File.separator+corpus_name+"_StanfordNER.txt";
			}
			else 
			{
				parsedResultPath = utils.getPath()+File.separator+"geoparser_output"+File.separator+corpus_name+"_StanfordNER.txt";
			}
			
			File directory = new File(corpusFolderPath);
			File[] files = directory.listFiles();
			int fileCount = files.length;
			
			// prepare Stanford NER toolbox
			Properties props = new Properties();
			// set annotators type
			props.put("annotators","tokenize,ssplit,pos,lemma,ner");
			// limit results of ner only within [location, person, organization] 
			props.setProperty("ner.applyFineGrained", "false");
			StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
			
			// Set up the output file 
			Out output = new Out(parsedResultPath);
			
			// start message and recording time
			StdOut.println("Starting StanfordNER on "+corpus_name+" ...");		
			long startTime = System.currentTimeMillis(); // start time
		
			
			for(int fileIndex = 0; fileIndex<fileCount; fileIndex++)
			{
				StdOut.println("Processing file "+fileIndex+" ...");
				String allNERRecord = "";
				
				try
				{
					In input = new In(corpusFolderPath+File.separator+fileIndex);
					String text = input.readAll();
					
					Annotation textAnnotation = new Annotation(text);
					pipeline.annotate(textAnnotation);
					
					Hashtable<String, String> existingCoordTable = new Hashtable<>(); // a hashtable used for storing the location of place names in order to save GeoNames queries
					Hashtable<String, String> existingTrueNameTable = new Hashtable<>();  // a hashtable used for storing the location of place names in order to save GeoNames queries

					
					List<CoreMap> sentences = textAnnotation.get(SentencesAnnotation.class);
					for (CoreMap sentence : sentences) 
					{
						Integer index = 0;
						Integer index2 = 0;
						//Integer index3;
						List<CoreLabel> tokens = sentence.get(TokensAnnotation.class);
						String lastNERType = "";
						String lastNER = "";
						String lastNERRecord = "";
						
						for (CoreLabel token : tokens) 
						{
							String nerType = token.get(NamedEntityTagAnnotation.class);
							if(nerType.equals("LOCATION"))
							{
								if(nerType.equals(lastNERType)) 
								{
									lastNER += " "+token.originalText();
									index2 = token.endPosition(); //end position if many words per toponym
								}
								else 
								{
									lastNER = token.originalText();		
									index = token.beginPosition();	//start position
									index2 = token.endPosition();   // end position if one word per toponym
								}
									
							}
							else if (lastNERType.equals("LOCATION")) 
							{
								String existingInfo = existingCoordTable.get(lastNER);
								if(existingInfo != null)
								{
									String trueName = existingTrueNameTable.get(lastNER);
									String record = null;
									if(trueName != null)
									   record = trueName+",,"+lastNER +",,"+existingInfo+",,"+index.toString()+",,"+index2.toString();
									else
									   record = lastNER+",,"+lastNER +",,"+existingInfo+",,"+index.toString()+",,"+index2.toString();
									
									allNERRecord += record+"||";
								}
								else
								{
									boolean temp = getGazeTopoByPops(lastNER);
									
									if(temp) // if found coordinates, then record this toponym
									{
										lastNERRecord = this.toponym;
										lastNERRecord += ",," + lastNER + ",," + this.latitude +",," + this.longitude + ",,"+index.toString()+",,"+index2.toString();
									
									
										allNERRecord += lastNERRecord+"||";
										
										existingCoordTable.put(lastNER, this.latitude +",," + this.longitude);
										existingTrueNameTable.put(lastNER, this.toponym);
									}
								}
								
								lastNER = "";
								lastNERRecord = "";
							}
							lastNERType = nerType;
						}
					}
				}
				catch (Exception e) // if any unexpected error happens
				{
					Thread.sleep(10000);
					e.printStackTrace();
					allNERRecord = "";  
				}
				
				output.println(allNERRecord);	
 			}
			output.close();
			
			// end message and recording time
			long stopTime = System.currentTimeMillis(); //stop time
		    double elapsedTime = (stopTime - startTime)*1.0 / 60000.0; // elapsed time
		    DecimalFormat df = new DecimalFormat();
		    df.setMaximumFractionDigits(2);
			StdOut.println("StanfordNER has finished processing "+corpus_name+". It took "+df.format(elapsedTime)+" minutes.");	
			
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	
	// search for the name of annotated location in the GeoNames DB and choose the location with most population
	public boolean getGazeTopoByPops(String location) 
	{
		try 
		{
			Thread.sleep(3000);
			String[] geoNamesUser = {"add your GeoNames Account here (more than three to avoid errors"};
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

			//add request header
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
