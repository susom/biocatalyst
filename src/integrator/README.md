##Local development
`gradle bootRun` for local development

##Environment deployment
`gradle docker` to publish a docker image locally (this can later be sent to a container registry)

then, restart docker-compose in the scripts/docker/ directory:
`docker kill integrator`
`docker-compose up -d integrator`

You may have to update either docker.properties or local.properties depending on your desired setup