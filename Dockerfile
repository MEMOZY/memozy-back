FROM ubuntu:22.04

ARG PROFILE
ARG TZ=Asia/Seoul
ARG JAR_FILE=./build/libs/memozy-back-0.0.1-SNAPSHOT.jar

ENV DEBIAN_FRONTEND=noninteractive

# Java 21 + ImageMagick + HEIC 지원 설치
RUN apt-get update && \
    apt-get install -y wget gnupg2 software-properties-common && \
    wget -O- https://packages.adoptium.net/artifactory/api/gpg/key/public | gpg --dearmor > /etc/apt/trusted.gpg.d/adoptium.gpg && \
    add-apt-repository -y 'deb https://packages.adoptium.net/artifactory/deb jammy main' && \
    apt-get update && \
    apt-get install -y \
    temurin-21-jdk \
    imagemagick \
    libheif1 \
    libheif-dev && \
    rm -rf /var/lib/apt/lists/*

COPY ${JAR_FILE} app.jar

ENV PROFILE=${PROFILE}
EXPOSE 8080
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8

ENTRYPOINT ["java","-jar","-Duser.timezone=${TZ}","-Dspring.profiles.active=${PROFILE}","app.jar"]