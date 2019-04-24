package edu.buffalo.gsda;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;

import edu.princeton.cs.algs4.StdOut;


public class ServiceConnectionTestServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
   
    public ServiceConnectionTestServlet() {
        super();
    }
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		String parserName = request.getParameter("parserName");
		String parserURL = request.getParameter("parserURL");
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		
		//test GeoNames Web Service
		if(parserName.equals("Version"))
		{
			String resultMessage = getServerVersion();
			out.print(resultMessage);
		}
		//test other geoparsers
		else 
		{
			String resultMessage = testServiceConnection(parserName,parserURL);
			out.print(resultMessage);
		}
	}

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		doGet(request, response);
	}
	
	//test the availability of geoparsers
	private String testServiceConnection(String parserName, String parserURL)
	{
		try 
		{
			// String url = parserURL+ URLEncoder.encode("Buffalo is a beautiful city in the State of New York.","UTF-8");
			String text = "Buffalo is a beautiful city in the State of New York.";			
			String url = parserURL + URLEncoder.encode("SELECT * FROM geo.placemaker WHERE documentContent=\""+text.replaceAll("\"", "'")+"\" AND documentType=\"text/plain\"","UTF-8");
			
			StdOut.println(url);
			
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			
			// optional default is GET
			con.setRequestMethod("GET");
			con.setConnectTimeout(60000); // time out after one minute
			

			//add request header
			con.setRequestProperty("User-Agent", "Mozilla/5.0");

			int responseCode = -1; 
			BufferedReader in = null;
			try
			{
				responseCode = con.getResponseCode();
				in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			}
			catch (Exception ee) {
				StdOut.println("Service connection testing: cannot connect to "+parserName+" ...");
				return "Erro";
			}
			
			
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			if(responseCode != 200)
			{
				StdOut.println("Service connection testing: cannot connect to "+parserName+" ...");
				StdOut.println("Response Code: " + responseCode);
			
				return "Erro";
			}
			
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		return "OK";
	}
	
	// Return the server-side version information in json to the browser
	private String getServerVersion() 
	{
		JSONArray response_json = new JSONArray();
		response_json.put(EUPEGConfiguration.geoparser_version_dic);
		response_json.put(EUPEGConfiguration.gazetteer_version_dic);
		return response_json.toString();
	}

}
