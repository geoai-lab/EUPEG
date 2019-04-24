package edu.buffalo.gsda;

import java.io.File;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.time.LocalDateTime;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;


import edu.princeton.cs.algs4.Out;



public class XmlCorpusParser 
{
	protected  String groundTruthTxtPath ;
	protected  String corpusTextDirect ;
	protected  int articleNumber;
	protected  int validToponymNum;
	protected  double meanToponymsArticle;
	// protected  double meanWordsArticle;
	protected  String corpusFinalName;

	public  XmlCorpusParser(String name) throws Exception 
	{
		articleNumber = 0;
		validToponymNum = 0;
		meanToponymsArticle = 0.0;
		corpusFinalName = getFileNameByTime() + "_" + name;
		groundTruthTxtPath = getPath() + File.separator + "gold" + File.separator + corpusFinalName + "_gold.txt";
		corpusTextDirect = getPath() + File.separator + "corpora" + File.separator + corpusFinalName;
		createFolder();
	}
	
	public int getArticleNum() {
		return this.articleNumber;
	}
	
	public int getValidTopoNum() {
		return this.validToponymNum;
	}
	
	public double getAverageNum() {
		return this.meanToponymsArticle;
	}
	
	public String getCorpusName() {
		return this.corpusFinalName;
	}
	
	//create the folder to store text files
	public void createFolder()
	{
		File myPath = new File( this.corpusTextDirect );
		if(!myPath.exists())
		{
			myPath.mkdir();
		}
	}
	
	
	// main Function of parsing XML and storing records
	public  boolean xmlParserFunc(InputStream xmlStream) throws Exception {
		SAXReader saxReader = new SAXReader();
		Document document = saxReader.read(xmlStream);
		Element classElement = document.getRootElement();
		walkWholeArticle(classElement);

		meanToponymsArticle = ((double)validToponymNum)/((double)articleNumber);
		meanToponymsArticle = (double)(Math.round(meanToponymsArticle*10))/10;
		return true;
	}
	
	// write texts of each article 
	public  void writeTextToFile(String path, String textString)
	{
		Out geoCorpusText = new Out(path);
		geoCorpusText.println(textString);
		geoCorpusText.close();
	}
	
	// write groundtruth of each article
	public  void writeGroundTruthToFile(String GroundTruthString, Out fileOut) 
	{
		fileOut.println(GroundTruthString);
	}
	
	// iterate all article nodes in xml 
	public  void walkWholeArticle(Element root) throws DocumentException
	{
		//System.out.println(root.getName());
		int size = root.nodeCount();
		//System.out.println(size);
		int index = 0;
		Out geoCorpusText = new Out(groundTruthTxtPath);
	    for (int i = 0; i < size; i++) {
	        Node node = root.node(i);
	        if (node instanceof Element) {
	        	//System.out.println(((Element)node).getName());
	        	walkSingleRecords((Element)node,index,geoCorpusText);
	        	index++;
	        }
	    }
	    articleNumber = index;
	    geoCorpusText.close();
	}

	// retrieve all toponyms info in one article
	public  void walkSingleRecords(Element root,int index,Out fileStream) throws DocumentException 
	{
		
		String  textStr = " ";
	    //iterate through child elements of root with element name "text"
	    for (Iterator<Element> textIt = root.elementIterator("text"); textIt.hasNext();) {
	        Element ptext = textIt.next();
	        textStr = (ptext).getText();
	        // do something
	        //System.out.println((ptext).getText());
	    }
        String textDirect = corpusTextDirect;
        textDirect = textDirect + File.separator + String.valueOf(index);
        writeTextToFile(textDirect,textStr);
        // this.meanWordsArticle += countWordsStringTokenizer(textStr);
	    List<Node> nodes = root.elements("toponyms");
	    List<Node> nodes2 = ((Element)nodes.get(0)).elements("toponym");
	    StringBuilder singlerRecordGroudTruth = new StringBuilder();
	    for(Node toponymNode: nodes2)
	    {
	        String temp = walkToponym((Element)toponymNode);
	        singlerRecordGroudTruth.append(temp);
	 }
	    fileStream.println(singlerRecordGroudTruth);
	}
	
	public  String addToGroundTruthString(String str1,String str2) {
		str1 = str1 + ",," + str2;
		return str1;
	}
	
	public  String addLastToGroundTruthString(String str1,String str2) {
		str1 = str1 + ",," + str2 + "||" ;
		return str1;
	}
	
	// retrieve name, location, text_length of single toponym 
	@SuppressWarnings("finally")
	public  String walkToponym(Element toponym) 
	{
		String a1 = toponym.element("start").getText();
		String a2 = toponym.element("end").getText();
		String a3 = toponym.element("phrase").getText();
		
		
		Element gaztag = toponym.element("place");
		String result_record = "";
		String latlon ="";
		try {
			Iterator<Element> gaztagIt = gaztag.elementIterator();
			boolean ifOfficialName = false;
			for (; gaztagIt.hasNext();) {
		        Element element = gaztagIt.next();
		        if(element.getName().equals("placename"))
		        {
		        	result_record = addToGroundTruthString(element.getText(),a3);  
		        	ifOfficialName = true;
		        }
		        else if (element.getName().equals("footprint")) {
		        	String coordinates = element.getText().strip();
		        	String lat = coordinates.split("\\s+")[1] ;
		        	String lon = coordinates.split("\\s+")[0];
		        	latlon = lat+",,"+lon;
				}
		    }
			
			// if official name does not exist
			if(ifOfficialName == false)
				result_record = addToGroundTruthString("null",a3);  

			result_record = addToGroundTruthString(result_record,latlon);
			result_record = addToGroundTruthString(result_record,a1);
			result_record = addLastToGroundTruthString(result_record,a2);
			validToponymNum ++;
		} 
		catch (Exception e) {
			System.out.println(e);
		}
		finally {
			// TODO: handle finally clause
			return result_record;
		}

	}
	
	
	public String getPath()
	{
		String fullPath = this.getClass().getClassLoader().getResource("").getPath();
		String pathArr[] = fullPath.split("/WEB-INF/classes/");
		fullPath = pathArr[0].replaceAll("%20", " ");	
		return fullPath;
	}
	
	//name uploaded corpus by uploaded time
	public String getFileNameByTime() {
		 LocalDateTime currentTime = LocalDateTime.now(); 	
		 String temp = currentTime.toLocalDate().toString()+"_" + String.valueOf(currentTime.getHour())+ String.valueOf(currentTime.getMinute());
		 return temp;
	}

	// count how many words in one article
	public  int countWordsStringTokenizer(String sentence)
	{ 
		if (sentence == null || sentence.isEmpty()) 
		{ 
			return 0; 
		} 
		StringTokenizer tokens = new StringTokenizer(sentence); 
		return tokens.countTokens(); 
	}

}