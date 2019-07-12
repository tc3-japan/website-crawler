
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

3. Building with Gradle, in project root:
    ```bash
    ./gradle build
    ```
4. To Run Test case in project root run:
    ```bash
    ./gradle run
    ```
5. To Run the website-crawler in project root run::
    ```bash
    ./gradle build
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
