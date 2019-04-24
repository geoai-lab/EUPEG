package edu.buffalo.gsda;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import edu.princeton.cs.algs4.In;




public class EvaluationServlet extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
        	
    public EvaluationServlet() 
    {
        super();
    }

    private List<String> corpusNameList = Arrays.asList("LGL","GeoVirus","TR-News","GeoWebNews","WikToR","GeoCorpora","Hu2014","Ju2016");
    private List<String> parserNameList = Arrays.asList("GeoTxt","Edinburgh","TopoCluster","CLAVIN","Yahoo","CamCoder","StanfordNER","SpaCyNER","DBpedia");
    
    // test if one particular output file already saved 
    public boolean if_Output_file(String datasetPath, String parsePath, Utils putils) 
    {
    	 StringBuilder datasetParseTxt = new StringBuilder(putils.getPath());
    
    	 datasetParseTxt.append(File.separator+"geoparser_output");
    	 datasetParseTxt.append(File.separator);
    	 datasetParseTxt.append(datasetPath);
    	 datasetParseTxt.append("_");
    	 datasetParseTxt.append(parsePath);
    	 datasetParseTxt.append(".txt");
    	
    	 File file = new File(datasetParseTxt.toString());
    	 return file.exists();		
	}
    
    // process the request of running experiment
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		// get list of selected corpora
		String datasets = request.getParameter("datasets");
		// get list of selected geoparsers
		String parsers = request.getParameter("parsers");
		// get experiment ID
		String idCode = request.getParameter("experimentID");
		// get list of selected evaluation metrics
		String metricsString = request.getParameter("metricsList");
		
		ArrayList<String> myList = new ArrayList<String>(Arrays.asList(metricsString.split(",")));
		
		// get experiment time
		SimpleDateFormat formatter_time = new SimpleDateFormat("MMM. dd, yyyy hh:mm:ss aa"); 
		String exp_time = formatter_time.format(new Date());
		
		SimpleDateFormat formatter_date = new SimpleDateFormat("MMM. yyyy"); 
		String exp_date = formatter_date.format(new Date());
		
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		
		String[] datasetArray = datasets.split("\\|");
		String[] parserArray = parsers.split("\\|");
		
		Utils utils = new Utils();
		JSONObject resultObject = new JSONObject();
		DatabaseOperator operator = new DatabaseOperator();
		
		
		for(int i=0;i<datasetArray.length;i++)
			for(int j=0;j<parserArray.length;j++)
			{
				// if geoparse-Corpus outfut file already exist or not
				boolean temp = if_Output_file(datasetArray[i], parserArray[j], utils);
				// if exist, do calculation
				if(temp)
				{						
					try 
					{
						String version_parser = EUPEGConfiguration.geoparser_version_dic.get(parserArray[j]);
						String version_gaze = EUPEGConfiguration.gazetteer_version_dic.get(parserArray[j]);
						
						resultObject.put(datasetArray[i]+"_"+parserArray[j],utils.calculateScores(utils.getPath()+File.separator+"geoparser_output"+File.separator+datasetArray[i]+"_"+parserArray[j]+".txt", utils.getPath()+File.separator+"gold"+File.separator+datasetArray[i]+"_gold.txt", false, myList));
						HashMap<String, Double> insertMaps =utils.getAllMetrics();
						exp_time = formatter_time.format(new Date());
						operator.insertRecord(idCode,datasetArray[i], parserArray[j],exp_time,version_parser,version_gaze,insertMaps);				
					} 
					catch (JSONException e) 
					{						
						e.printStackTrace();
					} 
					catch (Exception e) 
					{
						e.printStackTrace();
					}
				}
				// if not exist, do parsing before calculation
				else
				{	
					
					switch (parserArray[j]) {
					case "GeoTxt":
						GeoTxtParser geoTxtParser = new GeoTxtParser();
						geoTxtParser.parse(datasetArray[i],false);
						// get the time of finishing experiment
						exp_time = formatter_time.format(new Date());
						break;
					case "Yahoo":
						YahooParser yahooParser = new YahooParser();
						yahooParser.parse( datasetArray[i]);
						// get the time of finishing experiment
						exp_time = formatter_time.format(new Date());
						break;
					case "Edinburgh":
						EdinburgParser edinburgParser = new EdinburgParser();
						edinburgParser.parse( datasetArray[i],false);
						// get the time of finishing experiment
						exp_time = formatter_time.format(new Date());
						break;
					case "StanfordNER":
						StanfordNERParser stanfordner = new StanfordNERParser();
						stanfordner.parse( datasetArray[i],false);
						// get the time of finishing experiment
						exp_time = formatter_time.format(new Date());
						break;
					case "CLAVIN":
						CLAVINParser clavinParser = new CLAVINParser();
						clavinParser.parse(datasetArray[i]);
						// get the time of finishing experiment
						exp_time = formatter_time.format(new Date());
						break;
					case "DBpedia":
						DBpediaParser dbpediaParser = new DBpediaParser();
						dbpediaParser.parse(datasetArray[i],false);
						// get the time of finishing experiment
						exp_time = formatter_time.format(new Date());
						break;
					case "TopoCluster":
						TopoClusterParser topoClusterParser = new TopoClusterParser();
						topoClusterParser.parse(datasetArray[i]);
						// get the time of finishing experiment
						exp_time = formatter_time.format(new Date());
						break;
					case "SpaCyNER":
						SpaCyNERParser spaCyNERParser = new SpaCyNERParser();
						spaCyNERParser.parse(datasetArray[i],false);
						// get the time of finishing experiment
						exp_time = formatter_time.format(new Date());
						break;
					case "CamCoder":
						CamCoderParser camcoderparser = new CamCoderParser();
						camcoderparser.parse(datasetArray[i]);
						// get the time of finishing experiment
						exp_time = formatter_time.format(new Date());
						break;
					default:
						// read url of uploaded geoparser
						In parserFile = new In(utils.getPath()+File.separator+"geoparser"+File.separator + parserArray[j] + ".csv");
						String tempAPI = (parserFile.readAll().split(","))[1];
						parserFile.close();	
						// connect with the API of uploaded geoparser
						UserUploadedParser tempParser = new UserUploadedParser(tempAPI, parserArray[j],false);
						tempParser.parse(datasetArray[i]);
						// get the time of finishing experiment
						exp_time = formatter_time.format(new Date());
						break;
				    }
					
					// begin evaluation step
					try 
					{
						String nickCorpusName = datasetArray[i];
						String nickParserName = parserArray[j];
						String version_parser = exp_date;
						String version_gaze = exp_date;
						
						if(!corpusNameList.contains(datasetArray[i]))
						{
							nickCorpusName = operator.searchDataSetName(idCode,datasetArray[i]);
				        }
						
						if(!parserNameList.contains(parserArray[i]))
						{
							nickParserName = operator.searchParserName(idCode, parserArray[j]);
				        }
						else 
						{
							version_parser = EUPEGConfiguration.geoparser_version_dic.get(nickParserName);
							version_gaze = EUPEGConfiguration.gazetteer_version_dic.get(nickParserName);
						}
						
						resultObject.put(datasetArray[i]+"_"+parserArray[j],utils.calculateScores(utils.getPath()+File.separator+"geoparser_output"+File.separator+datasetArray[i]+"_"+parserArray[j]+".txt", utils.getPath()+File.separator+"gold"+File.separator+datasetArray[i]+"_gold.txt", false, myList));
						HashMap<String, Double> insertMaps =utils.getAllMetrics();
						operator.insertRecord(idCode,nickCorpusName, nickParserName,exp_time,version_parser,version_gaze,insertMaps);
					} 
					catch (JSONException e) {
						e.printStackTrace();
					} 
					catch (Exception e) {
						e.printStackTrace();
					}
				
				}
			}
		
		out.print(resultObject.toString());
		

	}

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		doGet(request, response);
	}

}
