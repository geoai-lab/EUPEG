// This array fixes the sequence that the metrics show up; otherwise the metrics can show up in any random manner
var idealMetricSequence = 
	["precision",
	"recall",
	"f_score",
	"accuracy",
	"mean",
	"median",
	"accuracy_161",
	"AUC"];

function getRecordsByID()
{
	var searchCode = document.getElementById("searchTextNew").value;
	if(searchCode == ""||searchCode.length!=16)   
	{  
	    alert("Please input the correct 16-digit Experiment ID.");  
	    return false;

	}  
	
	$("#searchExperimentButton").prop('disabled', true);
	
	//send series code to the server and get results
	$.ajax({
        url :  'GetRecordsByIDServlet',
        type : 'GET',
        data :   {index: searchCode},
        dataType : 'text',
        success : function(result, textStatus, jqXHR) 
        {
        	corpusList = [];
        	parserList = [];
        	var resultObject = JSON.parse(result);
        	var html = "<h5>Results: <a href=\"javascript:exportHistoryToCSV('result.csv')\">Download</a></h5>";
        	if(Object.keys(resultObject).length == 0)
        	{
        		html += "<p>No experiment found based on the ID you searched. If you have started an experiment earlier, this means your experiment has not finished yet, so please come back later.</p>";
        	}
        	else
        	{  
        		var metrics = resultObject.getMetricsList;
        		var timestamp = resultObject.getExpTime;
        	
        		delete resultObject.getMetricsList;
        		delete resultObject.getExpTime;
        		
        		html +="<h5>Date and time of experiment: <i style='color:#0275d8;'>"+ timestamp+"</i></h5>";
        		
    			for(key in resultObject)
    			{
    				corpusString = key.split("|")[0];
    				parserString = key.split("|")[1];
    				if($.inArray(corpusString, corpusList)== -1)
    				{
    					corpusList.push(corpusString);
    				}
    				if($.inArray(parserString, parserList)== -1)
    				{
    					parserList.push(parserString);
    				}
    			}
		
				
    			for(var datasetIndex = 0; datasetIndex < corpusList.length; datasetIndex++)
    			{
    				var datasetName = corpusList[datasetIndex];
    				html += "<h5>Performances based on the dataset: <i style='color:#0275d8;'>"+ datasetName +"</i></h5>";
    				
    				html += "<table class='table table-striped border text-center' >";        
    				html += "<thead><tr class='table-primary'><th scope='col'>Geoparser Name</th>";
    				
    				for(var idealMetricIndex=0; idealMetricIndex < idealMetricSequence.length; idealMetricIndex++)
					{
    					for(var metricIndex=0;metricIndex<metrics.length;metricIndex++)
        				{
        					var tempMetric = metrics[metricIndex];
        					if(tempMetric == idealMetricSequence[idealMetricIndex])
        					{
        						html += "<th scope='col' style='text-align:center; vertical-align:middle'>"+metricMapping[tempMetric]+"</th>";
        						break;
        					}
        				}
					}
    				
    				
    				html += "</tr></thead><tbody>";
    				
    				for(var parserIndex=0;parserIndex<parserList.length;parserIndex++)
    				{
    					var thisParserDataResult = resultObject[corpusList[datasetIndex]+"|"+parserList[parserIndex]];
    					
    					var version_parser = thisParserDataResult["parser_version"];
    					var version_gazetteer = thisParserDataResult["gaze_version"];
    					
    					delete thisParserDataResult.version_parser;
    	        		delete thisParserDataResult.version_gazetteer;
    	        		
    					html += "<tr><th scope='row'>"+parserList[parserIndex]+"<span style='font-size:0.7em'><br>parser_version:&nbsp"+
    					version_parser+"<br>gaze_version:&nbsp"+version_gazetteer+"</span></th>";
    				
    					
    					
    					for(var idealMetricIndex=0; idealMetricIndex < idealMetricSequence.length; idealMetricIndex++)
    					{
	    					for(var metricIndex=0;metricIndex<metrics.length;metricIndex++)
	    					{
	    						var tempMetric = metrics[metricIndex];
	        					if(tempMetric == idealMetricSequence[idealMetricIndex])
	        					{
		    						var metricValue = "-";
		    						if(thisParserDataResult[metrics[metricIndex]]>=0)
		    						{
		    							metricValue = thisParserDataResult[metrics[metricIndex]].toFixed(3);
		    						}
		    						html += "<td style='text-align:center; vertical-align:middle'>"+metricValue+"</td>";
		    						break;
	        					}
	    					}
    					}
    					
    					html+= "</tr>";
    				}
    				
    				html += "</tbody></table> <br />";
    				
    			}
        	}
			$('#experimentResultPanel2').html(html);
			$('#experimentResultPanel2').fadeIn();
			
		
			$("#searchExperimentButton").prop('disabled', false);
    			
        },
		error : function(jqXHR, textStatus, errorThrown) 
		{
			alert('ERRORS: ' + ':' + textStatus);
			document.getElementById("experimentResultPanel2").innerHTML = "Our server is under maintenance";
			$("#searchExperimentButton").prop('disabled', false);
		}});
    }


function metricsList()
{
	var judge = false;
	metricsArray = [];
	$('#searchMetrics .custom-control-input').each(function(index, element){
		
	    if($(element).prop('checked'))
	    {
	    	metricsArray.push($(element).attr("name"));
	    	judge = true;
	    }	
	})
	if(judge == false)
	{
		var datasetsFromConfig = configObject["datasets"];
		metricsArray = datasetsFromConfig[0]["support_metrics"];
	}
	
	return metricsArray;

}