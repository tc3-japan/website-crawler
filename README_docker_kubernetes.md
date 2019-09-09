1. docker-compose needs to be updated to latest version to recognize network flags. 
version 1.24.1 works! 

1. changes to mysql docker-compose for communicating with crawler app running inside container.
Following was added to the docker-compose.yaml.  
............
    networks:  
      network1:
    container_name: mycontainername
    hostname: myhostname
networks:
  network1:
    name: crawler_network

networks is needed to create a named network that can be joined by docker container hosting the crawler app and the docker container hosting the solr database. 

container_name and hostname (not used) are needed so that crawler process running inside container can find the mysql server running inside container.  Otherwise crawler process cannot find mysql by using localhost.   Only the hosting OS can access the container services by using localhost on the exposed ports.

1.  Start myssql in Docker
docker-compose up 

Start like before because changes have been made to the yaml file. 

1. No changes to Solr docker file
Start solr docker container with --network parameter so that it joins the network created when starting mysql docker container. 
docker run -d -t --name tc3 --network="crawler_network" -p 8093:8093 azh4r/solr-productsearch-test_data:8.1.1

1. For website-cralwer app in Docker

Changes to application-default.yaml file and build.gradle for running in container
When building the website-crawler app outside of container for the image, the mysql container needs to be accessed as localhost
because we are builind the app outside container and mysql container_name will not be recognized for mysql url. So we define 
url_flyway which will be used by the build.gradle flyway task

url_flyway: jdbc:mysql://localhost:3306/website_crawler?useSSL=false&nullNamePatternMatchesAll=true&allowPublicKeyRetrieval=true

1. The gradle.build flyway task is also modifed with the new url_flyway parameter.
url = (applicationConfig.spring.datasource.url_flyway =~ /(.*\/\/.*\/).*/)[0][1] // Extract URL

1. For mysql datasource url we will use container_name instead of localhost because this will be accessed from another docker container
url: jdbc:mysql://mycontainername:3306/website_crawler?useSSL=false&nullNamePatternMatchesAll=true&allowPublicKeyRetrieval=true

1. Also for solr uri we will use the solr container name instead of localhost because this will also be accessed from a different docker container. 
The convert solr uri
  solr-uri: http://tc3:8983/solr/manufacturer_product

1. After the docker image for crawler app is build using the docker file
docker build -t website-crawler .

1. Image can be run with --network parameter and passing in application argument --rest or otherwise
docker run --name website-crawler-container --network="crawler_network" -p 8090:8090 website-crawler --rest

Now we have each app component (mysql, solr and spring boot app) running in its own docker container.
This now be deployed to EKS pods.
