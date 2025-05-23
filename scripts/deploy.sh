#!/bin/bash

echo "> Docker 이미지 Pull"
aws ecr get-login-password | docker login --username AWS --password-stdin $ECR_REGISTRY
docker pull 665373354552.dkr.ecr.ap-northeast-2.amazonaws.com/memozy/server:prod

echo "> 기존 컨테이너 중지 및 삭제"
docker stop spring || true
docker rm spring || true

echo "> Docker Compose 실행"
docker compose -f /home/ubuntu/memozy-back/docker-compose.yml up -d

echo "> 이미지 정리"
docker image prune -af