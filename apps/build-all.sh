#!/usr/bin/env bash

#handle backend
cd fortune-backend
./build-backend.sh
#handle backend-jee
cd ../fortune-backend-jee/
./build-backend.sh
cd ../fortune-ui
#handle frontend
./build-ui.sh
