## Dockerize the "Articulate Application" for PKS

## Prerequisites
1. A provisioned kubernetes cluster with at least 2 worker nodes. See PKS documentation to install PKS provision a k8s cluster, and connect via `kubectl`. [PKS Documentation](https://docs.pivotal.io/runtimes/pks/1-0/)
1. Docker installed on your local machine, and an available image registry available (This example uses Docker Hub, Harbor could work too).
1. Clone the PCF Articulate sample application from [here](https://github.com/pivotal-education/pcf-articulate-code).
2. Copy the `dockerfile` from [here](/apps/articulate-docker/dockerfile) into the `pcf-articulate-code/` directory.

## Steps
1. Inspect the Dockerfile

`cat dockerfile`

2. Build the Articulate Docker image. Note: `name` refers to your Docker username.

`docker build . -t {{name}}/articulate`

3. Push the built image to your image registry.

`docker push {{name}}/articulate`

4. Deploy the image to PKS as a *Pod*.

`kubectl run --image={{name}}/articulate articulate --port=8080`

5. Expose the deployment via Kubernetes NodePort Service.

`kubectl expose deployment articulate --port=8080 --name=articulate-svc --type=NodePort`


## Clean up
1. `kubectl delete deployment articulate`
1. `kubectl delete svc articulate-svc`
