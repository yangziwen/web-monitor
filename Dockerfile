FROM openjdk:8-jre-alpine
COPY target/web-monitor.jar /usr/src/myapp/web-monitor.jar
WORKDIR /usr/src/myapp
CMD ["java", "-jar", "web-monitor.jar", "server"]
EXPOSE 8050
