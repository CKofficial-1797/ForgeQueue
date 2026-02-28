FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
COPY forgequeue-core/pom.xml forgequeue-core/pom.xml
COPY forgequeue-gateway/pom.xml forgequeue-gateway/pom.xml

RUN mvn -B -q -e -DskipTests dependency:go-offline

COPY . .

RUN mvn -pl forgequeue-core -am clean package -DskipTests


FROM eclipse-temurin:17-jre
WORKDIR /app

RUN addgroup --system forgequeue && adduser --system --ingroup forgequeue forgequeue

COPY --from=build /app/forgequeue-core/target/*.jar app.jar

USER forgequeue

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]