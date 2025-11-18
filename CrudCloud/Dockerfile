# Etapa 1: build con Maven + JDK 25
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /CrudCloud

# Copiar el pom.xml y descargar dependencias (capa de caché)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiar código fuente y construir
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: imagen ligera de ejecución
FROM eclipse-temurin:21-jdk
WORKDIR /CrudCloud

# Copiar el jar generado desde la etapa de build
COPY --from=build /CrudCloud/target/*.jar CrudCloud.jar

# Exponer el puerto 8080
EXPOSE 8080

# Comando de inicio
ENTRYPOINT ["java", "-jar", "CrudCloud.jar"]
