package edu.buffalo.gsda;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.util.Hashtable;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import edu.princeton.cs.algs4.Out;
import edu.princeton.cs.algs4.StdOut;

public class EdinburgParser 
{

	// main function of using Edinburgh geoparser tool to process selected corpus
	public void parse(String corpus_name,Boolean if_backup)
	{
		try 
		{
			// A utils class used for getting the current path of a Java class
			Utils utils = new Utils();
			
			String parsedResultPath = null;
			String corpusFolderPath = utils.getPath()+File.separator+"corpora"+File.separator+corpus_name;			
			// save the output file under backup folder when updating
			if(if_backup==true)
			{
				parsedResultPath = utils.getPath()+File.separator+"output_backup"+File.separator+corpus_name+"_Edinburgh.txt";
			}
			else 
			{
				parsedResultPath = utils.getPath()+File.separator+"geoparser_output"+File.separator+corpus_name+"_Edinburgh.txt";
			}
			
			File folder = new File(corpusFolderPath);
			File[] corpusFiles = folder.listFiles();
			int fileCount = corpusFiles.length;
			
			// create the output file
			Out geoparsedResult = new Out(parsedResultPath);
			
			// get the path of Edinburgh parser
			String parserPath = EUPEGConfiguration.Edinburgh_Path;
			
			// start message and recording time
			StdOut.println("Starting Edinburgh Geoparser on "+corpus_name+" ...");		
			long startTime = System.currentTimeMillis(); // start time

			// parse each file using Edinburgh
			for(int fileIndex=0;fileIndex<fileCount;fileIndex++)
			{
				StdOut.println("Processing file "+fileIndex+" ...");
				
				String filePath = corpusFolderPath+File.separator+fileIndex;
				String response = getEdinburgResponse(filePath, parserPath); 
				
				//Thread.sleep(750);
				
				Vector<String> toponymVector = new Vector<>();
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
			StdOut.println("Edinburgh Geoparser has finished processing "+corpus_name+". It took "+df.format(elapsedTime)+" minutes.");		
			
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
	}
	
	// extract and save information of annotated toponyms by Edinburgh geoparser
	private Vector<String> extractToponymFromResponse(String response, Vector<String> toponymVector)
	{
		if(response.length() == 0)
			return toponymVector;
		try 
		{
			DocumentBuilderFactory docbf = DocumentBuilderFactory.newInstance();
			docbf.setNamespaceAware(true);
			DocumentBuilder docbuilder = docbf.newDocumentBuilder();
			Document doc = docbuilder.parse(new InputSource(new StringReader(response)));
			
			Vector<Hashtable<String, Object>> targets = new Vector<>();
			
			 // Create XPathFactory object
            XPathFactory xpathFactory = XPathFactory.newInstance();
            // Create XPath object
            XPath xpath = xpathFactory.newXPath();
            
           
            XPathExpression expr = xpath.compile("/document/standoff/ents[@source='ner-rb']/ent[@type='location']");
            //evaluate expression result on XML document
            NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++)
            {
            	Element thisNode = (Element)nodes.item(i);
            	String latitude = thisNode.getAttribute("lat");
            	String longitude = thisNode.getAttribute("long"); 
            	String surfaceName = thisNode.getTextContent().trim();
            	
            	if(latitude == null || longitude == null || latitude.equalsIgnoreCase("null") || longitude.equalsIgnoreCase("null"))
            		continue;
            	
            	Element partsElement = (Element)thisNode.getElementsByTagName("parts").item(0);
            	Element toponymnNameNode = (Element)partsElement.getElementsByTagName("part").item(0);
            	//StdOut.println(toponymnNameNode.getAttribute("sw"));
            	
            	
            	Hashtable<String, Object> recordHashtable = new Hashtable<>();
            	recordHashtable.put("name", surfaceName);
            	recordHashtable.put("node", toponymnNameNode);
            	recordHashtable.put("lat", latitude);
            	recordHashtable.put("lng", longitude);
            	
            	targets.add(recordHashtable);
            }
            
            for(Hashtable<String, Object> target : targets)
            {
            	int index = 0, start = 0, end = 0;
            	Element toponymNode = (Element)target.get("node");
            	String sw = toponymNode.getAttribute("sw");
            	String ew = toponymNode.getAttribute("ew");
            	
            	XPathExpression expr2 = xpath.compile("/document/text/p/s/w");
                //evaluate expression result on XML document
                NodeList wordNodes = (NodeList) expr2.evaluate(doc, XPathConstants.NODESET);
                
                for (int i = 0; i < wordNodes.getLength(); i++)
                {
                	Element word = (Element)wordNodes.item(i);
                	
                	if(word.getAttribute("id").equals(sw))
                        start = index;
                    index += word.getTextContent().length();
                    if(word.getAttribute("id").equals(ew))
                        end = index;
                    if(!word.getAttribute("pws").equals("no"))
                        index += 1;
                
                }
                
                if(start == 0 && end == 0)
                    StdOut.println("can't find the start and end position of toponym "+target.get("name").toString());
                
                toponymVector.add("No Gaz" + ",," + target.get("name").toString() + ",," + target.get("lat").toString() + ",," + target.get("lng").toString() + ",," + start + ",," + end);
                	
            }
            
            return toponymVector;
	
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		
		return toponymVector;
		
	}
	
	// use command builder to annotate one single article via command builder
	private String getEdinburgResponse(String filePath, String parserPath)
	{
		try 
		{
	        String command = "cat "+filePath + " | "+parserPath+" " + "-t plain -g geonames -top";

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
