# Dockerfile — en la raíz del proyecto
# Es multi-stage porque primero en el primer stage se compila el proyecto
# y en el segundo stage se ejecuta el jar

# Stage 1: Build
# A partir de la imagen de maven
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
# Establecemos el directorio de trabajo de nuestro contenedor
WORKDIR /app
# Copiamos el pom.xml para que compile el proyecto
COPY pom.xml .
# Tambien copiamos el src en un directorio llamado src dentro del contenedor
COPY src ./src
# Ejecutamos el comando mvn clean package -DskipTests que compilará el proyecto y generará el jar
RUN mvn clean package -DskipTests

# Stage 2: Run
# En este stage usamos una imagen de java con soporte para jvm para ejecutar el jar
FROM eclipse-temurin:17-jre-alpine
# En nuestro contenedor establecemos el directorio de trabajo llamado /app
WORKDIR /app
# Luego , copiamos del stage build el jar que esta en el contenedor build en el directorio /app/target/music-api-*.jar hacia nuestro contenedor de este stage con el nombre app.jar
COPY --from=build /app/target/music-api-*.jar app.jar
# Exponemos el puerto 8080
EXPOSE 8080
# Ejecutamos el jar
ENTRYPOINT ["java", "-jar", "app.jar"]