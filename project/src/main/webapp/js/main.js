var seriesCode = "null";
var if_new_test = true;


// This is the key function for running an experiment
function runAnExperiment()
{
	if(if_new_test == false)
	{
		if_new_test = true;
		seriesCode = randomHash();
	}

	// get the datasets specified by the user
	var datasetString = "";  // for data id
	var datasetString2 = "";  // for data name
	$('#corpusList .custom-control-input').each(function(index, element){
	
	    if($(element).prop('checked'))
	    {
	    	datasetString += $(element).attr("id")+"|";
	    	datasetString2 += $(element).attr("name")+"|";
	    }	
	})

	// if no dataset is selected, remind the user
	if(datasetString.length == 0)
	{
		alert("You must select at least one dataset to run your experiment.");
		return;
	}
	datasetString = datasetString.substring(0, datasetString.length-1);
	datasetString2 = datasetString2.substring(0, datasetString2.length-1);
	
	
	// get the geoparser specified by the user
	var geoparserString = "";   // geoparser id for retrieving data
	var geoparserString2 = "";  // for displaying geoparser names
	$('#geoparserList .custom-control-input').each(function(index, element){
		
	    if($(element).prop('checked'))
	    {
	    	var parserID = $(element).attr("id");
	    	geoparserString2 += parserID+"|";
	    	if(parserID in parserDic)      // parserDic is a dictionary that stores the temporary geoparser names 
    		{
    			parserID = parserDic[parserID];
    		}
	    	geoparserString += parserID+"|";
	    	
	    }	
	})
	// if no geoparser is selected, remind the user
	if(geoparserString.length == 0)
	{
		alert("You must select at least one geoparser to run an experiment.");
		return;
	}
	geoparserString = geoparserString.substring(0, geoparserString.length-1);
	geoparserString2 = geoparserString2.substring(0, geoparserString2.length-1);
	
	
	// check if the user has selected at least one metric
	var metricSelected = false;
	$('#runMetrics .custom-control-input').each(function(index, element)
	{
	    if($(element).prop('checked'))
	    {
	    	metricSelected = true;
	    }	
	})
	if(!metricSelected)
	{
		alert("You must select at least one metric to run an experiment.");
		return;
	}
	
	
	// start to show messages
	showWaitingInfo(seriesCode);
	resultPannelClear();
	
	var metrics = metricsListFunc();
	
	var metricsInput = metrics.toString();
	//var metrics = ["precision","recall","f_score","median","mean","accuracy161","AUC"];
	
	// prevent the same user from clicking the button multiple times if an experiment is submitted
	$("#runExpButton").prop('disabled', true);
	$("#runExpButton").html("Experiment submitted")
	
	$.ajax({
		url: "EvaluationServlet",
		
		data: {datasets: datasetString, parsers: geoparserString, experimentID: seriesCode, metricsList: metricsInput},
		success: function(result){
			
			var resultObject = JSON.parse(result);
			var datasets = datasetString.split("|");  // this is for dataset id
			var datasetNames = datasetString2.split("|"); // this is for dataset name
			
			
			var geoparsers = geoparserString.split("|");
			var geoparserNames = geoparserString2.split("|"); // this is for geoparser name
			
			var html = "<h5>Results: <a href=\"javascript:exportTableToCSV('result.csv')\">Download</a></h5>";
			for(var datasetIndex = 0; datasetIndex < datasets.length; datasetIndex++)
			{
				var datasetName = datasetNames[datasetIndex];
				html += "<h5>Performances based on the dataset: <i style='color:#0275d8;'>"+ datasetName +"</i></h5>";
				
				html += "<table class='table table-striped border text-center'>";        
				html += "<thead><tr class='table-primary'><th scope='col'>Geoparser Name</th>";
				
		
				
				for(var metricIndex=0;metricIndex<metrics.length;metricIndex++)
				{
					var tempMetric = metrics[metricIndex];
					html += "<th scope='col'style='text-align:center; vertical-align:middle'>"+metricMapping[tempMetric]+"</th>";
				}
				
				html += "</tr></thead><tbody>";
				
				for(var parserIndex=0;parserIndex<geoparsers.length;parserIndex++)
				{
					html += "<tr><th scope='row'>"+geoparserNames[parserIndex]+"<span style='font-size:0.7em'><br>parser_version:&nbsp"+
					geoparserVersion[geoparserNames[parserIndex]]+"<br>gaze_version:&nbsp"+gazetteerVersion[geoparserNames[parserIndex]]+"</span></th>";
				    
					var thisParserDataResult = resultObject[datasets[datasetIndex]+"_"+geoparsers[parserIndex]];

					for(var metricIndex=0;metricIndex<metrics.length;metricIndex++)
					{
						var metricValue = "-";
						if(thisParserDataResult.hasOwnProperty(metrics[metricIndex]))
						{
							metricValue = thisParserDataResult[metrics[metricIndex]].toFixed(3);
						}
						html += "<td style='text-align:center; vertical-align:middle'>"+metricValue+"</td>";
					}
					html+= "</tr>";
				}
				
				html += "</tbody></table> <br />";
				
			}
			showFinishInfo(seriesCode);
			$('#experimentResultPanel').html(html);
			$('#experimentResultPanel').fadeIn();
			if_new_test = false;
			
			$("#runExpButton").prop('disabled', false);
			$("#runExpButton").html("Run this experiment")
		},
		
		error : function(jqXHR, textStatus, errorThrown) {
		
			$("#runExpButton").prop('disabled', false);
			$("#runExpButton").html("Run this experiment")
		}
	});
	
}


// create the random series code as experiment ID
function randomHash() 
{
    var str = "",
        range = 16,
        arr = ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'];

    for (var i = 0; i < range; i++) {
        pos = Math.round(Math.random() * (arr.length - 1));
        str += arr[pos];
    }
    return str;
}

