FROM maven:3.9.11-eclipse-temurin-21-noble AS build

WORKDIR /workspace

COPY pom.xml .
COPY src ./src

RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre-noble

WORKDIR /app

RUN mkdir -p /tmp/employee-service /config && chmod 1777 /tmp/employee-service

COPY --from=build /workspace/target/employee-service-*.jar /app/employee-service.jar
COPY docker-entrypoint.sh /app/docker-entrypoint.sh

RUN chmod +x /app/docker-entrypoint.sh

ENV LOG_FILE=/tmp/employee-service/employee-service.log

EXPOSE 8080

VOLUME ["/tmp/employee-service"]

ENTRYPOINT ["/app/docker-entrypoint.sh"]
