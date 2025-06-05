FROM eclipse-temurin:21-jdk
RUN apt-get update && apt-get install -y imagemagick libheif-dev && rm -rf /var/lib/apt/lists/*

ARG PROFILE
ARG TZ=Asia/Seoul
ARG JAR_FILE=./build/libs/memozy-back-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
ENV PROFILE=${PROFILE}
EXPOSE 8080
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8
ENTRYPOINT ["java","-jar","-Duser.timezone=${TZ}","-Dspring.profiles.active=${PROFILE}","app.jar"]