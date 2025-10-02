FROM openjdk:17.0.1-jdk-slim
WORKDIR /app
COPY ./build/libs/backend-0.0.1-SNAPSHOT.jar /app/backend.jar
EXPOSE 8080
ENTRYPOINT ["java"]
CMD ["-jar", "backend.jar"]