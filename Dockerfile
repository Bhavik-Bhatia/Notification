# Use OpenJDK 21 base image
FROM openjdk:21-slim

# Set the working directory inside the container
WORKDIR /home/asite/Documents/notification

# Copy your WAR file into the container (adjust paths as necessary)
COPY notification-web/build/libs/notification-web.war /home/asite/Documents/notification/notification-web.war
COPY resources/application.properties /home/asite/Documents/notification/resources/application.properties

# Expose the necessary port for the Spring Boot application (default is 8080, but you're debugging on port 7000)
EXPOSE 8080
EXPOSE 7000

# Set JAVA_HOME (not necessary if you're using OpenJDK base image, but added for completeness)
ENV JAVA_HOME="/home/asite/Desktop/Bhavik/Sboot_project/jdk-21.0.1"
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# Command to run the WAR file with custom java options (debugging and Spring properties)
CMD ["java", "-Xdebug", "-Xrunjdwp:server=y,transport=dt_socket,address=7000,suspend=n", \
    "-Djava.locale.providers=COMPAT,CLDR", \
    "-Djboss.server.home.dir=/home/a/Documents/notification", \
    "-jar", "notification-web.war", \
    "--spring.config.location=resources/application.properties"]