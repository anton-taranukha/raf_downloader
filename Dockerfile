# syntax=docker/dockerfile:1

FROM node:24-alpine AS frontend-build
WORKDIR /app/frontend

COPY frontend/package*.json ./
RUN npm ci

COPY frontend/ ./
RUN npm run build

FROM maven:3.9.9-eclipse-temurin-21 AS backend-build
WORKDIR /app

COPY backend/ ./backend/
COPY --from=frontend-build /app/frontend/dist/ ./backend/src/main/resources/static/

RUN mvn -f backend/pom.xml -q package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app

ENV SERVER_PORT=8080
ENV GOOGLE_DRIVE_FOLDER_ID=1hbWUcdEkdDcPMGi-9lTB1ykqcBsq1gTA

COPY --from=backend-build /app/backend/target/google-documents-downloader-0.0.1-SNAPSHOT.jar /app/app.jar

USER app
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
