var parserDic={};


// a function for checking if a geoparser with the same name exits
function ifGeoparserExist(geoparserName)
{
	 var exists = false;
	 $('#geoparserList .custom-control-input').each(function(index, element){
	    	thisGeoparserName = $(element).attr("id");
	    	if(thisGeoparserName.toUpperCase() === geoparserName.toUpperCase())
	    		exists = true;
		});
	 
	 if(exists)
		 return true;
		
}



function testService() {
	// disable the button first to prevent a user from clicking multiple times
	$("#geoparserConnectButton").prop('disabled', true);
	
	if(if_new_test == false)
	{
		seriesCode = randomHash();
		if_new_test = true;
	}
	var name = document.getElementById("parserName").value;
	var serviceURL = document.getElementById("parserURL").value;
	if(serviceURL == ""||name == "")   
	{  
	    alert("Geoparser name or URL is missing.");  
	    $("#geoparserConnectButton").prop('disabled', false);
	    return false;

	}  
	
	if(ifGeoparserExist(name))
    {
 	   alert("The geoparser name you specified already exists. Please choose a different name.");
 	   $("#geoparserConnectButton").prop('disabled', false);
 	   return;
    }
	
	// test if url valid in regular expression syntax
	//  url_gold in EUPEG: 	"http://geotxt.org/v2/api/geotxt.json?m=stanfords&q="
	var strRegex ='(http?|https?)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]';  
	var re=new RegExp(strRegex);   
	if (!re.test(serviceURL)) {   
	    alert("Please input your URL in a correct format");  
	    $("#geoparserConnectButton").prop('disabled', false);
	    return false;   
	 } 
	$("#testArticle").css('display','block'); 
	//test if valid in connection & geoparsing
	$.ajax({
        url :  'ParserUploadServlet',
        type : 'GET',
        data :   {parserName: name,parserURL: serviceURL,experimentID: seriesCode},
        dataType : 'text',
        success : function(result, textStatus, jqXHR) 
        {
        	if(result == "Erro")
        	{
        		document.getElementById("testArticle").innerHTML = "The geoparser URL is either invalid or not following the recommended format.";
        	}
        	else
        	{
        		var resultObject = JSON.parse(result);
        		var text = resultObject["text"];
        		var toponyms = JSON.parse(resultObject["topoList"]);
        		parserDic[name] = resultObject["parserName"];
        	
        		var base_index = 0;
        		var array_start = [];
        		var array_end = [];
        		for(var i =0;i<toponyms.length; i++)
        		{
        			array_start.push( Number(toponyms[i]["start"]));
        			array_end.push( Number(toponyms[i]["end"]));
        		}
        		array_start.sort(function(a, b){return a - b});
        		array_end.sort(function(a, b){return a - b});
        		var testBoxHTML = "<h5>Your geoparser is connected to EUPEG successfully. You can close this window and use your geoparser now. </h5> <br/>Below is a test result:<br/>";
        		for(var i =0; i<toponyms.length; i++)
        		{
        			var index1 = array_start[i] + base_index;
        			var index2 = array_end[i] + base_index;
        			
        			var str_pre = text.substring(0,index1);
        			var str_mid = text.substring(index1,index2);
        			var str_aft = text.substring(index2);
        			text = str_pre+"<b>"+str_mid+"</b>"+str_aft;
        			base_index = base_index + 7;
        		
        		}
        		$('#geoParserModal').modal('handleUpdate');
        		testBoxHTML = testBoxHTML + text + "<p>You can close this window to continue.</p>";
        		document.getElementById("testArticle").innerHTML = testBoxHTML;
        		
        		// update the new geoparser' information on the html page 
        		var html = document.getElementById("geoparserList").innerHTML;
        		html += "<div class='custom-control custom-checkbox custom-control-inline mt-2  mr-4'>";
        		html += "<input type='checkbox' class='custom-control-input' id=" + name + ">";
        		html += "<label class='custom-control-label' for=" + name + ">"+ name + "</label></div> ";
        		document.getElementById("geoparserList").innerHTML = html;
        		
        		// add new geoparser' version information temporarily 
        		geoparserVersion[name]=(new Date().toShortFormat(1));
        		gazetteerVersion[name]=(new Date().toShortFormat(1));
        		
        	}
        	$("#geoparserConnectButton").prop('disabled', false);
        },
		error : function(jqXHR, textStatus, errorThrown) 
		{
			alert('ERRORS: ' + ':' + textStatus);
			document.getElementById("testArticle").innerHTML = "The geoparser URL is either invalid or not following the recommended format.";
			$("#geoparserConnectButton").prop('disabled', false);
		}});
    }

