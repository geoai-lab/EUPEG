/* 
 * this file save the initial configuration of EUPEG web page
 * configObject -- Copora & supported metrics; Geoparsers (nickname & official name)
 * metricMapping (nickname & official name)
 * geoparserVersion -- initial version information of hosted geoparsers
 * gazetteerVersion -- initial version information of gazetteer of hosted geoparsers
 */
var configObject = {
		
		"datasets":[

			{
				"dataset_name":"LGL",
				"support_metrics":["precision","recall","f_score","median","mean","accuracy_161","AUC"]
			},

			{
				"dataset_name":"GeoVirus",
				"support_metrics":["precision","recall","f_score","median","mean","accuracy_161","AUC"]
			},
			
			{
				"dataset_name":"TR-News",
				"support_metrics":["precision","recall","f_score","median","mean","accuracy_161","AUC"]
			},
			
			{
				"dataset_name":"GeoWebNews",
				"support_metrics":["precision","recall","f_score","median","mean","accuracy_161","AUC"]
			},

			{
				"dataset_name":"WikToR",
				"support_metrics":["accuracy","median","mean","accuracy_161","AUC"]
			},
			
			{
				"dataset_name":"GeoCorpora",
				"support_metrics":["precision","recall","f_score","median","mean","accuracy_161","AUC"]
			},
			
			{
				"dataset_name":"Hu2014",
				"support_metrics":["accuracy","median","mean","accuracy_161","AUC"]
			},
			
			{
				"dataset_name":"Ju2016",
				"support_metrics":["accuracy","median","mean","accuracy_161","AUC"]
			}
			
		],
		"geoparsers":[
			{
				"geoparser_id":"GeoTxt",
				"geoparser_name":"GeoTxt"
			},
			{
				"geoparser_id":"Edinburgh",
				"geoparser_name":"Edinburgh Geoparser"
			},
			
			{
				"geoparser_id":"TopoCluster",
				"geoparser_name":"TopoCluster"
			},
			
			{
				"geoparser_id":"CLAVIN",
				"geoparser_name":"CLAVIN"
			},
			
			{
				"geoparser_id":"Yahoo",
				"geoparser_name":"Yahoo!PlaceSpotter"
			},
			
			{
				"geoparser_id":"CamCoder",
				"geoparser_name":"CamCoder"
			},
			
			{
				"geoparser_id":"StanfordNER",
				"geoparser_name":"StanfordNER+Pop"
			},
			{
				"geoparser_id":"SpaCyNER",
				"geoparser_name":"SpaCyNER+Pop"
			},
			
			{
				"geoparser_id":"DBpedia",
				"geoparser_name":"DBpedia Spotlight"
			}
	
		]
}


// this mapping table contains the full names of the metrics
var metricMapping = {
		"precision":"Precision",
		"recall":"Recall",
		"f_score":"F-Score",
		"accuracy":"Accuracy",
		"mean":"Mean (km)",
		"median":"Median (km)",
		"accuracy_161":"Accuracy@161",
		"AUC":"AUC"
}

var geoparserVersion = {
		"GeoTxt":"version 1.0",
		"Edinburgh":"version 1.1",
		"TopoCluster":"updated Nov. 2016",
		"CLAVIN":"version 2.1.0",
		"Yahoo":"--",
		"DBpedia":"version 1.0.0",
		"SpaCyNER":"version 2.0.18",
		"StanfordNER":"version 3.9.2",
		"CamCoder":"updated Sept. 2018"
}

var gazetteerVersion = {
		"GeoTxt":"updated July 2017",
		"Edinburgh":"R201904",
		"TopoCluster":"updated Nov. 2016",
		"CLAVIN":"updated Apr. 2019",
		"Yahoo":"--",
		"DBpedia":"2019-04",
		"SpaCyNER":"R201904",
		"StanfordNER":"R201904",
		"CamCoder":"updated July 2018"
}
		
		
		