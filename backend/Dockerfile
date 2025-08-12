# TODO: use graalvm native image
FROM ghcr.io/graalvm/jdk-community:24
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java","-jar","/app.jar"]