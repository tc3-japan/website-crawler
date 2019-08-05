
# Website-Cralwer

## Requirements
* JDK 8
* MySQL server `8` or higher. Or use latest version as in docker image.
* Gradle 5.5
* Spring Boot 1.5.7.RELEASE
* Docker 18
* Lombok Latest >= 1.18.8 Install it to your own IDE if needed.

## Quick Start

1. To start the mySql server in Docker:
   ```bash
   cd crawler-database
   docker-compose up
   ```

 2. Edit database settings on src/main/resources/application-default.yml
    ```yaml
    spring:
      datasource:
        url: jdbc:mysql://{mysql server host}:{port}/website_crawler?useSSL=false
        username: {username}
        password: {password}
    ```
    
    The username and password are in the docker-compose.yml file in the 'crawler-database' sub project.
    
    

    you can update solr-uri if you need
    
    ```
    crawler-settings:
      solr-uri: http://localhost:8983/solr/manufacturer_product
    ```
 
    NOTE: if you get error for loading the application config in below steps, you may need to edit `applicationConfig` in `build.gradle` to absolute path
 
1. Migrate the database
    ```bash
    ./gradlew flywayMigrate
    ```
 
2. Building with Gradle, in project root:
    ```bash
    ./gradlew build
    ```
    
       after build, you can found bug report in *./⁨crawler-service⁩/build⁩/⁨reports⁩/⁨spotbugs⁩/index.html*
 
3. To Run Test case, in project root:
    ```bash
    ./gradlew clean test jacocoTestReport
    ```
    after test, you can found report in *./crawler-service/build/jacoco/index.html*

4. To Run the website-crawler, in project root:
    ```bash
    ./gradlew bootRun -Pargs=--site=1,--proc=crawler
    ```

5. Download and Run the Docker Image for pre-configured Solr Core:
  
		1. Download the Solr docker image.
		docker pull gigops/solr-productsearch:8.1.1-challenge
		
		2. Run the Docker container. 
			docker run -d -t --name tc3 -p 0.0.0.0:8983:8983 gigops/solr-productsearch:8.1.1-challenge
		
		3. Open a shell inside the Docker container and run the preconfigured Solr instance.
			 
			docker exec -i -t tc3 bash
			cd opt/solr-8.1.1
			bin/solr start -force
		
		Now Solr is running in docker image mapped to port 8983 on your host.	
	
	
	
	To verify the Solr core:
	
		1. Point your browser to http://localhost:8983 to open Solr Admin
		
		2. Select manufacturer_product Core from the drop down Menu on the LHS Navigation menu. This will give you more options specific to the Core. 
		
		3. Select the Schema from the LHS menu
		
		4. In the body of the page now you can choose the different defined fields for this Challenge, such as
		manufacturer_name, product_url, html_title, html_body, page_updated_at


## Code formatting
We use (google code style)[https://google.github.io/styleguide/javaguide.html].  
You might be able to find the code formatter setting file written for your editor here: https://github.com/google/styleguide

## Spot Bugs
Spot Bugs[https://spotbugs.github.io/] is integrated in the build process.
You should remove all potential bugs or flaws found by Spot Bugs.

## Unit Test
Unit test is integrated in the build process.
You also can run the tests as below.

`./gradlew clean test jacocoTestReport`,  you can found report in *./crawler-service/build/jacoco/index.html

## Verifcation

1. startup solr service first
2. run `./gradlew bootRun -Pargs=--site=1,--proc=crawler` to fetch some page data ( need few mins to fetch data and almost need 2 hours to finish) , you can exit crawler when got data to test converter process
3. use `./gradlew bootRun -Pargs=--proc=converter,--site=1`  to run converter process
   - after run, check the solr(Select manufacturer_product Core from the drop down Menu on the LHS Navigation menu, and then click **Query**, click "**Execute Query**")
   - update deleted to 1 in pages table, then run this again and check the solr service
   - update some last_modified_at in pages table to 2018-10-10, then run this again and check the solr service
4. update pages table last_modified_at value, then use `./gradlew bootRun -Pargs=--proc=converter,--only-data-cleanup,--site=1` to run cleanup process independently

## Note

- fixed issue when crawler running 10 mins or later, it is always only have one task in same time,  no matter the parallel size value
- Because of convert process need check deleted, last_modified_date and last_processed_at, so there is nothing to do on sql. i use paging search on database and multiple thread to speed up whole process. it will run effective no matter how big the database.
- for `--proc=converter`, the `--site` params is optional param.