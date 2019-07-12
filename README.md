
# Website-Cralwer

## Requirements
* JDK 8
* MySQL server `8` or higher. Or use latest version as in docker image.
* Gradle 5.5
* Spring Boot 1.5.7.RELEASE
* Docker 18

## Quick Start

1. To start the mySql server in Docker:
   ```bash
   cd crawler-database
   docker-compose up

 2. Edit database settings on src/main/resources/application-default.yml
    ```yaml
    spring:
      datasource:
        url: jdbc:mysql://{mysql server host}:{port}/website-crawler?useSSL=false
        username: {username}
        password: {password}
    ```
    
    The username and password are in the docker-compose.yml file in the 'crawler-database' sub project.
    
    NOTE: if you get error for loading the application config in below steps, you may need to edit `applicationConfig` in `build.gradle` to absolute path
1. Migrate the database
    ```bash
    ./gradlew flywayMigrate
    ```
1. Insert sample data by executing db/V1.1_create_sample.sql on the database

3. Building with Gradle, in project root:
    ```bash
    ./gradle build
    ```
4. To Run Test case in project root run:
    ```bash
    ./gradle test
    ```
5. To Run the website-crawler in project root run::
    ```bash
    ./gradle run
    ```

6. To Build the Docker Image of website-crawler: 
    ```bash
    cd cralwer-service
    docker build -t website-crawler
    ```

7. To Run the website-crawler app inside the docker image:
    ```bash
    docker run website-crawler
    ```

## Code formatting
We use (google code style)[https://google.github.io/styleguide/javaguide.html].  
You might be able to find the code formatter setting file written for your editor here: https://github.com/google/styleguide

## Spot Bugs
Spot Bugs[https://spotbugs.github.io/] is integrated in the build process.
You should remove all potential bugs or flaws found by Spot Bugs.

## Unit Test
Unit test is integrated in the build process.
You also can run the tests as below.
    ```bash
    ./gradlew test
    ```

## Build
1. Building with Gradle
    ```bash
    ./gradlew build
    ```
