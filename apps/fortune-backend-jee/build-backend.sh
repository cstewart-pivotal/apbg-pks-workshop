#!/usr/bin/env bash

docker build -t azwickey/fortune-backend-jee .
docker push azwickey/fortune-backend-jee:latest