// add selected metrics into request body
function metricsListFunc()
{
	var judge = false;
	metricsArray = [];
	$('#runMetrics .custom-control-input').each(function(index, element){
		
	    if($(element).prop('checked'))
	    {
	    	metricsArray.push($(element).attr("id"));
	    	judge = true;
	    }	
	})
	
	if(judge == false)
	{
		var datasetsFromConfig = configObject["datasets"];
		metricsArray = datasetsFromConfig[1]["support_metrics"];
	}
	
	return metricsArray;

}


// showing the notification when the experiment is running
function showWaitingInfo(seriesCode)
{
	var html =  "<h5>Experiment ID: <i style='color:red;'>" +seriesCode+ "</i></h5>";
	html +="<p>Your experiment is running now. Depending on the datasets and geoparsers you have chosen, the experiment can take from hours to days. " +
			"Please save your experiment ID and come back later to search for results.</p>";
	$('#experimentMetadataPanel').html(html);
	$('#experimentMetadataPanel').fadeIn();
}

// showing the notification when the experiment is finished
function showFinishInfo(seriesCode)
{
	var html =  "<h5>Experiment ID: <i style='color:red;'>"+seriesCode+"</i></h5>";
	html +="<p>Please remember this ID. You will need to input this ID when searching for this experiment. </p>";
	$('#experimentMetadataPanel').html(html);
	$('#experimentMetadataPanel').fadeIn();
}

// clear the exsisting result
function resultPannelClear()
{
	var html = "";
	$('#experimentResultPanel').html(html);
	$('#experimentResultPanel').fadeIn();
}


// Test if Yahoo!PlaceSpotter/GeoTxt is connected
function testingConnectedService()
{
	$.ajax({
        url :  'ServiceConnectionTestServlet',
        type : 'GET',
        data :   {parserName: "Yahoo", parserURL: "https://query.yahooapis.com/v1/public/yql?format=json&q="},
        dataType : 'text',
        success : function(result, textStatus, jqXHR) 
        {
        	if(result == "Erro")
        	{
        		$('#Yahoo').prop('disabled', true);
        		$('#errorMessage').html('The online service of Yahoo!PlaceSpotter is currently not available.');
        		$('#errorMessageContainer').fadeIn();
        	}

        },
		error : function(jqXHR, textStatus, errorThrown) 
		{
			$('#Yahoo').prop('disabled', true);
    		$('#errorMessage').html('The online service of Yahoo!PlaceSpotter is currently not available.');
    		$('#errorMessageContainer').fadeIn();
			
		}}); 
}

// send request to server to update the version of geoparsers and gazetter 
function updateVersionInfo()
{
	$.ajax({
        url :  'ServiceConnectionTestServlet',
        type : 'GET',
        data :   {parserName: "Version", parserURL: "default"},
        dataType : 'text',
        success : function(result, textStatus, jqXHR) 
        {
        	if(result == null)
        		alert("Unable to update the version information because of server maintaince!");
        	
        	else
        		{
        			geoparserVersion = JSON.parse(result)[0];
        			gazetteerVersion = JSON.parse(result)[1];
        		}
        		
        },
		error : function(jqXHR, textStatus, errorThrown) 
		{
			alert("Unable to update the version information because of server maintaince!");
			
		}});
}

/* convert JS date into official version format
 * ptype -- 0: date format as YYYY-mm
 * 		 -- 1: date format as MMM. YYYY
 */
Date.prototype.toShortFormat = function(ptype) {
    var month_names1 =["01","02","03",
                      "04","05","06",
                      "07","08","09",
                      "10","11","12"];
    
    var month_names2 =["Jan.","Feb.","Mar.",
        				"Apr.","May","June",
        				"July","Aug.","Sept.",
        				"Oct.","Nov.","Dec."];
    
    var month_index = this.getMonth();
    var year = this.getFullYear();
    
    if(ptype == 0)
    	return year+"-"+month_names1[month_index];
    else
    	return month_names2[month_index]+" "+year;
}


// This initialization function loads datasets and geoparsers
$(function()
  {
	// populate datasets
	var datasets = configObject["datasets"];
	var html = "";
	for(var i=0; i<datasets.length; i++)
	{
		html += '<div class="custom-control custom-checkbox custom-control-inline mt-2 mr-4"  > '+
					  "<input type=\"checkbox\" class=\"custom-control-input\" id=\""+datasets[i]["dataset_name"] +"\"  name=\""+datasets[i]["dataset_name"] +"\" > " +
					  "<label class=\"custom-control-label\" for=\""+datasets[i]["dataset_name"]+"\">"+datasets[i]["dataset_name"]+"</label>"+
				"</div>";
	}
				
	$('#corpusList').html(html);
	
	
	// populate geoparsers
	var geoparsers = configObject["geoparsers"];
	html = ""
	for(var i=0; i< geoparsers.length; i++)
	{
		html += "<div class=\"custom-control custom-checkbox custom-control-inline mt-2  mr-4 \"> "+
					  "<input type=\"checkbox\" class=\"custom-control-input\" id=\""+geoparsers[i]["geoparser_id"] +"\" > " +
					  "<label class=\"custom-control-label\" for=\""+geoparsers[i]["geoparser_id"]+"\">"+geoparsers[i]["geoparser_name"]+"</label>"+
				"</div>";
	}

	$('#geoparserList').html(html);
	
	
	
	seriesCode = randomHash();
	if_new_test = true;
	
	
	// Examine whether Yahoo/GeoTxt works 
	setTimeout(testingConnectedService,500);
	// Update the versioning information from the server
	setTimeout(updateVersionInfo,1000);
  });

