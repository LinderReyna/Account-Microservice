FROM openjdk:11
MAINTAINER jossemar.com
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} account-microservice-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/account-microservice-0.0.1-SNAPSHOT.jar"]