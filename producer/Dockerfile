#FROM openjdk:8-jdk-slim
FROM gradle:5.0-jdk8-slim
ADD . /src
WORKDIR /src
USER root
RUN gradle war

FROM tomcat:8-slim
RUN rm -rf //usr/local/tomcat/webapps/*
COPY --from=0 /src/build/libs/producer-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/producer.war