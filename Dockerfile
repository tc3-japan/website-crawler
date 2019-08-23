# Start with a base image containing Java runtime (mine java 8)
FROM openjdk:8-slim
# Add Maintainer Info
LABEL maintainer="someone@tc3.com"
# Copy the executable jar from the local file system to the image
COPY ./crawler-service/build/libs/crawler-service-1.0.jar /usr/app/
# sets the working directory
WORKDIR /usr/app
# execute a bash command inside the container
RUN sh -c 'touch crawler-service-1.0.jar'
# Make port 8080 available to the world outside this container
EXPOSE 8080
# Run the jar file 
ENTRYPOINT ["java", "-jar", "crawler-service-1.0.jar"]