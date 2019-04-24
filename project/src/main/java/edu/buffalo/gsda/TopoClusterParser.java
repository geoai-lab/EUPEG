package edu.buffalo.gsda;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.Hashtable;
import java.util.Vector;

import org.json.JSONArray;

import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.Out;
import edu.princeton.cs.algs4.StdOut;

public class TopoClusterParser 
{
	
	// main function of using TopoCluster to process the selected corpus
	public void parse(String corpus_name)
	{
		try 
		{
			// A utils class used for getting the current path of a Java class
			Utils utils = new Utils();
			
			// Get the paths of the corpus and parsing output
			String parsedResultPath = utils.getPath()+File.separator+"geoparser_output"+File.separator+corpus_name+"_TopoCluster.txt";
			String corpusFolderPath = utils.getPath()+File.separator+"corpora"+File.separator+corpus_name;
			
			File folder = new File(corpusFolderPath);
			File[] corpusFiles = folder.listFiles();
			int fileCount = corpusFiles.length;
			
			// create the output file
			Out geoparsedResult = new Out(parsedResultPath);
			
			// get the path of Topo parser
			String parserPath = EUPEGConfiguration.Topo_Path;
			
			// start message and recording time
			StdOut.println("Starting TopoCluster on "+corpus_name+" ...");		
			long startTime = System.currentTimeMillis(); // start time
					
			// parse each file using TopoCluster
			for(int fileIndex=0;fileIndex<fileCount;fileIndex++)
			{
				StdOut.println("Processing file "+fileIndex+" ...");
				
				String filePath = corpusFolderPath+File.separator+fileIndex;
				String response = getTopoResponse(filePath, parserPath); 
				
				Thread.sleep(1000);
				In inputFile = new In(filePath);
				String fileContent = inputFile.readAll();
				inputFile.close();
				
				Vector<String> toponymVector = new Vector<>();
				toponymVector = extractToponymFromResponse(fileContent, response, toponymVector);
				
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
			StdOut.println("TopoCluster has finished processing "+corpus_name+". It took "+df.format(elapsedTime)+" minutes.");	
			
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
	}
	
	// extract and save information of annotated toponyms by TopoCluster
	private Vector<String> extractToponymFromResponse(String originalText, String response, Vector<String> toponymVector)
	{
		if(response.length() == 0)
			return toponymVector;
		
		try 
		{
			Hashtable<String, Integer> currentIndexTable = new Hashtable<>();
			String[] toponyms = response.split("==========");
			
			for(int i=1;i<toponyms.length;i++)
			{
				String thisToponym = toponyms[i];
				int startIndex = thisToponym.lastIndexOf("[");
				int endIndex = thisToponym.lastIndexOf("]");
				thisToponym = thisToponym.substring(startIndex, endIndex+1);
				thisToponym = thisToponym.replace("\\", "");  // remove special utf-8 characters
				
				try // surround the following code with try catch, because some special character can lead to error in constructing JSON Array
				{
					JSONArray topoInfoArray = new JSONArray(thisToponym);
					
					String phrase = topoInfoArray.getString(0);
					float lat = topoInfoArray.getFloat(7);
					float lng = topoInfoArray.getFloat(8);
					
					int previousIndex = 0;
					Integer previousIndexInteger = currentIndexTable.get(phrase);
					if(previousIndexInteger != null)
						previousIndex = previousIndexInteger.intValue();
					
					String currentText = originalText.substring(previousIndex);
					int start = previousIndex + currentText.indexOf(phrase);
					int end = start + phrase.length();
					currentIndexTable.put(phrase, end);
					toponymVector.add("No Gaz" + ",," + phrase + ",," + lat + ",," + lng + ",," + start + ",," + end);
				}
				catch (Exception e) 
				{
					e.printStackTrace();
				}
			}

            return toponymVector;
	
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}

		return toponymVector;
		
	}
	
	
	// load local TopoCluster geoparser to processes one article using command builder
	private String getTopoResponse(String filePath, String parserPath)
	{
		try 
		{
	        String command = EUPEGConfiguration.Topo_Virtual_Env+" "+parserPath+"/TopoCluster.py -mode plain_topo_resolve -outdomain_stat_tbl enwiki20130102_train_kernel100k_grid5_epanech_allwords_ner_fina -tstf "+filePath+" -conn \"dbname=topocluster user=gsda host='localhost' password='gsda831'\" -gtbl globalgrid_5_clip_geog -percentile 1.0 -window 15 -main_topo_weight 40.0 -other_topo_weight 5.0 -other_word_weight 1.0 -country_tbl countries_2012 -region_tbl regions_2012 -state_tbl states_2012 -geonames_tbl geonames_all -out_domain_lambda 1.0 -stan_path "+parserPath+"/stanford-ner-2014-06-16";

	        ProcessBuilder pb = new ProcessBuilder("sh","-c", command);
            pb.redirectError();
            Process p = pb.start();
            BufferedReader reader =  new BufferedReader(new InputStreamReader(p.getInputStream()));
          
            String response = "";
            String inputLine = null;
            while ((inputLine = reader.readLine()) != null) 
            {
            	response += inputLine;
            }

			return response;
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		return null;
	}
  
}
