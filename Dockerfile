FROM 665373354552.dkr.ecr.ap-northeast-2.amazonaws.com/memozy/server-base:latest

ARG PROFILE
ARG TZ=Asia/Seoul
ARG JAR_FILE=./build/libs/memozy-back-0.0.1-SNAPSHOT.jar

COPY ${JAR_FILE} app.jar

ENV PROFILE=${PROFILE}
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Duser.timezone=${TZ}", "-Dspring.profiles.active=${PROFILE}", "app.jar"]