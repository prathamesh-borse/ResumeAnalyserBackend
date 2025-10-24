# Step 1: Build the app
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Step 2: Run the app
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
# ENTRYPOINT in the dockerfile specifies the instruction for an app to be executable when the container starts.
# In entrypoint we specify the executable, params1 and param2 instruction
# for example, for this application it is an java application so we specify java as executable
# and -jar as the param because the application will be run from a jar file.
# Finally, we specify the app name of the jar file as the second param.

