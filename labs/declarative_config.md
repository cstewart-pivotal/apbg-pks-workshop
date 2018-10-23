## Using Declarative Configuration to Deploy a Sample Application

Lets deploy the initial version of our demo application, PKS Fortune Teller. This is a 3 tier application that contains a HTML/JavaScript app served from Nginx, a Spring Boot REST backend, and a Redis persistent data service.
<img src="/images/02-1.png"  width="250" height="250">

Our app will look like this:
<img src="/images/02-2.png"  width="250" height="250">

### Create declarative resource definition for demo application
1. Create an empty _.yml_ file named `demo-pod.yml`. We will use this file to create the declarative configuration of the API objects required to deploy the demo application in a single pod.
