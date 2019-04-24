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


public class XmlCorpusParser2 
{
	protected  String groundTruthTxtPath ;
	protected  String corpusTextDirect ;
	protected  int articleNumber;
	protected  int validToponymNum;
	protected  int meanToponymsArticle;
	protected  int meanWordsArticle;
	protected  String corpusFinalName;

	public  XmlCorpusParser2(String name) throws Exception 
	{
		articleNumber = 0;
		validToponymNum = 0;
		meanToponymsArticle = 0;
		meanWordsArticle = 0;
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
	
	public int getAverageNum() {
		return this.meanToponymsArticle;
	}
	
	public int getAverageWords() {
		return this.meanWordsArticle;
	}
	
	public String getCorpusName() {
		return this.corpusFinalName;
	}
	
	public void createFolder()
	{
		File myPath = new File( this.corpusTextDirect );
		if(!myPath.exists())
		{
			myPath.mkdir();
		}
	}
	
	// main Function of parsing xml and storing records
	public  boolean xmlParserFunc(InputStream xmlStream) throws Exception {
		//File inputFile = new File("D:\\Program Files\\Java\\Eclipse\\workspace\\xmlTest\\src\\main\\data\\lgl_test.xml");
		SAXReader saxReader = new SAXReader();
		Document document = saxReader.read(xmlStream);
		Element classElement = document.getRootElement();
		walkWholeArticle(classElement);

		meanToponymsArticle = validToponymNum;
		meanWordsArticle = meanWordsArticle/articleNumber;
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
		 System.out.println(size);
		int index = 0;
		Out geoCorpusText = new Out(groundTruthTxtPath);
	    for (int i = 0; i < size; i++) {
	        Node node = root.node(i);
	        if (node instanceof Element) {
	        	System.out.println(((Element)node).getName());
	        	walkSingleRecords((Element)node,index,geoCorpusText);
	        	index++;
	        }
	    }
	    articleNumber = index;
	    geoCorpusText.close();
	}

	// retrieve all toponyms info in one article
	public void walkSingleRecords(Element root,int index,Out fileStream) throws DocumentException 
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
        //writeTextToFile(textDirect,textStr);
        this.meanWordsArticle += countWordsStringTokenizer(textStr);
	    List<Node> nodes = root.elements("toponymIndices");
	    String topos = ((Element)nodes.get(0)).attributeValue("count");
	    this.validToponymNum += Integer.parseInt(topos);
	    /*List<Node> nodes2 = ((Element)nodes.get(0)).elements("location");
	    StringBuilder singlerRecordGroudTruth = new StringBuilder();
	    for(Node toponymNode: nodes2)
	    {
	        String temp = walkToponym((Element)toponymNode);
	        singlerRecordGroudTruth.append(temp);
	 }
	    fileStream.println(singlerRecordGroudTruth);*/
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
		if(toponym.element("name")!=null) {
			String a6 = toponym.element("name").getText();
			String a1 = toponym.element("start").getText();
			String a2 = toponym.element("end").getText();
			//String a3 = toponym.element("extractedName").getText();
			String a4 = toponym.element("lat").getText();
			String a5 = toponym.element("lon").getText();	
			//String a7 = toponym.element("normalisedName").getText();
			
		
		String aa = "";
		
		try 
		{
			if(a6.length()>1) 
			{
			aa = a6;
			aa = addToGroundTruthString(aa,a6);
			aa = addToGroundTruthString(aa,a4);
			aa = addToGroundTruthString(aa,a5);
			aa = addToGroundTruthString(aa,a1);
			aa = addLastToGroundTruthString(aa,a2);
			validToponymNum ++;
			}
		} 
		catch (Exception e) {
			System.out.println(e);
		}
		finally {
			// TODO: handle finally clause
			return aa;
		}
		}
		else 
		{
			return "";
		}

	}
	
	public String getPath()
	{
		String fullPath = this.getClass().getClassLoader().getResource("").getPath();
		String pathArr[] = fullPath.split("/WEB-INF/classes/");
		//System.out.println(fullPath);
		//System.out.println(pathArr[0]);
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
