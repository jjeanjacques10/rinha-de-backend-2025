FROM eclipse-temurin:21-jre-alpine

ENV DB_HOST=payment-processor-default-db
ENV DB_PORT=5432

ARG JAR_FILE=target/rinhabackend-0.0.1-SNAPSHOT.jar

COPY ${JAR_FILE} app.jar

#ENTRYPOINT ["java",  "-XX:+UseG1GC", "-Xmx64m", "-Xms64m", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
ENTRYPOINT ["java",  "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]

EXPOSE ${PORT}