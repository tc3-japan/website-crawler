# docker-compose needs to be updated to latest version to recognize network flags. 
version 1.24.1 works! 

# changes to mysql docker-compose for communicating with crawler app running inside container.
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





# Start solr in Docker

# changes in starting solr in Docker for communications with crawler app running inside container




# Start website-cralwer app in Docker

# changes to application-default.yaml file and build.gradle for running in container
