# Extensible and Unified Platform for Evaluating Geoparsers

### Enhancing spatial and textual analysis with EUPEG for Transaction GIS 
The accepted pdf manuscript for Transactions in GIS included in this directory.

#### Overall description
Geoparsers are useful tools that extract structured geographic information from unstructured texts, thereby enabling spatial analysis on textual data. While a number of geoparsers have been developed, they were tested on different datasets using different metrics. Therefore, it is difficult to directly compare the performances of existing geoparsers. This paper presents EUPEG: An Extensible and Unified Platform for Evaluating Geoparsers. EUPEG is an open source and Web based benchmarking platform which hosts a majority of open corpora, geoparsers, and performance metrics reported in the literature. A newly developed geoparser can be connected to EUPEG and compared with other geoparsers based on the hosted datasets and new datasets uploaded. The main objective of EUPEG is to reduce the time and efforts that researchers have to spend in preparing datasets and baselines, thereby facilitating effective and efficient comparisons of geoparsers. Consequently, we open the source codes of EUPEG so that it can be more convenient for other researchers to host their own data and tools.  

#### Resources
This repository contains the accompanying data and source code for EUPEG proposed in the paper. Additional raw corpora dataset required as the files are too large for GitHub, please download files from:(https://path/download/the-raw-corpora). 


#### Repository organization

The whole repository is a complete and well-organized Maven Java Web Application:
* pom.xml file contains information about the EUPEG as well as its configuration details used by Maven to build the project;
* The file "/src/main/webapp/index.html" is the home page of EUPEG;
* The folder "/src/main/webapp/js" contains the JavaScript code for implementing the user side functions: selecting corpora, geoparsers, and metrics, sending request to the server, and visualizing the results;
* The folder "/src/main/java" contains the Java source code for implementing the server-side functions: receive and process the request, run the selected geoparsers on selected corpora, save the results and return response.


## Getting Started

These instructions will get you a copy of the project up and running on your local machine for further development purpose. 

### Dependencies
* Java SE 10+
* Apache Tomcat 8.0+
* Apache Maven 3.6.0
* SQLite3 database
* Professional Java development IDE (e.g. Eclipse or IntelliJ IDEA)
* Python2.7+ and necessary python libraries if you want to host python-based geoparsers(e.g. SpaCyNER and CamCoder)
* Follow the Geoparsers instruction to install other dependencies for one particular geoparser

### Begin to deploy
Clone this project to your local machine, then follow this step by step tutorials that tell you how to get a local EUPEG running.


#### Step1 - Corpora

* unzip the corpora.zip as a folder named "corpora" under "/src/main/webapp" folder;
* The output files and ground-truth files are already attached with the source codes. See the groundtruth files in the folder "/src/main/webapp/gold", and the output files in the folder "/src/main/webapp/geoparser_output".


#### Step2 - Geoparsers
The provided source codes already save your lots of effort in connecting geoparsers with the EUPEG platform. However, you still need to do extra work in installing some of the geoparsers on your own machine:

* CamCoder (https://github.com/milangritta/Geocoding-with-Map-Vector)
* CLAVIN (https://clavin.bericotechnologies.com/clavin-core/)
* Edinburgh (https://www.ltg.ed.ac.uk/software/geoparser/)
* SpaCyNER (https://spacy.io/usage)
* Topocluster (https://github.com/grantdelozier/TopoCluster)

You don't need to care about deploying the three geoparsers listed below since they are either pure API connected or totally embedded inside the EUPEG platform. Still, you can check their documents and modify the default configuration on EUPEG to better support your own application.
* DBpedia Spotlight (https://www.dbpedia-spotlight.org/)
* GeoTxt (http://www.geotxt.org/)
* StanfordNER (https://stanfordnlp.github.io/CoreNLP/)

Yahoo has closed the YQL service which we previously used to access Yahoo! PlaceSpotter geoparser. Therefore, it's late for us to include the new version of PlaceSpotter in this demo. Sorry about that. Please follow this instruction (https://developer.yahoo.com/boss/geo/docs/PM_KeyConcepts.html) if you want to implement the Yahoo! PlaceSpotter by your own.

#### Step3 - Start EUPEG
This tutorial is the example of deploying and configuring the EUPEG as the Java Web application using Eclipse IDE. Still, you can use it as the reference if you work with other  IDE.

#### Import the project via Maven
Import EUPEG as the existing Maven project, Maven tool will download and deploy all Java dependencies as listed in the pom.xml;

Open "/src/main/java/EUPEGConfiguration.java" file and change all the path information to the actual path on your machine.

#### Test on the local server
Complete the connection of tomcat server with Eclipse IDE;

Run index.html as Run on the server \(Follow the wizard to add EUPEG into local server for the first time deployment\);

Open http://localhost:8080/EUPEG/index.html in your preferred browser.

#### Possible errors and how to fix

* Updating Maven Dependencies is very slow:

Make sure you have completed CLAVIN configuration. If you want to deploy CLAVIN later, please replace the content of pom.xml with the content of pom_no_CLAVIN.xml. 

* Java version error:

Check the Java version of Java Compiler and Project Facets in the Properties window. Change the Java version to 10 or 11.

* Java Build Path configuration error:

If an error of occurs at the "EdinburghParser.java" file. Open "Configure Build Path" window  of EUPEG (right click EUPEG and find Build Path). Move JRE System Library to the Top and choose "Apply and Close". You shall see the  error disappear.

* Feel free to contact us if you meet any extra problems.

Congratulations! You have gone through all steps of deploying the EUPEG. You may now begin to edit and debug the source codes for your own research.

## Direct usage of online demo

You can also directly use our [online demo](https://geoai.geog.buffalo.edu/EUPEG/)

### Construct your experiment
Use the checkbox to select your needed corpora, geoparsers, and metrics.
<p align="center">
<img align="center" src="fig/main_page1.png" width="600" />
</p>

### Upload your resources
* Click the "Add corpus..." button to upload your dataset
<p align="center">
<img align="center" src="fig/main_page4.png" width="260" height="150" />
<img align="center" src="fig/main_page5.png" width="260" height="150"/>
<img align="center" src="fig/main_page8.png" width="260" height="150"/>
</p>

Note: The "Adding a new corpus" window can only process one uploaded corpus in xml format, you need to upload your files separately if you want to test your multiple corpora in the same expoeriment.


* Click the "Add geoparser..." button to connect with your geoparser API
<p align="center">
<img align="center" src="fig/main_page9.png" width="260" height="150" />
<img align="center" src="fig/main_page10.png" width="260" height="150"/>
<img align="center" src="fig/main_page11.png" width="260" height="150"/>
</p>

Note: The "Adding geoparser" function may take some time to connect with your geoparser.


### View the evaluation result
Click "Run this experiment" button to send request, you will get the experiment ID immediately while the parsing process can take quite some time (varying from several minutes to several hours) if you try to use your own resources. You are highly suggested to search for the result of this experiment using the provided ID. However, if you just try to run the experiment only with the existing resources, the result shall be shown quickly.
<p align="center">
<img align="center" src="fig/main_page2.png" width="600" />
</p>

### Search for previous experiment
Input your experiment ID in the "Search Experiments" panel and click "Search"
<p align="center">
<img align="center" src="fig/main_page3.png" width="600" />
</p>


## Authors

* **Yingjie Hu** - *GeoAI Lab* - Email: yhu42@buffalo.edu
* **Jimin Wang** - *GeoAI Lab* - Email: jiminwan@buffalo.edu

## License

This project is licensed under the **** License - see the [LICENSE.md](LicenseNSE.md) file for details

## Acknowledgments

* Hat tip to anyone whose code was used
* Inspiration
* etc
