// convert raw text into downloadable csv link
function downloadCSV(ptext, filename) 
{
    var csvFile;
    var downloadLink;
    // CSV file
    csvFile = new Blob([ptext], {type: "text/csv"});
    // Download link
    downloadLink = document.createElement("a");
    // File name
    downloadLink.download = filename;
    // Create a link to the file
    downloadLink.href = window.URL.createObjectURL(csvFile);
    // Hide download link
    downloadLink.style.display = "none";
    // Add the link to DOM
    document.body.appendChild(downloadLink);
    // Click download link
    downloadLink.click();
}

// extract raw text from experiment result table
function exportHistoryToCSV(filename) 
{    
    var rows = document.getElementById("experimentResultPanel2").querySelectorAll("table tr");

    var csv = [];
    var corpora_list= document.getElementById("experimentResultPanel2").querySelectorAll("h5 i");
    var n_geoparser = rows.length/(corpora_list.length-1);
    
    //add timestamp
    csv.push(corpora_list[0].innerText);
    
    for (var i = 0; i < rows.length; i++) 
    {
        var row = [];
        var cols = rows[i].querySelectorAll("td, th");
        
        for (var j = 0; j < cols.length; j++) 
            row.push(cols[j].innerText.replace(/\n/g,"#"));
       
        if(i%n_geoparser==0){ 
        	csv.push(" ");
        	csv.push(corpora_list[i/n_geoparser+1].innerText);
        }

        csv.push(row.join(","));        
    }

    // Download CSV file
    downloadCSV(csv.join("\n"), filename);
}

// // extract raw text from experiment research table
function exportTableToCSV(filename) 
{    
    var rows = document.getElementById("experimentResultPanel").querySelectorAll("table tr");
    var csv = [];
    var corpora_list= document.getElementById("experimentResultPanel").querySelectorAll("h5 i");
    var n_geoparser = rows.length/(corpora_list.length);
    
    //add timestamp
    csv.push(String(new Date()));
    
    for (var i = 0; i < rows.length; i++) 
    {
        var row = [];
        var cols = rows[i].querySelectorAll("td, th");
        
        for (var j = 0; j < cols.length; j++) 
            row.push(cols[j].innerText.replace(/\n/g,"#"));
       
        if(i%n_geoparser==0){
        	csv.push(" ");
        	csv.push(corpora_list[i/n_geoparser].innerText);
        }

        csv.push(row.join(","));        
    }

    // Download CSV file
    downloadCSV(csv.join("\n"), filename);
}