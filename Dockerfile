FROM azul/zulu-openjdk-alpine:21.0.1-21.30.15
WORKDIR /app
COPY build/libs/spribe-task-0.0.1-SNAPSHOT.jar /app
EXPOSE 8080
CMD ["java", "-jar", "spribe-task-0.0.1-SNAPSHOT.jar"]