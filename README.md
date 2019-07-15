
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

 2. Edit database settings on src/main/resources/application-default.yml
    ```yaml
    spring:
      datasource:
        url: jdbc:mysql://{mysql server host}:{port}/website_crawler?useSSL=false
        username: {username}
        password: {password}
    ```
    
    The username and password are in the docker-compose.yml file in the 'crawler-database' sub project.
    
    NOTE: if you get error for loading the application config in below steps, you may need to edit `applicationConfig` in `build.gradle` to absolute path

1. Migrate the database
    ```bash
    ./gradlew flywayMigrate
    ```
2. Building with Gradle, in project root:
    ```bash
    ./gradlew build
    ```
3. To Run Test case, in project root:
    ```bash
    ./gradlew test
    ```
4. To Run the website-crawler, in project root:
    ```bash
    ./gradlew bootRun -Pargs=--site=1
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
    ./gradle test
    ```

## Build
1. Building with Gradle
    ```bash
    ./gradlew build
    ```
