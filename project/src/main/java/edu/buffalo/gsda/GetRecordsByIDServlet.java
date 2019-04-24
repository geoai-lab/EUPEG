package edu.buffalo.gsda;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



public class GetRecordsByIDServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	
    public GetRecordsByIDServlet() {
        super();
    }
	
    //process the request of searching for the experiment records by experiment ID 
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String experimentID = request.getParameter("index");
		
		DatabaseOperator operator = new DatabaseOperator();
		
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		out.print(operator.searchRecord(experimentID).toString());
	}

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}


}
