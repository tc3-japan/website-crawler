FROM openjdk:8-slim
VOLUME /tmp
ARG PROFILE
ARG JAR_FILE
RUN apt-get -qq update && apt-get -qq -y install curl
ENV PROFILE=${PROFILE}
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app.jar", "--spring.profiles.active=${PROFILE}"]
