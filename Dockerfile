FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

COPY src/ src/

RUN ./mvnw -q -DskipTests package

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

RUN useradd --create-home spring

COPY --from=build /app/target/app-0.0.1-SNAPSHOT.jar app.jar

USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
