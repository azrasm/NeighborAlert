FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:InitialRAMPercentage=50.0 \
               -XX:+UseG1GC \
               -XX:+OptimizeStringConcat \
               -Djava.security.egd=file:/dev/./urandom"

COPY target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]