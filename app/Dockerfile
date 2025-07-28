FROM eclipse-temurin:21-jre-alpine

ARG JAR_FILE=target/rinhabackend-0.0.1-SNAPSHOT.jar

COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-XX:+UseG1GC", "-Xms300m", "-Xmx300m", "-jar", "app.jar"]

EXPOSE ${PORT}