package edu.buffalo.gsda;
import java.util.TimerTask;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class UpdateResultTask extends TimerTask
{
	private List<String> corpusNameList = Arrays.asList("LGL","GeoVirus","TR-News","GeoWebNews","WikToR","GeoCorpora","Hu2014","Ju2016");
	// private List<String> corpusNameList2 = Arrays.asList("Hu2014");
	
	//main function of TimerTask to automatically update the geoparsing results and geoparsers' version information
	public  void  run()  
	{   
		Calendar cal = Calendar.getInstance(); 	
		int day = cal.get(Calendar.DAY_OF_MONTH);
		
		// update all results: 1) at the first day of each month; or 2)The first time start the server
		if(day == 0||EUPEGConfiguration.if_first_update == true)
		{
			
			EUPEGConfiguration.if_first_update = false;
			
			// update the version information when updating finished
			refreshVersion();		
			System.out.println("EUPEG has finished updating version info!");
			
			// get the new results of geoparsers
			loadGeoparsers();	
			System.out.println("EUPEG has finished updating hosted geoparsing results!");
			
			// update the files under output folder
			replaceFile();
					
		}
		
	}    
	
	//updating the geoparsing results of GeoTxt, DBpedia Spotlight, SpacyNER, and StanfordNER
	protected void loadGeoparsers() 
	{
		// Get the paths of the corpus and parsing output
		for(int i = 0; i<corpusNameList.size();i++)
		{
			String corpus_name = corpusNameList.get(i);
			
			/*--- GeoTxt ---*/
			GeoTxtParser geotxt_month = new GeoTxtParser();
			geotxt_month.parse(corpus_name,true);
			
			/*--- DBSpotlight ---*/
			DBpediaParser dbpedia_month = new DBpediaParser();
			dbpedia_month.parse(corpus_name,true);
			
			
			/*--- SpaCyNER ---*/
			SpaCyNERParser spacy_month = new SpaCyNERParser();
			spacy_month.parse(corpus_name,true);
			
			/*--- StanfordNER ---*/
			StanfordNERParser stanford_month = new StanfordNERParser();
			stanford_month.parse(corpus_name,true);
			
			/*--- The Edinburgh Geoparser ---*/
			EdinburgParser edinburgh_month = new EdinburgParser();
			edinburgh_month.parse(corpus_name, true);
		}
		
	}
	
	//replace the last month geoparsing results with new ones
	protected void replaceFile() 
	{
		Utils utils = new Utils();
		String backupFolder = utils.getPath()+File.separator+"output_backup";
		String geoparserOutput = utils.getPath()+File.separator+"geoparser_output";
		File folder = new File(backupFolder);
		File[] corpusFiles = folder.listFiles();
		
		for(File item : corpusFiles)
		{
			Path sourceDirectory = Paths.get(backupFolder+File.separator+item.getName());
	        Path targetDirectory = Paths.get(geoparserOutput+File.separator+item.getName());
	        try 
	        {
	        	// copy file to output folder with replace_existing
	            Files.copy(sourceDirectory, targetDirectory,StandardCopyOption.REPLACE_EXISTING);
	            
	            // delete the copied file at backup folder
	            Files.deleteIfExists(sourceDirectory);
	        } catch (IOException e) {
	            System.out.println(e.toString());
	        }
		}
		
	}
	
	//refresh dynamic gazetteer version at the beginning of each month
	protected void refreshVersion() 
	{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM"); 
		String exp_date = formatter.format(new Date());
		
		EUPEGConfiguration.gazetteer_version_dic.replace("DBpedia", exp_date);
		
		try {
			String url = "http://api.geonames.org/versionJSON?username=PutYourAccountHere";
			
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection)obj.openConnection();
			
			// optional default is GET
			con.setRequestMethod("GET");
			con.setConnectTimeout(60000); // time out after one minute
			
	
			// add request header
			con.setRequestProperty("User-Agent", "Mozilla/5.0");
			
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
				JSONObject versionObj = new JSONObject(response.toString());
				// System.out.println(versionObj);
				String geoname_version = versionObj.getJSONObject("version").getString("softwareVersion");
				
				EUPEGConfiguration.gazetteer_version_dic.replace("StanfordNER", geoname_version);
				EUPEGConfiguration.gazetteer_version_dic.replace("SpaCyNER", geoname_version);	
				EUPEGConfiguration.gazetteer_version_dic.replace("Edinburgh", geoname_version);
			}
		}
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
