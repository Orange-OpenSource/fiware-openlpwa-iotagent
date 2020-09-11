
FROM openjdk:15-jdk-alpine3.11
COPY ./target/fiware-openlpwa-genericagent-1.0.1.jar /
COPY ./DockerReleaseNote.txt /
COPY ./run.sh /
RUN chmod +x /run.sh
WORKDIR /
EXPOSE 8080
ENTRYPOINT ["/run.sh"]
