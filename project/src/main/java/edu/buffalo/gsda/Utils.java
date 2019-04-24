package edu.buffalo.gsda;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import org.json.JSONObject;

import edu.princeton.cs.algs4.StdOut;


public class Utils 
{
	private double precision;
	private double recall;
	private double f_score;
	private double accuracy;
	private double mean;
	private double AUC;
	private double median;
	private double accuracy_161;
	
	public HashMap<String, Double> getAllMetrics() 
	{
		HashMap<String, Double> metricsMaps = new HashMap<String, Double> ();
		metricsMaps.put("precision",this.precision);
		metricsMaps.put("recall",this.recall);
		metricsMaps.put("f_score",this.f_score);
		metricsMaps.put("accuracy",this.accuracy);
		metricsMaps.put("mean",this.mean);
		metricsMaps.put("median",this.median);
		metricsMaps.put("AUC",this.AUC);
		metricsMaps.put("accuracy_161",this.accuracy_161);
		return metricsMaps;
	}
	
	
	
	public JSONObject calculateScores(String predicted, String gold, boolean inspect, ArrayList<String> selectedMetrics) throws Exception, IOException 
	{
	    /*"""
	    Given the predictions and the gold annotations, calculate precision, recall, F Score and accuracy.
	    :param inspect: If True, the differences between gold and predicted files will be printed
	    :param predicted: path to the file with parser predictions
	    :param gold: path to the file with gold annotations
	    :return: a list of errors per toponym i.e how far away is each correctly identified toponym from
	    the gold location. This is used to measure the accuracy of the geocoding part
	    """*/	
	
		double tp = 0.0, fp = 0.0, fn = 0.0;
	    Hashtable<Integer, Double> errorDistance = new Hashtable<>();
	    
	    // test if the dataset is like WikToR which does not support precision, recall, and f-score 
	    int wiki = 0;
	    if(predicted.contains("WikToR") || predicted.contains("Hu2014") || predicted.contains("Ju2016")) 
	    	wiki = 1;
	   
	    // read geoparsing output and the ground truth
	    File fileDir = new File(predicted);
		BufferedReader predictions_file = new BufferedReader(new InputStreamReader( new FileInputStream(fileDir), "UTF8"));
		File fileDir2 = new File(gold);
		BufferedReader gold_file = new BufferedReader(new InputStreamReader( new FileInputStream(fileDir2), "UTF8"));
		

	    // load geoparsing output and the ground truth to two vectors
	    Vector<String> predictedVector = new Vector<>();
	    Vector<String> goldVector = new Vector<>();
	    
	    String inputString = null;
	    while((inputString = gold_file.readLine()) != null)
	    {
	    	goldVector.add(inputString);
	    }
	    gold_file.close();
	    
	    while((inputString = predictions_file.readLine()) != null)
	    {
	    	predictedVector.add(inputString);
	    }
	    predictions_file.close();
	    
	    
	    // test if the ground truth and geoparsing output have the same number of records
	    if(goldVector.size() != predictedVector.size())
	    {
	    	StdOut.println(goldVector.size());
	    	StdOut.println(predictedVector.size());
	    	StdOut.println("Gold and predicted have different numbers of records. Exit ...");
	    	return null;
	    }
	    
	    // toponym index for storing error distance
	    int toponym_index = -1;
	    
	    // start to iterate through the ground truth for comparison
	    for(int recordIndex=0;recordIndex<goldVector.size();recordIndex++)
	    {
	    	// since each record can have multiple toponyms, we further put each toponym into a vector
	    	String[] gold_tops_array = goldVector.get(recordIndex).split("\\|\\|");
	    	Vector<String> gold_tops = new Vector<>();
	    	for(int i=0;i<gold_tops_array.length;i++)
	    	{
	    		if(gold_tops_array[i].length()>1)
	    		{
	    			// Here we need to further test if a ground truth record has coordinates
	    			// some ground truth records do not have coordinates and thus are not included
	    			String[] gold_top_items = gold_tops_array[i].split(",,");
	    			if(gold_top_items[2].length()>0 && gold_top_items[3].length()>0 && !gold_top_items[2].equalsIgnoreCase("null") && !gold_top_items[3].equalsIgnoreCase("null"))
	    				gold_tops.add(gold_tops_array[i]);
	    		}
	    	}
	    	
	    	String[] predicted_tops_array = predictedVector.get(recordIndex).split("\\|\\|");
	    	Vector<String> predicted_tops = new Vector<>();
	    	for(int i=0;i<predicted_tops_array.length;i++)
	    	{
	    		if(predicted_tops_array[i].length()>1)
	    		{
	    			// we do the same to the predicted
	    			String[] predicted_top_items = predicted_tops_array[i].split(",,");
	    			if(predicted_top_items[2].length()>0 && predicted_top_items[3].length()>0 && !predicted_top_items[2].equalsIgnoreCase("null") && !predicted_top_items[3].equalsIgnoreCase("null"))
	    				predicted_tops.add(predicted_tops_array[i]);
	    		}	
	    	}
	    	
	    	
	    	// start to iterate each toponym
	    	for(String gold_top : gold_tops)
	    	{
	    		String[] gold_top_items = gold_top.split(",,");
	    		
	    		boolean match = false;  // A flag to establish whether this is a matching prediction
		        
	    		for(String predicted_top : predicted_tops)
	    		{
	    			String[] predicted_top_items = predicted_top.split(",,");
	    			double mean_g = (Integer.valueOf(gold_top_items[4]) + Integer.valueOf(gold_top_items[5])) / 2.0;
	    	        double mean_p = (Integer.valueOf(predicted_top_items[4]) + Integer.valueOf(predicted_top_items[5])) / 2.0;
	    	        
	    	        //  If the toponym position (its mean) is no more than 9 characters from gold AND the two
	                //  strings are equal then it's a match. For reasons to do with UTF-8 encoding and decoding,
	                //  the toponym indices may, in a few instances, be off by a few positions when using Web APIs.
	    	        if (Math.abs(mean_g - mean_p) < 10) //&& (predicted_top_items[1].toLowerCase().equals(gold_top_items[1].toLowerCase())))
    	        	{
    	        		match = true;   // Change the number above to 0 for EXACT matches, 10 for INEXACT matches
					}
	    	        
	    	        if(match)
	    	        {
	    	        	tp += 1.0;	
	    	        	
	    	        	// remove this matched record from predicted
	    	            predicted_tops.remove(predicted_top);	    	       

	    	            // calculate error distance
    	            	if((predicted_top_items[2].length()>0) || (predicted_top_items[3].length()>0))
    	            	{	
    	            		double[] predicted_coord = {Double.valueOf(predicted_top_items[2]), Double.valueOf(predicted_top_items[3])};
    	            		double[] gold_coord = {Double.valueOf(gold_top_items[2]), Double.valueOf(gold_top_items[3])};
    	            		
    	            		toponym_index += 1;
    	            		errorDistance.put(toponym_index,  HaversineAlgorithm.distanceInKm(predicted_coord[0], predicted_coord[1], gold_coord[0], gold_coord[1]));
    	            	}
    	            	
	    	            break;
	    	        }
	    		}
	    		
	    		if(!match) 
	    			fn += 1.0;
	    	}
	    	
	    	// if this is not a WikToR-like dataset, we count false positive
	    	if(wiki != 1)
	          fp += predicted_tops.size();

	        // if inspect, we print out the mismatched records
	        if(inspect)
	            if(predicted_tops.size() > 0 || gold_tops.size() > 0)
	            {
	            	String misMatched_predicted = "";
	            	for(String thisMisMatched: predicted_tops)
	            		misMatched_predicted += " " +thisMisMatched;
	            	StdOut.println(misMatched_predicted);
	            	
	            	String misMatched_gold = "";
	            	for(String thisMisMatched: gold_tops)
	            		misMatched_gold += " " +thisMisMatched;
	            	StdOut.println(misMatched_gold);
	            }
	    }
	    // finish calculating the tp, fp, fn
	    
	        
	    JSONObject resultObject = new JSONObject();
	    double MAX_ERROR = 20039;  // Furthest distance between two points on Earth, i.e the circumference / 2

	    
	    // set every metric as -1 initially; these values will be written in the database
    	this.precision = -1;
    	this.recall = -1;
    	this.f_score = -1;
    	this.accuracy = -1;
    	this.mean = -1;
    	this.median = -1;
    	this.accuracy_161 = -1;
    	this.AUC = -1;
    	
	    if(wiki == 1) // if this is a WikToR-like dataset, we do not apply precision, recall, and f-score
	    {
	    	double accuracyValue = tp/(tp + fn);

	    	if(selectedMetrics.contains("accuracy"))
	    	{
	    		resultObject.put("accuracy", accuracyValue);
	    		this.accuracy = accuracyValue;
	    	}
	    }
	    else // if this is a normal dataset
	    {
	    	double p_precision = tp / (tp + fp);
	    	double p_recall = tp / (tp + fn);
	    	double pf_score = 0;
	    	if(tp > 0) pf_score = 2 * p_precision * p_recall / (p_recall + p_precision); // this is to avoid the situation when both precision and recall are 0
	    	
	    	double accuracyValue = tp/ (tp + fn);
	    	
	    	if(selectedMetrics.contains("precision"))
	    	{
	    		resultObject.put("precision", p_precision);
	    		this.precision = p_precision;
	    	}
	    	if(selectedMetrics.contains("recall"))
	    	{
	    		resultObject.put("recall", p_recall);
	    		this.recall = p_recall;
	    	}
	    	if(selectedMetrics.contains("f_score"))
	    	{
	    		resultObject.put("f_score", pf_score);
	    		this.f_score = pf_score;
	    	}
	    	if(selectedMetrics.contains("accuracy"))
	    	{
	    		resultObject.put("accuracy", accuracyValue);
	    		this.accuracy = accuracyValue;
	    	}
	    }
	    
	    
	    // Here we begin to calculate distance based metrics
	    ArrayList<Map.Entry<?, Double>> sorted =  sortValue(errorDistance);
	    int size = sorted.size();
	    resultObject.put("size", size);	
	    
	    // median error distance
	    double medianErrorDistance = 0.0;
	    if(size ==0)  // special case: a geoparser may not return any matched toponym
	    	medianErrorDistance = 0.0;
	    else if((size%2)==0) {
	    	medianErrorDistance = (sorted.get((size/2-1)).getValue() + sorted.get(size/2).getValue()) / 2.0;
	    	//System.out.println("if");
	    	//System.out.println(medianAccuracy);
	    }
	    else {
	    	medianErrorDistance = sorted.get(size/2).getValue();
	    	//System.out.println("else");
	    	//System.out.println(medianAccuracy);
	    }
    	if(selectedMetrics.contains("median"))
    	{
    		this.median = medianErrorDistance;
    	    resultObject.put("median", medianErrorDistance);	  
    	}
	    
	    // mean error distance
	    double meanErrorDistance = 0.0;
	    if(size > 0)
	    {
	    	for(int i=0;i<size;i++)
		    {
		    	meanErrorDistance += sorted.get(i).getValue();
		    	//StdOut.println(sorted.get(i).getValue());
		    }
		    meanErrorDistance = meanErrorDistance / size;
	    }
    	if(selectedMetrics.contains("mean"))
    	{
    		this.mean = meanErrorDistance;
    	    resultObject.put("mean", meanErrorDistance);	  
    	}
	    
    	
	    // 161 accuracy@k
	    double accuracyAtKCount = 0.0;
	    if(size > 0)
	    {
	    	 for(int i=0;i<size;i++)
	 	    {
	 	    	if(sorted.get(i).getValue() < 161)
	 	    		accuracyAtKCount += 1.0;
	 	    }
	 	    accuracyAtKCount = accuracyAtKCount/size;
	    }

	    if(selectedMetrics.contains("accuracy_161"))
    	{
		    this.accuracy_161 = accuracyAtKCount;
		    resultObject.put("accuracy_161", accuracyAtKCount);
    	}

	    
	    // calcualte AUC using trapezoidal rule
	    double AUC = 0.0; 
	    if(size > 1)
	    {
		    double h = 1;             // step size
		    double sum = 0.5 * (Math.log(1+sorted.get(0).getValue())/Math.log(MAX_ERROR) + Math.log(1+sorted.get(size-1).getValue())/Math.log(MAX_ERROR));    // area
	        for (int i = 1; i < size-1; i++) 
	        {
		       sum = sum + Math.log(1+sorted.get(i).getValue())/Math.log(MAX_ERROR);
	        }
	        AUC =  sum * h / (size - 1);
	    }
	    
	    if(selectedMetrics.contains("AUC"))
    	{
	    	this.AUC = AUC;
		    resultObject.put("AUC", AUC);
    	}
	    

	    return resultObject;
 
	}
	
	
	
	public ArrayList<Map.Entry<?, Double>> sortValue(Hashtable<?, Double> t)
	{
       //Transfer as List and sort it
       ArrayList<Map.Entry<?, Double>> l = new ArrayList<>(t.entrySet());
       Collections.sort(l, new Comparator<Map.Entry<?, Double>>(){

         public int compare(Map.Entry<?, Double> o1, Map.Entry<?, Double> o2) {
            return o1.getValue().compareTo(o2.getValue());
        }});

       return l;
	}
	

	
	// get the path of the current servlet
	public String getPath()
	{
		String fullPath = this.getClass().getClassLoader().getResource("").getPath();
		String pathArr[] = fullPath.split("/WEB-INF/classes/");
		fullPath = pathArr[0].replaceAll("%20", " ");
		
		return fullPath;
	}

}
