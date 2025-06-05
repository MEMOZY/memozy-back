FROM ubuntu:22.04

ARG PROFILE
ARG TZ=Asia/Seoul
ARG JAR_FILE=./build/libs/memozy-back-0.0.1-SNAPSHOT.jar

ENV DEBIAN_FRONTEND=noninteractive

# 기본 패키지 + heif 지원 포함된 ImageMagick 설치
RUN apt-get update && \
    apt-get install -y \
    software-properties-common && \
    add-apt-repository -y ppa:strukturag/libheif && \
    apt-get update && \
    apt-get install -y \
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