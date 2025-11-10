FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy root pom.xml
COPY pom.xml /app/pom.xml

# Create a minimal parent POM for Docker build (only includes modules we're building)
RUN sed -i '/<module>shared\/proto-definitions<\/module>/d; /<module>services\/order-service<\/module>/d; /<module>services\/executive-service<\/module>/d; /<module>services\/restaurant-service<\/module>/d; /<module>services\/notification-service<\/module>/d; /<module>infrastructure<\/module>/d' /app/pom.xml || true

# Copy shared common-lib
COPY shared/common-lib/pom.xml /app/shared/common-lib/pom.xml
COPY shared/common-lib/src /app/shared/common-lib/src

# Copy routing-service
COPY services/routing-service/pom.xml /app/services/routing-service/pom.xml
COPY services/routing-service/src /app/services/routing-service/src

# Build the application
WORKDIR /app
RUN mvn clean install -N -DskipTests && \
    mvn clean install -DskipTests -pl shared/common-lib && \
    mvn clean package -DskipTests -pl services/routing-service -am

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install wget for health checks
RUN apk add --no-cache wget

# Copy the built jar
COPY --from=build /app/services/routing-service/target/*.jar app.jar

# Expose port
EXPOSE 8082

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

