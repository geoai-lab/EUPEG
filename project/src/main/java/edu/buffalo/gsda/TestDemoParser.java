package edu.buffalo.gsda;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.bericotech.clavin.ClavinException;
import com.bericotech.clavin.GeoParser;
import com.bericotech.clavin.GeoParserFactory;
import com.bericotech.clavin.resolver.ResolvedLocation;


public class TestDemoParser extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private GeoParser parser;
	  
	//this is a demo geoparser API based on EUPEG geoparser uniformed format 
	public TestDemoParser() 
    {
        super();
       
        try 
        {
        	//use CLAVIN to implement the geoparsing function
			parser = GeoParserFactory.getDefault(EUPEGConfiguration.CLAVIN_Path);
		} 
        catch (ClavinException e) 
        {
			e.printStackTrace();
		}
    }
	
	//demo geoparser web service process the request and return the response
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		try 
		{
			String textToParse = request.getParameter("text");
			response.setContentType("text/html;charset=UTF-8");
			PrintWriter out = response.getWriter();
			
			// if no text is provided
			if(textToParse == null || textToParse.length() == 0)
			{
				out.print("{\"message\":\"No text is provided for geoparsing.\"}");
			}
			else
			{
				List<ResolvedLocation> resolvedLocations = parser.parse(textToParse);
				JSONArray toponymsArray = new JSONArray();
				for (int i =0; i<resolvedLocations.size(); i++) 
				{
					ResolvedLocation tempLocation = resolvedLocations.get(i);
					String standardName = tempLocation.getGeoname().getName();
					String phrase =  tempLocation.getLocation().getText();
					double latitude =  tempLocation.getGeoname().getLatitude();
					double longitude = tempLocation.getGeoname().getLongitude();		
					int startPosition = tempLocation.getLocation().getPosition();
					int endPosition = tempLocation.getLocation().getPosition() + tempLocation.getLocation().getText().length();
					
					JSONObject toponymObject = new JSONObject();
					toponymObject.put("start", startPosition);
					toponymObject.put("end", endPosition);
					toponymObject.put("phrase", phrase);
					
					JSONObject placeObject = new JSONObject();
					placeObject.put("placename", standardName);
					JSONArray footprintArray = new JSONArray();
					JSONArray coordinateArray = new JSONArray();
					coordinateArray.put(0, longitude);
					coordinateArray.put(1, latitude);
					footprintArray.put(coordinateArray);
					placeObject.put("footprint", footprintArray);
					toponymObject.put("place", placeObject);
					
					toponymsArray.put(toponymObject);
										
				}
				
				JSONObject resultObject = new JSONObject();
				resultObject.put("toponyms", toponymsArray);
				out.println(resultObject.toString());
			}
			
			return;
			
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		doGet(request, response);
	}

}
