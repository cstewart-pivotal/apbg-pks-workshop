## Dockerize the "Articulate Application" for PKS

## Prerequisites
1. A provisioned kubernetes cluster with at least 2 worker nodes. See PKS documentation to install PKS provision a k8s cluster, and connect via `kubectl`. [PKS Documentation](https://docs.pivotal.io/runtimes/pks/1-0/)
1. Docker installed on your local machine, and an available image registry available (This example uses Docker Hub, Harbor could work too).
1. Clone the PCF Articulate sample application from [here](https://github.com/pivotal-education/pcf-articulate-code).
2. Copy the `dockerfile` from [here](/apps/articulate-docker/dockerfile) into the `pcf-articulate-code/` directory.

### Create Container Using Docker
1. Inspect the Dockerfile

    `cat dockerfile`

2. Build the Articulate Docker image. Note: `name` refers to your Docker username.

    `docker build . -t {{name}}/articulate`

3. Push the built image to your image registry.

    `docker push {{name}}/articulate`

### Deploy your first pod
In order to deploy our first container we must use a Pod. A Pod is a Kubernetes abstraction that represents a group of one or more application containers (such as Docker or rkt), and some shared resources for those containers.

1. Lets create our very first container application in the cluster. To do this we'll use the kubectl run command to create a single pod:

`kubectl run --image={{name}}/articulate articulate --port=8080`

2. Expose the deployment via Kubernetes NodePort Service.

`kubectl expose deployment articulate --port=8080 --name=articulate-svc --type=NodePort`

<!-- 3. You can use the kubectl CLI to retrieve details about the running pod. Open a new command window and use the _get_ and _describe_ commands to view varying levels of details about your deployed pod:

```bash
$ kubectl get pod ubuntu -o wide
NAME READY STATUS RESTARTS AGE IP NODE
ubuntu 1/1 Running 0 9m 10.200.24.3 vm-623978bf-6ccc-412e-5673-ee7fc5d3d4a1
``` -->




## Clean up
1. `kubectl delete deployment articulate`
1. `kubectl delete svc articulate-svc`
