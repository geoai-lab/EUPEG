package edu.buffalo.gsda;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.text.DecimalFormat;

import edu.princeton.cs.algs4.StdOut;

public class CamCoderParser {
	
	private String py_path = EUPEGConfiguration.CamCoder_Path;
	private String py_venv = EUPEGConfiguration.CamCoder_Virtual_Env; 
	
	public boolean setPy_Path(String path) 
	{
		this.py_path = path;
		return true;
	}
	
	
	// main function of using CamCoder to process the selected corpus
	public void parse(String corpus_name) 
	{
		try 
		{
			Utils utils = new Utils();
			
			// get paths of corpus and geoparsing output
			String parsedResultPath = utils.getPath()+File.separator+"geoparser_output"+File.separator+corpus_name+"_CamCoder.txt";
			String corpusFolderPath = utils.getPath()+File.separator+"corpora"+File.separator+corpus_name;
			
			// start message and recording time
			StdOut.println("Starting CamCoder on "+corpus_name+" ...");		
			long startTime = System.currentTimeMillis(); // start time
						
			runScriptProcess(corpusFolderPath, parsedResultPath);
			
			// end message and recording time
			long stopTime = System.currentTimeMillis(); //stop time
		    double elapsedTime = (stopTime - startTime)*1.0 / 60000.0; // elapsed time
		    DecimalFormat df = new DecimalFormat();
		    df.setMaximumFractionDigits(2);
			StdOut.println("CamCoder has finished processing "+corpus_name+". It took "+df.format(elapsedTime)+" minutes.");		
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// load CamCoder .py file using command builder
	public boolean runScriptProcess(String corpusPath, String outputPath) 
	{
		
		String command = this.py_venv+" "+this.py_path+" "+corpusPath+"/ "+outputPath;
		
		ProcessBuilder pb = new ProcessBuilder("sh","-c", command);//this.py_venv,this.py_path,corpusPath,outputPath);
		pb.redirectError();
		String temp = null;	
		String result = null;
		
		try 
		{
			
			Process p = pb.start();
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
		
			// read the output from the command
			while ((temp = stdInput.readLine()) != null)
			{
				result = temp;
			}
			StdOut.println(result);
			
			
			return true;
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			return false;
		}
	}
	
	
}
