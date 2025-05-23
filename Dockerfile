FROM eclipse-temurin:21-jdk

ARG PROFILE
ARG TZ=Asia/Seoul
ARG JAR_FILE=build/libs/app.jar

COPY ${JAR_FILE} app.jar

ENV LANG C.UTF-8
ENV LC_ALL C.UTF-8

EXPOSE 8080

ENTRYPOINT ["java", "-Duser.timezone=${TZ}", "-Dspring.profiles.active=${PROFILE}", "-jar", "app.jar"]