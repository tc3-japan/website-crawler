
# Solr-API 

## Requirements
* JDK 8
* Gradle 5.5
* Spring Boot 1.5.7.RELEASE
* Docker 18
* Lombok Latest >= 1.18.8 Install it to your own IDE if needed.
* Solr 8.1.1

## Quick Start


 1. Edit database settings on src/main/resources/application-default.yml
 
    you can update solr-uri if you need
    
    ```
    crawler-settings:
      solr-uri: http://localhost:8983/solr/manufacturer_product
    ```
 

2. Download and Run the Docker Image for pre-configured Solr Core with Test Data:
  
		1. Download the Solr docker image.
		docker pull azh4r/solr-productsearch-test_data:8.1.1
		
		2. Run the Docker container. 
			docker run -d -t --name tc3 -p 0.0.0.0:8983:8983 azh4r/solr-productsearch-test_data:8.1.1
		
		3. Open a shell inside the Docker container and run the preconfigured Solr instance.
			 
			docker exec -i -t tc3 bash
			cd opt/solr-8.1.1
			bin/solr start -force
		
		Now Solr is running in docker image mapped to port 8983 on your host.	
	
	
	
	To verify the Solr core:
	
		1. Point your browser to http://localhost:8983 to open Solr Admin
		
		2. Select manufacturer_product Core from the drop down Menu on the LHS Navigation menu. This will give you more options specific to the Core. 
		
		3. Select the Query from the LHS menu
		
		4. Execute the default query (q field = *:*) and in the response numFound will have a value = 835.  That is 835 records exists in the Solr Index. 


## Code formatting
We use (google code style)[https://google.github.io/styleguide/javaguide.html].  
You might be able to find the code formatter setting file written for your editor here: https://github.com/google/styleguide

## Spot Bugs
Spot Bugs[https://spotbugs.github.io/] is integrated in the build process.
You should remove all potential bugs or flaws found by Spot Bugs.

## Unit Test
Unit test is integrated in the build process
