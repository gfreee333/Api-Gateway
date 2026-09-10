FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN apk add --no-cache curl
COPY api-gateway/target/api-gateway-*.jar app.jar
COPY api-gateway/src/main/resources/keys /app/keys
EXPOSE 8092
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=dev", "/app/app.jar"]