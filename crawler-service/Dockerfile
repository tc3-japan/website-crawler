FROM java:8
VOLUME /tmp
COPY /build/libs/crawler-service.jar docker-crawler.jar
ENTRYPOINT ["java","-jar","docker-crawler.jar"]