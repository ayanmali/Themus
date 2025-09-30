# TODO: use graalvm native image
FROM ghcr.io/graalvm/jdk-community:25.0.0-ol8-20250916
# TODO: add this to a shell script
# RUN "mvn", "clean", "package"
COPY ./target/*.jar app.jar
RUN chmod +x app.jar

CMD ["java","-jar","app.jar"]