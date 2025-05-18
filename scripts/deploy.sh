#!/bin/bash

PROJECT_NAME=memozy-back
REPOSITORY=/home/ubuntu/$PROJECT_NAME
JAR_NAME=$(ls $REPOSITORY/build/libs | grep 'SNAPSHOT.jar' | tail -n 1)
JAR_PATH=$REPOSITORY/build/libs/$JAR_NAME

cd $REPOSITORY

echo "> 현재 실행 중인 애플리케이션 PID 확인"
CURRENT_PID=$(pgrep -f $PROJECT_NAME)

if [ -z "$CURRENT_PID" ]; then
  echo "> 현재 실행 중인 애플리케이션이 없습니다."
else
  echo "> kill -15 $CURRENT_PID"
  kill -15 $CURRENT_PID
  sleep 5
fi

echo "> 새 애플리케이션 배포: $JAR_PATH"
nohup java -Djava.net.preferIPv4Stack=true -jar "$JAR_PATH" > jarExecute.log 2>&1 < /dev/null &