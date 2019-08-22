# Start with a base image containing Java runtime (mine java 8)
FROM openjdk:8
# Add Maintainer Info
LABEL maintainer="someone@tc3.com"
# Install gradle so it doesn't keep downloading it
RUN wget -q https://services.gradle.org/distributions/gradle-5.5-bin.zip \
    && unzip gradle-5.5-bin.zip -d /opt \
    && rm gradle-5.5-bin.zip
ENV GRADLE_HOME /opt/gradle-5.5
ENV PATH $PATH:/opt/gradle-5.5/bin
# Execute following in the image to create a working directory
RUN mkdir /website-crawler
# Set the working directory to the above directory
WORKDIR /website-crawler
# Copy everything to the working directory above
COPY ./gradlew ./settings.gradle ./
COPY ./config ./config
COPY ./crawler-service ./crawler-service
COPY ./gradle ./gradle
# Make port 8080 available to the world outside this container
EXPOSE 8080
# Run the jar file 
ENTRYPOINT ["sh", "./gradlew", "bootRun"]