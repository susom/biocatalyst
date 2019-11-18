FROM openjdk:8-jdk-alpine
VOLUME /tmp
COPY build/libs/*.war app.war
ENTRYPOINT ["java","-jar","/app.war"]