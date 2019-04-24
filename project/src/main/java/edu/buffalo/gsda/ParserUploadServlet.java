package edu.buffalo.gsda;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.Out;


public class ParserUploadServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	

	public ParserUploadServlet() {
		super();
	}

	/*
	 * process the request of uploading the url of a new geoparser provided by users;
	 * -- Validate the connectivity of url 
	 * -- Test the geoparser on the testing article
	 * -- Return the parsing result to the browser
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		String parserName = request.getParameter("parserName");
		String parserURL = request.getParameter("parserURL");
		String experimentID = request.getParameter("experimentID");

		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();

		// use test set with geographic names to examine url
		// run parser
		UserUploadedParser newParser = new UserUploadedParser(parserURL, parserName, true);
		newParser.parse("test");
		if (!newParser.getServiceCode()) {
			out.print("Erro");
			return;
		} else {
			Utils utils = new Utils();

			String serverName = newParser.getFinalName();
			String text = newParser.getFirstArticle();
	
			String tempJson = readResult(serverName, text, utils);

			String parserURLData = utils.getPath() + File.separator + "geoparser" + File.separator + serverName + ".csv";
			Out outputFile = new Out(parserURLData);
			outputFile.print(serverName);
			outputFile.print(",");
			outputFile.print(parserURL);
			outputFile.close();

			DatabaseOperator operator = new DatabaseOperator();
			operator.addParserMetadata(experimentID, parserURL, parserName, serverName);

			out.print(tempJson);
		}
	}


	// extract and write the information of annotated toponyms into http response in json
	public String readResult(String serverName, String arcText, Utils utils) 
	{
		String parsedResultPath = utils.getPath() + File.separator + "geoparser_output" + File.separator + "test_"
				+ serverName + ".txt";
		In input = new In(parsedResultPath);
		String parsingText = input.readLine();
		String[] topoObects = parsingText.split("\\|\\|");
		List<Map<String, String>> plist = new ArrayList<Map<String, String>>();
		
		for (int i = 0; i < topoObects.length; i++) {
			String[] toponym = topoObects[i].split(",,");
			Map<String, String> pMap = new HashMap<String, String>();
			pMap.put("toponym", toponym[0]);
			pMap.put("start", toponym[4]);
			pMap.put("end", toponym[5]);
			plist.add(pMap);
		}
		Gson gson = new Gson();
		String json = gson.toJson(plist);
		Map<String, String> resultJson = new HashMap<String, String>();
		resultJson.put("text", arcText);
		resultJson.put("topoList", json);
		resultJson.put("parserName", serverName);
		String json2 = gson.toJson(resultJson);

		return json2;
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		doGet(request, response);
	}

}
