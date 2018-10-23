#!/usr/bin/env bash

docker build -t azwickey/fortune-backend .
docker push azwickey/fortune-backend:latest
