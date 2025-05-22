FROM eclipse-temurin:21-jdk

ARG PROFILE
ARG TZ=Asia/Seoul
ARG JAR_FILE=./build/libs/*.jar
COPY ${JAR_FILE} app.jar
ENV PROFILE=${PROFILE}
EXPOSE 8080
ENTRYPOINT ["java","-jar","-Duser.timezone=${TZ}","-Dspring.profiles.active=${PROFILE}","app.jar"]