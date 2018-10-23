## Using Declarative Configuration to Deploy a Sample Application

Lets deploy the initial version of our demo application, PKS Fortune Teller. This is a 3 tier application that contains a HTML/JavaScript app served from Nginx, a Spring Boot REST backend, and a Redis persistent data service.
<img src="/images/02-1.png"  width="1000" height="500">

Our app will look like this:
<img src="/images/02-2.png"  width="1000" height="500">

### Create declarative resource definition for demo application
1. Create an empty _.yml_ file named `demo-pod.yml`. We will use this file to create the declarative configuration of the API objects required to deploy the demo application in a single pod.

2. Add the resource declaration to the yml file for a Pod API abject as follows:
```
  apiVersion: v1
  kind: Pod
  metadata:
    name: fortune
```

3. Next, on the line beneath the _metadata_ definition add the spec for the 3 containers within our application. The _containers_ element is an array. Each container should define the image, a name for the container within the pod, and a list of ports to be exposed from the container
```
spec:
  containers:
  - image: azwickey/fortune-ui:latest
    name: fortune-ui
    ports:
    - containerPort: 80
      protocol: TCP
  - image: azwickey/fortune-backend-jee:latest
    name: fortune-backend
    ports:
    - containerPort: 9080
      protocol: TCP
  - image: redis
    name: redis
    ports:
    - containerPort: 6379
      protocol: TCP
```

4. Lastly, add the _labels_ attribute to the metadata section of the resource configuration. We'll add an "app" and "deployment" label. Make sure this is a child of the metadata attribute
```
labels:
    app: fortune
    deployment: pks-workshop
```

5. The completed pod declaration should look like this:

```
apiVersion: v1
kind: Pod
metadata:
  labels:
    app: fortune
    deployment: pks-workshop
  name: fortune
spec:
  containers:
  - image: azwickey/fortune-ui:latest
    name: fortune-ui
    ports:
    - containerPort: 80
      protocol: TCP
  - image: azwickey/fortune-backend-jee:latest
    name: fortune-backend
    ports:
    - containerPort: 9080
      protocol: TCP
  - image: redis
    name: redis
    ports:
    - containerPort: 6379
      protocol: TCP
```

### Create a service resource for routing external traffic

1. Within the same yml file, create another yml directive at the top of the file. Yml directives are separated by 3 dashes `---`. Within this new directive add the resource definition for a Service API object as follows:
```
apiVersion: v1
kind: Service
metadata:
    name: fortune-service
---
```

2. Next, on the line beneath the _metadata_ element add the spec for the ports that need to exposed by the application containers. The _ports_ element is an array.

- The service spec requires a service type. We'll use the type LoadBalancer since we want to expose our application externally

```
spec:
  ports:
  - port: 80
    name: ui
  - port: 9080
    name: backend
  - port: 6379
    name: redis
  type: LoadBalancer
```

3. We need to create a resource selector in order to determine which pods to link to the service. This is where the label we applied to the pod resource object comes into play. We'll create a selector to select any pod that has the label _app: fortune_. Add this selector as the last attribute of the Service _spec_, right under the type attribute.

```
selector:
  app: fortune
```

4. As with the pod API object, add a _labels_ attribute to the metadata section of the service resource configuration. We'll add the same labels "app" and "deployment", but use a different value for the app label to differentiate our pod and our service.

```
labels:
    app: fortune-service
    deployment: pks-workshop
```

5. The completed configuration for the Pod and Service API objects should appear as follows:

```
apiVersion: v1
kind: Service
metadata:
  labels:
    app: fortune-service
    deployment: pks-workshop
  name: fortune-service
spec:
  ports:
  - port: 80
    name: ui
  - port: 9080
    name: backend
  - port: 6379
    name: redis
  type: LoadBalancer
  selector:
    app: fortune
---
apiVersion: v1
kind: Pod
metadata:
  labels:
    app: fortune
    deployment: pks-workshop
  name: fortune
spec:
  containers:
  - image: azwickey/fortune-ui:latest
    name: fortune-ui
    ports:
    - containerPort: 80
      protocol: TCP
  - image: azwickey/fortune-backend-jee:latest
    name: fortune-backend
    ports:
    - containerPort: 9080
      protocol: TCP
  - image: redis
    name: redis
    ports:
    - containerPort: 6379
      protocol: TCP
```

### Deploy the demo application
