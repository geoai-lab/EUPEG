package edu.buffalo.gsda;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;

import com.bericotech.clavin.ClavinException;
import com.bericotech.clavin.GeoParser;
import com.bericotech.clavin.GeoParserFactory;
import com.bericotech.clavin.resolver.ResolvedLocation;

import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.Out;
import edu.princeton.cs.algs4.StdOut;


public class CLAVINParser 
{
	private String luceneIndexDirectory = EUPEGConfiguration.CLAVIN_Path;
	private GeoParser parser;
	
	public CLAVINParser() 
	{
		try 
		{
			parser = GeoParserFactory.getDefault(luceneIndexDirectory);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	
	// main function of using CLAVIN to process selected corpus
	public void parse(String corpus_name)
	{
		// a utils class used for getting the current path of a Java class
		Utils utils = new Utils();
					
		// get the paths of the corpus and parsing output
		String parsedResultPath = utils.getPath()+File.separator+"geoparser_output"+File.separator+corpus_name+"_CLAVIN.txt";
		String corpusFolderPath = utils.getPath()+File.separator+"corpora"+File.separator+corpus_name;
		File folder = new File(corpusFolderPath);
		File[] corpusFiles = folder.listFiles();
		int fileCount = corpusFiles.length;
					
		// create the output file
		Out geoparsedResult = new Out(parsedResultPath);
		
		// start message and recording time
		StdOut.println("Starting CLAVIN on "+corpus_name+" ...");		
		long startTime = System.currentTimeMillis(); // start time
						
		// parse each file using CLAVIN
		for(int fileIndex=0;fileIndex<fileCount;fileIndex++)
		{
			StdOut.println("Processing file "+fileIndex+" ...");
			
			In input = new In(corpusFolderPath+File.separator+fileIndex);
			String text = input.readAll();
			
			String resultLine = clavinParse(text, parser);				
			//StdOut.println(resultLine);					
			geoparsedResult.println(resultLine);			
		}
		geoparsedResult.close();
		
		// end message and recording time
		long stopTime = System.currentTimeMillis(); //stop time
	    double elapsedTime = (stopTime - startTime)*1.0 / 60000.0; // elapsed time
	    DecimalFormat df = new DecimalFormat();
	    df.setMaximumFractionDigits(2);
		StdOut.println("CLAVIN has finished processing "+corpus_name+". It took "+df.format(elapsedTime)+" minutes.");	
		
	}
	
	// extract and save information of CLAVIN annotated toponyms 
	@SuppressWarnings("finally")
	public String clavinParse(String text, GeoParser parser)
	{
		String resultLine = "";
		try 
		{		
			List<ResolvedLocation> resolvedLocations = parser.parse(text);
			for (int i =0; i<resolvedLocations.size(); i++) 
			{
				ResolvedLocation tempLocation = resolvedLocations.get(i);
				resultLine += tempLocation.getGeoname().getName();
				resultLine += ",," + tempLocation.getLocation().getText().replace("\n", "");  // some place names in text contain \n in their middle
				resultLine += ",," + tempLocation.getGeoname().getLatitude();
				resultLine += ",," + tempLocation.getGeoname().getLongitude();		
				resultLine += ",," + tempLocation.getLocation().getPosition();
				int endPosition = tempLocation.getLocation().getPosition() + tempLocation.getLocation().getText().length();
				resultLine += ",," + String.valueOf(endPosition) + "||" ;								
			}
		} 
		catch (ClavinException e) 
		{
			e.printStackTrace();
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally 
		{
			return resultLine;
		}
	}
}
