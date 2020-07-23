
FROM openjdk:15-jdk-alpine3.11
COPY ./target/fiware-openlpwa-genericagent-1.0.0.jar /
WORKDIR /
EXPOSE 8080
ENTRYPOINT ["java", "-jar","fiware-openlpwa-genericagent-1.0.0.jar" ]
