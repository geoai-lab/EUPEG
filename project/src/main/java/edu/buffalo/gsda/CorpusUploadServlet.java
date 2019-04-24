package edu.buffalo.gsda;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.JSONObject;



public class CorpusUploadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private String id;
  
    public CorpusUploadServlet() {
        super();
       
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		try {
			processRequest(request, response);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// process the user's uploaded XML format corpus
	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws Exception 
	{
		PrintWriter out = response.getWriter();
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		// if not contain file in the request
		if (!isMultipart) {
			// if not then stop!
			out.println("Error: uploaded XML file does not follow the recommended format.");
			out.flush();
			return;
		} 
		else if (isMultipart) 
		{
			FileItemFactory factory = new DiskFileItemFactory();
			ServletFileUpload upload = new ServletFileUpload(factory);

			try {
				JSONObject resultObject = new JSONObject();
				List<FileItem> fields = upload.parseRequest(request);
				Iterator<FileItem> it = fields.iterator();
				// for each xml object
				while (it.hasNext()) {
					FileItem fileItem = it.next();
					if (!fileItem.isFormField()) {
						
						String orginalName = (fileItem.getName());
						XmlCorpusParser parser = new XmlCorpusParser(fileItem.getName());
						// xml parsing procession
						parser.xmlParserFunc(fileItem.getInputStream());
						
						if(parser.getValidTopoNum() == 0) // if no valid toponym is retrieved
						{
							out.println("Error: uploaded XML file does not follow the recommended format.");
							out.flush();
							return;
						}

						// write parsing results into json response
						resultObject.put("Name", parser.getCorpusName());
						resultObject.put("nickName", orginalName);
						resultObject.put("articleNum", parser.getArticleNum());
						resultObject.put("validToponymNum", parser.getValidTopoNum());
						resultObject.put("averageNum", parser.getAverageNum());

					} else {
						String value = fileItem.getString();
						System.out.println(value);
						this.id = value;
					}
				}
				DatabaseOperator operator = new DatabaseOperator();
				operator.addDataSetMetadata(resultObject.getString("nickName"), resultObject.getString("Name"),
						this.id);
				out.print(resultObject.toString());

			} catch (FileUploadException e) {
				e.printStackTrace();
			}
		}
		out.flush();

	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			processRequest(request, response);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}