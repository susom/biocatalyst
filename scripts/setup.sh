#!/bin/bash -e
echo 'Building client (UI) docker container'
docker build ../src/client -t client
echo 'Building backend (Spring Boot Java services) docker conatiners'
cd ../src/integrator
gradle build
docker build . -t integrator
cd ../indexer
gradle build
docker build . -t indexer
cd ../proxy
gradle build
docker build . -t proxy
echo 'Ready to rock!'
echo 'Next steps: `cd docker` then `docker-compose up -d`'
