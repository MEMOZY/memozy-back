FROM ubuntu:22.04

ARG PROFILE
ARG TZ=Asia/Seoul
ARG JAR_FILE=./build/libs/memozy-back-0.0.1-SNAPSHOT.jar

ENV DEBIAN_FRONTEND=noninteractive

# 필수 패키지 설치 + Java 21
RUN apt-get update && \
    apt-get install -y wget gnupg2 software-properties-common curl git build-essential pkg-config \
    libpng-dev libjpeg-dev libtiff-dev libwebp-dev libheif-dev libde265-dev ca-certificates && \
    rm -rf /var/lib/apt/lists/*

# Java 설치 (Temurin 21)
RUN wget -O- https://packages.adoptium.net/artifactory/api/gpg/key/public | gpg --dearmor > /etc/apt/trusted.gpg.d/adoptium.gpg && \
    add-apt-repository -y 'deb https://packages.adoptium.net/artifactory/deb jammy main' && \
    apt-get update && \
    apt-get install -y temurin-21-jdk && \
    rm -rf /var/lib/apt/lists/*

# ImageMagick 7 소스 빌드
WORKDIR /tmp
RUN git clone https://github.com/ImageMagick/ImageMagick.git && \
    cd ImageMagick && \
    git checkout 7.1.1-29 && \
    ./configure --with-heic=yes && \
    make && make install && \
    ldconfig && \
    cd .. && rm -rf ImageMagick

# JAR 복사
COPY ${JAR_FILE} app.jar

# 환경 변수 설정
ENV PROFILE=${PROFILE}
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8

# 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "-Duser.timezone=${TZ}", "-Dspring.profiles.active=${PROFILE}", "app.jar"]