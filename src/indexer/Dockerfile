FROM openjdk:8-jdk-alpine
COPY build/libs/*.war app.war
ENTRYPOINT ["java","-jar","/app.war"]