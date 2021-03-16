
# Website-Crawler & Solr-API 

## Requirements
* JDK 8
* MySQL server `8` or higher. Or use latest version as in docker image.
* Gradle 5.5
* Spring Boot 1.5.7.RELEASE
* Docker 18
* Lombok Latest >= 1.18.8 Install it to your own IDE if needed.
* Solr 8.1.1

## Quick Start

1. To start the mySql server and solr in Docker:
   ```bash
   cd website-crawler/crawler-docker
   docker-compose up -d mysql
   docker-compose up -d solr
```
   
1. Edit database settings on src/main/resources/application-default.yml
    ```yaml
    spring:
      datasource:
        url: jdbc:mysql://{mysql server host}:{port}/website_crawler?useSSL=false
        username: {username}
        password: {password}
    ```
    
    The username and password are in the docker-compose.yml file in the 'crawler-database' sub project.
    
    

    you can update solr.uri if you need
    
    ```
    solr:
        uri: http://localhost:8983/solr/manufacturer_product
    ```

    NOTE: if you get error for loading the application config in below steps, you may need to edit `applicationConfig` in `build.gradle` to absolute path

1. Migrate the database
    ```bash
    ./gradlew flywayMigrate
    ```

1. Building with Gradle, in project root:
    ```bash
    ./gradlew build
    ```
    
       after build, you can found bug report in *./⁨crawler-service⁩/build⁩/⁨reports⁩/⁨spotbugs⁩/index.html*

1. To Run Test case, in project root:
    ```bash
    ./gradlew clean test jacocoTestReport
    ```
    after test, you can found report in *./crawler-service/build/jacoco/index.html*

1. To Run the website-crawler, in project root:
    ```bash
    ./gradlew bootRun -Pargs=--site=1,--proc=crawler
    ```

1.  Verify the Solr core:
  
		1. Point your browser to http://localhost:8983 to open Solr Admin
		
		2. Select manufacturer_product Core from the drop down Menu on the LHS Navigation menu. This will give you more options specific to the Core. 
		
		3. Select the Query from the LHS menu
		
		4. Execute the default query (q field = *:*) and in the response numFound will have a value = 835.  That is 835 records exists in the Solr Index. 
	
1.  To Run the API service, in project root:
    ```bash
    ./gradlew bootRun -Pargs=--rest
    ```

    The service is running at http://localhost:8090/api/

    The example endpoint:
    http://localhost:8090/api/websites/1


## Code formatting
We use (google code style)[https://google.github.io/styleguide/javaguide.html].  
You might be able to find the code formatter setting file written for your editor here: https://github.com/google/styleguide

## Spot Bugs
Spot Bugs[https://spotbugs.github.io/] is integrated in the build process.
You should remove all potential bugs or flaws found by Spot Bugs.

## Unit Test
Unit test is integrated in the build process

## Verifcation

Following is not necessarily needed for challenge#3 F2F, but is here as a reference if the developer needs to reproduce the data.

1. startup solr service first
2. run `./gradlew bootRun -Pargs=--site=1,--proc=crawler` to fetch some page data ( need few mins to fetch data and almost need 2 hours to finish) , you can exit crawler when got data to test converter process
3. use `./gradlew bootRun -Pargs=--proc=converter,--site=1`  to run converter process
   - after run, check the solr(Select manufacturer_product Core from the drop down Menu on the LHS Navigation menu, and then click **Query**, click "**Execute Query**")
   - update deleted to 1 in pages table, then run this again and check the solr service
   - update some last_modified_at in pages table to 2018-10-10, then run this again and check the solr service
4. update pages table last_modified_at value, then use `./gradlew bootRun -Pargs=--proc=converter,--only-data-cleanup,--site=1` to run cleanup process independently
5. run `./gradlew bootRun -Pargs=--calc_period=15,--proc=calctr` will collect users' click logs and calculate click-through rate of pages in search results. 
