// Call uploaFile Function when submiting the form by clicking submit button
 $(function() 
		 {
            $('#xmlFormSubmit').on('submit', uploadFile);
		 });
 
 // a function for checking if a corpus with the same name exits
 function ifCorpusExist(corpusName)
 {
	 var exists = false;
	 $('#corpusList .custom-control-input').each(function(index, element){
	    	thisCorpusName = $(element).attr("name");
	    	//console.log(thisCorpusName);
	    	if(thisCorpusName.toUpperCase() === corpusName.toUpperCase())
	    		exists = true;
		});
	 
	 if(exists)
		 return true;
		
 }

// get file objects from form control and send to servlet using ajax
function uploadFile(event) {
	
	// first, disable the submit button to prevent a user clicking it multiple times
	$("#corpusSubmitButton").prop('disabled', true);
	
       event.stopPropagation();
       event.preventDefault();
       // pop the first file object
       if(if_new_test == false)
    	{
    	   seriesCode = randomHash();
    	   if_new_test = true;
        }
       var xmlFile = document.getElementById("corpusSelect").files[0];
       var corpusName = document.getElementById("corpusName").value;
       
       if(corpusName == "")
       {
    	   alert("Corpus name is not provided.");
    	   $("#corpusSubmitButton").prop('disabled', false);
    	   return;
       }
       
       if(xmlFile == undefined)
       {
    	   alert("Corpus file is not provided.");
    	   $("#corpusSubmitButton").prop('disabled', false);
    	   return;
       }
       
       //console.log(corpusName);
       //console.log(ifCorpusExist(corpusName));
       if(ifCorpusExist(corpusName))
       {
    	   alert("The corpus name you specified already exists. Please choose a different name.");
    	   $("#corpusSubmitButton").prop('disabled', false);
    	   return;
       }
       
       // initiate data frame in html5 form format 
	   var data = new FormData();
	   // add file data into data frame
	   // console.log(seriesCode);
	   data.append('file',xmlFile,corpusName);
	   data.append('id',seriesCode);
	   var fileSize = Math.round(xmlFile.size / 1024)+ 'KB';
	   postFilesData(data,fileSize,corpusName);
	   
	   $('#xmlFormSubmit').reset();
}


function postFilesData(data,size,corpusName) {
            $.ajax({
                url :  'CorpusUploadServlet',
                type : 'POST',
                data :   data,
                cache : false,
                dataType : 'text',
                processData : false,
                contentType : false,
                success : function(result, textStatus, jqXHR) {
                	
                	if(result.indexOf("Error") == 0)
                	{
                		alert(result);
                		return;
                	}
                	
                	// parse returned json message (statistics of contents in uploaded xml)
                	var resultObject = JSON.parse(result);
                    //alert("successfully parsing your uploaded xml!!");
                    document.getElementById("corpusTableTitle").innerHTML = corpusName;
                    document.getElementById("corpusSize").innerHTML = size;
                    document.getElementById("articleNum").innerHTML = resultObject["articleNum"];
            		document.getElementById("toponymNum").innerHTML = resultObject["validToponymNum"];
            		document.getElementById("avgToponyms").innerHTML =  resultObject["averageNum"];
            		$("#dataset-group").css('display','block'); 
            		var html = document.getElementById("corpusList").innerHTML;
            		
            		// update the new corpus information on the html page
            		var checkhtml = "<div class='custom-control custom-checkbox custom-control-inline mt-2 mr-4'>";
            		checkhtml += "<input type= 'checkbox' class='custom-control-input' name ='" +corpusName+ "' id='"+  resultObject["Name"] + "'>";
            		checkhtml += "<label class='custom-control-label' for='"+  resultObject["Name"] +"'>" + corpusName + "</lable></div>";
					html += checkhtml;
            		$('#corpusList').html(html);
            		
            		
            		// re-enable the submit button 
            		$("#corpusSubmitButton").prop('disabled', false);
            		
                },
                error : function(jqXHR, textStatus, errorThrown) {
                    alert('ERRORS: ' + ':' + textStatus);
                    
                    //re-enable the submit button 
            		$("#corpusSubmitButton").prop('disabled', false);
                }
            });
        }

// function to check file type and change html content when upload user's xml 
function updateCorpusName()
{
	var nBytes = 0,
	oFiles = document.getElementById("corpusSelect").files;
	nFiles = oFiles.length;
	var xmlfileName = "";
	var res1, res2 = " ";
	for (var nFileId = 0; nFileId < nFiles; nFileId++) {
		xmlfileName = oFiles[nFileId].name;
		res1 = xmlfileName.split(".")[0];
		res2 = xmlfileName.split(".")[1];
		if(res2 !== "xml"&& res2 !== "XML")
			{
				//alert(xmlfileName + " is not the XML file!");
				alert(res2)
				res1 = "File Selection Erro"
			}
		//document.getElementById("XMLfileName").innerHTML = res1;
		//document.getElementById("corpusSize").innerHTML =  Math.round(oFiles[nFileId].size / 1024)+ 'KB';
	}

}
