# ---- Stage 1: build tracker.war ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q -DskipTests package

# ---- Stage 2: slim, non-root runtime (the WAR is executable: embedded Tomcat starts from it) ----
FROM eclipse-temurin:17-jre-alpine
ARG APP_VERSION=1.0.0
LABEL org.opencontainers.image.title="microfinance-tracker" \
      org.opencontainers.image.version="${APP_VERSION}"

RUN addgroup -S app && adduser -S app -G app && mkdir -p /data && chown app:app /data
WORKDIR /app
COPY --from=build /build/target/tracker.war /app/tracker.war

ENV SPRING_PROFILES_ACTIVE=prod \
    APP_DATA_DIR=/data \
    PORT=8081 \
    JAVA_OPTS="-XX:MaxRAMPercentage=75"

USER app
VOLUME /data
EXPOSE 8081

HEALTHCHECK --interval=15s --timeout=3s --start-period=40s --retries=5 \
  CMD wget -qO- "http://localhost:${PORT}/actuator/health" | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/tracker.war"]
