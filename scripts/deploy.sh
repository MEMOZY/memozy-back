#!/bin/bash

ECR_REGISTRY="665373354552.dkr.ecr.ap-northeast-2.amazonaws.com"
ECR_REPOSITORY="memozy/server"
ECR_REPOSITORY_AI="memozy/ai-server"
TAG="prod"

aws ecr get-login-password --region ap-northeast-2 \
| docker login --username AWS --password-stdin $ECR_REGISTRY

docker pull $ECR_REGISTRY/$ECR_REPOSITORY:$TAG
docker pull $ECR_REGISTRY/$ECR_REPOSITORY_AI:$TAG

echo "> 기존 컨테이너 중지 및 삭제"
docker stop spring || true
docker rm spring || true

echo "> Docker Compose 실행"
docker compose -f /home/ubuntu/memozy-back/docker-compose.yml up -d

echo "> 이미지 정리"
docker image prune -af