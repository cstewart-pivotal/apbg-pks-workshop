## Using Declarative Configuration to Deploy a Sample Application

Lets deploy the initial version of our demo application, PKS Fortune Teller. This is a 3 tier application that contains a HTML/JavaScript app served from Nginx, a Spring Boot REST backend, and a Redis persistent data service.
<img src="/images/02-1.png"  width="500" height="500">

Our app will look like this:
<img src="/images/02-2.png"  width="500" height="500">

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
