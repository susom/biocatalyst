#!/bin/bash
echo 'Stopping all containers, if running'
docker kill indexer integrator proxy es db logstash
echo 'Cleaning up pipeline directory for any hanging logstash configuration files'
rm docker/logstash/pipelines_dir/pipeline*/*
echo 'Starting all containers'
docker-compose -f docker/docker-compose.yml up -d 
echo 'Please wait a moment (30-60 seconds) before using the app to allow everything to start up'