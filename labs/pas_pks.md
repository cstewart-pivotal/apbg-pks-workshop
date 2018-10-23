## PKS & PAS - Separate Abstractions Working in Harmony

### Goal
Many types of workloads should be deployed into Pivotal Application Service (PAS) while others should prefer PKS. Our Fortune UI application is a good example of this as it is something we may want to iterate upon adding features, and because it simply requires a web server to service status HTML and Javascript content. Lets migrate our Frontend UI into the appropriate platform. The end result will resemble the following diagram:

<img src="/images/03-1.png"  width="750" height="500">

### Prerequisites
Prior to completing this demo the following pre-reqs need to be met:

- Pivotal Application Service (PAS) deployed and bound to an externally accessible IP or load balancer. This IP or DNS is referred to as the _PAS API Endpoint_.
  - Username and Password used to log into PAS and Org/Space tenancy.
- Pivotal Container Service (PKS) deployed
    - A Kubernetes Cluster running Redis, and the Fortunes Backend.
    - This can be accomplished by following the [Using Declarative Configuration to Deploy a Sample Application](/labs/declarative_config.md) lab.

### Deploy the Frontend UI to Pivotal Application Service

1. Using the PCF CLI, login to the Pivotal Application Service (PAS) using the `PAS API Endpoint`, and your `username` and `password` as follows:

```bash
$ cf login -a api.sys.apbg.apcf.io
API endpoint: cf login -a api.sys.apbg.apcf.io

Email> cf-user

Password> *******
Authenticating...
OK

Select an org (or press enter to skip):
1. apbg-dev
2. PKS Workshop

Org> 2
Targeted org PKS Workshop

Select a space (or press enter to skip):
1. PKS 1
2. PKS 2
3. PKS 3
4. PKS 4
5. PKS 5

Space> 1
Targeted space PKS 1



API endpoint:   https://api.sys.apbg.apcf.io (API version: 2.98.0)
User:           cf-user
Org:            PKS Workshop
Space:          PKS 1
```

2. Change directories to the root of the fortune-ui application under _$WORKSHOP_ROOT/apps/fortune-ui/_ and deploy the application to PAS using cf push. When we deploy the application we will direct PAS to use the staticfile buildpack:

**Note, you will need to push with a unique route. Ie, appending your initials to the app name `fortune-ui`

```bash
$ cf push fortune-ui -m 64M -b staticfile_buildpack
Pushing app fortune-ui to org Pivotal / space development as azwickey...
Getting app info...
Updating app with these attributes...
  name:                fortune-ui
  path:                /Users/azwickey/Desktop/pks-walkthrough/apps/fortune-ui
  buildpack:           staticfile_buildpack
  disk quota:          1G
  health check type:   port
  instances:           1
  memory:              64M
  stack:               cflinuxfs2
  routes:
    fortune-ui.apps.cloud.zwickey.net

Updating app fortune-ui...
Mapping routes...
Comparing local files to remote cache...
Packaging files to upload...
Uploading files...
 3.32 KiB / 3.32 KiB [====================================================================================================================] 100.00% 1s

Waiting for API to complete processing files...

Staging app and tracing logs...
   Downloading staticfile_buildpack...
   Downloaded staticfile_buildpack
   Creating container
   Successfully created container
   Downloading app package...
   Downloaded app package (3.3K)
   -----> Staticfile Buildpack version 1.4.18
   -----> Installing nginx
          Using nginx version 1.13.6
   -----> Installing nginx 1.13.6
          Copy [/tmp/buildpacks/4e19ed4fb1dfc48806ec7fbc5df9f7df/dependencies/a212d0a2bdc205474bed1efb149a7865/nginx-1.13.6-linux-x64-b624d604.tgz]
   -----> Root folder /tmp/app
   -----> Copying project files into public
   -----> Configuring nginx
   Exit status 0
   Uploading droplet, build artifacts cache...
   Uploading build artifacts cache...
   Uploading droplet...
   Uploaded build artifacts cache (216B)
   Uploaded droplet (2.7M)

Waiting for app to start...
   Uploading complete
   Stopping instance 15963c71-35a7-442f-8ccf-1b0036dae102
   Destroying container
   Successfully destroyed container

name:              fortune-ui
requested state:   started
instances:         1/1
usage:             64M x 1 instances
routes:            fortune-ui.apps.cloud.zwickey.net
last uploaded:     Mon 29 Jan 13:07:37 EST 2018
stack:             cflinuxfs2
buildpack:         staticfile_buildpack
start command:     $HOME/boot.sh

     state     since                  cpu    memory     disk      details
#0   running   2018-01-29T18:07:49Z   0.0%   0 of 64M   0 of 1G
```

3. Open a browser and navigate to the http endpoint that is listed in the "routes" portion of the command output. Make sure you access http and not https. E.G. http://fortune-ui.apps.cloud.zwickey.net You may notice that no fortunes appear!

<img src="/images/03-2.png"  width="750" height="500">

4. Fortunes do not appear as this UI is not connecting to our backend deployed to PKS. Login into the PAS application manager UI, which is is typically deployed to https://apps.$FQDN_PAS_SYSTEM_DOMAIN and navigate to the organization and space to which the fortune-ui application was deployed and click on the application _fortune-ui_:

<img src="/images/03-3.png"  width="750" height="500">

5. Navigate to _Settings_ and Click on the _REVEAL USER PROVIDED ENV VARIABLES_ button. Add an env variable named "BACKEND" and provide the http address to the ip and port (which should be 9080) of the fortune backend in pks. E.G. http://35.227.19.28:9080. Make sure to click the save button.

<img src="/images/03-4.png"  width="750" height="500">

6. Switch back to the PCF CLI and execute the `cf restage` command on the fortune-ui application.

```bash
$ cf restage fortune-ui
Restaging app fortune-ui in org Pivotal / space development as azwickey...

Staging app and tracing logs...
   Downloading staticfile_buildpack...
   Downloaded staticfile_buildpack
   Creating container
   Successfully created container
   Downloading build artifacts cache...
   Downloading app package...
   Downloaded app package (3.3K)
   Downloaded build artifacts cache (216B)
   -----> Staticfile Buildpack version 1.4.18
   -----> Installing nginx
          Using nginx version 1.13.6
   -----> Installing nginx 1.13.6
          Copy [/tmp/buildpacks/4e19ed4fb1dfc48806ec7fbc5df9f7df/dependencies/a212d0a2bdc205474bed1efb149a7865/nginx-1.13.6-linux-x64-b624d604.tgz]
   -----> Root folder /tmp/app
   -----> Copying project files into public
   -----> Configuring nginx
   Exit status 0
   Uploading droplet, build artifacts cache...
   Uploading build artifacts cache...
   Uploading droplet...
   Uploaded build artifacts cache (220B)

Waiting for app to start...

name:              fortune-ui
requested state:   started
instances:         1/1
usage:             64M x 1 instances
routes:            fortune-ui.apps.cloud.zwickey.net
last uploaded:     Mon 29 Jan 13:07:37 EST 2018
stack:             cflinuxfs2
buildpack:         staticfile_buildpack
start command:     $HOME/boot.sh

     state     since                  cpu    memory        disk         details
#0   running   2018-01-29T18:15:02Z   0.1%   3.7M of 64M   5.8M of 1G
---------------------------------------------------------------------
```
7. Refresh the browser window running the fortunes UI application. Since the frontend is now configured via and env variable to connect to the appropriate backend, fortunes are now appearing:

<img src="/images/03-5.png"  width="750" height="500">

### Deploy the Fortune Backend as a Kubernetes Deployment

1. Create an additional empty _yml_ file named demo-deployment.yml. This file will be used to deploy the fortune backend as a _Deployment_, a Kubernetes primitive for highly available and scalable applications. As before, add the `apiVersion`, `kind`, and basic `metadata` to the empty yml file:

```yaml
apiVersion: apps/v1beta2
kind: Deployment
metadata:
  name: fortune-backend
---------------------------------------------------------------------
```

2. Next, add the beginning of the spec for the _Deployment_. This will include the number of replicas, a label selector, and the declaration of the the template and metadata for the spec:

```yaml
spec:
  replicas: 2
  selector:
    matchLabels:
      app: fortune-backend
  template:
    metadata:
      name: fortune-backend
      labels:
        app: fortune-backend
        deployment: pks-workshop

```

3. Continue adding to the Deployment template by defining the spec for the containers that are part of the deployment template. That following is part of the _template_ fragment in the YML document:

```yaml
spec:
  containers:
  - image: azwickey/fortune-backend-jee:latest
    name: fortune-backend
    ports:
    - containerPort: 9080
      protocol: TCP
```

4. As part of the container fragment, add the following section that defines how the platform determines container readiness and health:

```yaml
livenessProbe:
  httpGet:
    path: /fortune-backend-jee/
    port: 9080
  initialDelaySeconds: 5
  timeoutSeconds: 1
  periodSeconds: 10
  failureThreshold: 2
```

5. Lastly, add an environment variable that defines how the backend will connect to the redis server. This is required since the backend app and redis are no longer colocated in the same pod (and referenceable via localhost). This value references the DNS entry created in kube-DNS for the original fortune-service created during the initial pod deployment. Make sure this value is part of the first element in the _containers_ array - e.g. is a sibling to the _image_, _name_, _livenessProbe_ attributes in the data structure:

```yaml
env:
- name: REDIS_HOST
  value: "fortune-service.default.svc.cluster.local"
```

6. The completed pod declaration should look like this:

```yaml
apiVersion: apps/v1beta2
kind: Deployment
metadata:
  name: fortune-backend
spec:
  replicas: 2
  selector:
    matchLabels:
      app: fortune-backend
  template:
    metadata:
      name: fortune-backend
      labels:
        app: fortune-backend
        deployment: pks-workshop
    spec:
      containers:
      - image: azwickey/fortune-backend-jee:latest
        name: fortune-backend
        ports:
        - containerPort: 9080
          protocol: TCP
        livenessProbe:
          httpGet:
            path: /fortune-backend-jee/
            port: 9080
          initialDelaySeconds: 5
          timeoutSeconds: 1
          periodSeconds: 10
          failureThreshold: 2
        env:
        - name: REDIS_HOST
          value: "fortune-service.default.svc.cluster.local"
```

### Create a Service Resource for Routing Traffic to the Deployment Backend

1. Within the same yml file, create another yml directive at the top of the file. Within this new directive add the resource definition for a Service API object as follows:

```yaml
apiVersion: v1
kind: Service
metadata:
  name: fortune-backend-service
---
```

2. As before, add the spec for the ports that need to exposed by our application and the Service type:

```yaml
spec:
  ports:
  - port: 9080
    name: backend
  type: LoadBalancer
```

3. Create a resource selector linking the service to the fortune-backend deployment:

```yaml
selector:
  app: fortune-backend
```

4. As with the all other API objects, add a labels attribute to the metadata section of the service resource configuration:


```yaml
labels:
  app: fortune-backend-service
  deployment: pks-workshop
```

5. The completed configuration for the API objects should appear as follows:

```yaml
apiVersion: v1
kind: Service
metadata:
  name: fortune-backend-service
  labels:
    app: fortune-backend-service
    deployment: pks-workshop
spec:
  ports:
  - port: 9080
    name: backend
  type: LoadBalancer
  selector:
    app: fortune-backend
---
apiVersion: apps/v1beta2
kind: Deployment
metadata:
  name: fortune-backend
spec:
  replicas: 2
  selector:
    matchLabels:
      app: fortune-backend
  template:
    metadata:
      name: fortune-backend
      labels:
        app: fortune-backend
        deployment: pks-workshop
    spec:
      containers:
      - image: azwickey/fortune-backend-jee:latest
        name: fortune-backend
        ports:
        - containerPort: 9080
          protocol: TCP
        livenessProbe:
          httpGet:
            path: /fortune-backend-jee/
            port: 9080
          initialDelaySeconds: 5
          timeoutSeconds: 1
          periodSeconds: 10
          failureThreshold: 2
        env:
        - name: REDIS_HOST
          value: "fortune-service.default.svc.cluster.local"

```

6. Open a command window and _watch_ the kubectl _get_ command, if not still running from the previous demo. Use the labels we attached to the resources earlier as filters so only the resources associated with the demo application appear:

```bash
watch kubectl get all -l deployment=pks-workshop --show-labels
```

7. Deploy the API objects to your Kubernetes cluster using the kubectl _create_ command, using the declarative configuration you just created:

```bash
$ kubectl create -f demo-deployment.yml
service "fortune-backend-service" created
deployment "fortune-backend" created
```

8. Inspect the output of your watch of the kubectl get command. You'll see the newly deployed Deployment, associated Pods, and Service appear and startup. Take note of the external IP address that is assigned to the new fortune-backend-service as that can be used to access the backend.

```bash
Every 2.0s: kubectl get all -l deployment=pks-workshop --show-labels                                                          Mon Jan 29 15:34:40 2018

NAME                     DESIRED   CURRENT   UP-TO-DATE   AVAILABLE   AGE       LABELS
deploy/fortune-backend   2         2         2            2           2m        app=fortune-backend,deployment=pks-workshop

NAME                            DESIRED   CURRENT   READY     AGE       LABELS
rs/fortune-backend-7c54577f6c   2         2         2         2m        app=fortune-backend,deployment=pks-workshop,pod-template-hash=3710133927

NAME                     DESIRED   CURRENT   UP-TO-DATE   AVAILABLE   AGE       LABELS
deploy/fortune-backend   2         2         2            2           2m        app=fortune-backend,deployment=pks-workshop

NAME                            DESIRED   CURRENT   READY     AGE       LABELS
rs/fortune-backend-7c54577f6c   2         2         2         2m        app=fortune-backend,deployment=pks-workshop,pod-template-hash=3710133927

NAME                                  READY     STATUS    RESTARTS   AGE       LABELS
po/fortune                            3/3       Running   0          5h        app=fortune,deployment=pks-workshop
po/fortune-backend-7c54577f6c-smdqs   1/1       Running   0          2m        app=fortune-backend,deployment=pks-workshop,pod-template-hash=371013392
7
po/fortune-backend-7c54577f6c-wmxv7   1/1       Running   0          2m        app=fortune-backend,deployment=pks-workshop,pod-template-hash=371013392
7

NAME                          TYPE           CLUSTER-IP       EXTERNAL-IP     PORT(S)                                      AGE       LABELS
svc/fortune-backend-service   LoadBalancer   10.100.200.113   35.190.163.92   9080:31848/TCP                               2m        app=fortune-backe
nd-service,deployment=pks-workshop
svc/fortune-service           LoadBalancer   10.100.200.11    35.227.19.28    80:30312/TCP,9080:31274/TCP,6379:32468/TCP   5h        app=fortune-servi
ce,deployment=pks-workshop

```
9. Test the new backend using a web browser or curl just as before. This time, use the external IP address assigned to the fortune-backend-service. E.G. http://35.190.163.92:9080/fortune-backend-jee/app/fortune/all

```bash
$ curl -v http://35.190.163.92:9080/fortune-backend-jee/app/fortune/all
 *   Trying 35.190.163.92...
 * TCP_NODELAY set
 * Connected to 35.190.163.92 (35.190.163.92) port 9080 (#0)
 > GET /fortune-backend-jee/app/fortune/all HTTP/1.1
 > Host: 35.190.163.92:9080
 > User-Agent: curl/7.54.0
 > Accept: */*
 >
 < HTTP/1.1 200 OK
 < X-Powered-By: Servlet/3.1
 < Content-Type: application/json
 < Date: Mon, 29 Jan 2018 20:37:32 GMT
 < Content-Language: en-US
 < Content-Length: 160
 <
 * Connection #0 to host 35.190.163.92 left intact
 [{"id":1,"text":"Life is like a box of chocolates"},{"id":2,"text":"YOLO, go for it!!!"},{"id":3,"text":"You will be presented with an intriguing opportunity"}]%

```

10. Using the PCF CLI or Pivotal Apps Manager UI, update the BACKEND environment variable bound to the frontend-ui application deployed to PAS to reflect the new location of the fortune-backend-service (the external IP used above).

```bash
$ cf set-env fortune-ui BACKEND http://35.190.163.92:9080
Setting env variable 'BACKEND' to 'http://35.190.163.92:9080' for app fortune-ui in org Pivotal / space development as azwickey...
OK
TIP: Use 'cf restage fortune-ui' to ensure your env variable changes take effect
```

11. As the command output advises, `cf restage` must be executed for this change to take affect.

```
$ cf restage fortune-ui
Restaging app fortune-ui in org Pivotal / space development as azwickey...

Staging app and tracing logs...
   Downloading staticfile_buildpack...
   Downloaded staticfile_buildpack
   Creating container
   Successfully created container
   Downloading build artifacts cache...
   Downloading app package...
   Downloaded app package (3.3K)
   Downloaded build artifacts cache (223B)
   -----> Staticfile Buildpack version 1.4.18
   -----> Installing nginx
          Using nginx version 1.13.6
   -----> Installing nginx 1.13.6
          Copy [/tmp/buildpacks/4e19ed4fb1dfc48806ec7fbc5df9f7df/dependencies/a212d0a2bdc205474bed1efb149a7865/nginx-1.13.6-linux-x64-b624d604.tgz]
   -----> Root folder /tmp/app
   -----> Copying project files into public
   -----> Configuring nginx
   Exit status 0
   Uploading droplet, build artifacts cache...
   Uploading build artifacts cache...
   Uploading droplet...
   Uploaded build artifacts cache (222B)
   Uploaded droplet (2.7M)

Waiting for app to start...
   Uploading complete
   Stopping instance cf07c972-db11-43b3-bce4-a0e94b40148b
   Destroying container
   Successfully destroyed container

name:              fortune-ui
requested state:   started
instances:         1/1
usage:             64M x 1 instances
routes:            fortune-ui.apps.cloud.zwickey.net
last uploaded:     Mon 29 Jan 13:07:37 EST 2018
stack:             cflinuxfs2
buildpack:         staticfile_buildpack
start command:     $HOME/boot.sh

     state     since                  cpu    memory        disk         details
#0   running   2018-01-29T20:42:12Z   0.0%   3.8M of 64M   5.8M of 1G
```

12. Refresh the browser window running the fortunes UI application. Fortunes should now be served using the new backend.

<img src="/images/03-51.png"  width="750" height="500">

### Change Compute Resources Allocated to Fortune-Backend Application

1. Update the deployment definition with demo-deployment.yml with an addition to the container spec adding a resource request and limit section. We'll request 1GB of memory and .5 cpu and limit our container so as to not use more than 2GBs of memory and 1 cpu. As with the env var, make sure this value is part of the first element in the _containers_ array - e.g. is a sibling to the _image_, _name_, _livenessProbe_ attributes in the data structure:

```yaml
resources:
  requests:
    memory: "1G"
    cpu: "0.5"
  limits:
    memory: "2G"
    cpu: "1.0"
```

2. The completed configuration for the Deployment API object should appear as follows:

```yaml
apiVersion: apps/v1beta2
kind: Deployment
metadata:
  name: fortune-backend
spec:
  replicas: 2
  selector:
    matchLabels:
      app: fortune-backend
  template:
    metadata:
      name: fortune-backend
      labels:
        app: fortune-backend
        deployment: pks-workshop
    spec:
      containers:
      - image: azwickey/fortune-backend-jee:latest
        name: fortune-backend
        ports:
        - containerPort: 9080
          protocol: TCP
        livenessProbe:
          httpGet:
            path: /fortune-backend-jee/
            port: 9080
          initialDelaySeconds: 5
          timeoutSeconds: 1
          periodSeconds: 10
          failureThreshold: 2
        env:
        - name: REDIS_HOST
          value: "fortune-service.default.svc.cluster.local"
        resources:
          requests:
            memory: "1G"
            cpu: "0.5"
          limits:
            memory: "2G"
            cpu: "1.0"
```

3. The deployment API object already exists within the Kubernetes cluster. Use the kubectl _apply_ command to update the existing objects, passing in the yml description of the api objects:

```bash
$ kubectl apply -f demo-deployment.yml                                     
service "fortune-backend-service" unchanged
deployment "fortune-backend" configured
```

4. Kubernetes will create 2 new pods governed by the new resource locations and destroy the old pods. This can be seen by viewing the age of the fortune-backend pods displayed in the output from our watch command on kubectl, which now indicate they are only 1 min old:

```bash
Every 2.0s: kubectl get all -l deployment=pks-workshop --show-labels                                                          Mon Jan 29 16:08:25 2018

 NAME                     DESIRED   CURRENT   UP-TO-DATE   AVAILABLE   AGE       LABELS
 deploy/fortune-backend   2         2         2            2           35m       app=fortune-backend,deployment=pks-workshop

 NAME                            DESIRED   CURRENT   READY     AGE       LABELS
 rs/fortune-backend-5cc4b6dc6    2         2         2         9m        app=fortune-backend,deployment=pks-workshop,pod-template-hash=177062872
 rs/fortune-backend-7c54577f6c   0         0         0         35m       app=fortune-backend,deployment=pks-workshop,pod-template-hash=3710133927
 rs/fortune-backend-7c87696b97   0         0         0         11m       app=fortune-backend,deployment=pks-workshop,pod-template-hash=3743252653

 NAME                     DESIRED   CURRENT   UP-TO-DATE   AVAILABLE   AGE       LABELS
 deploy/fortune-backend   2         2         2            2           35m       app=fortune-backend,deployment=pks-workshop

 NAME                            DESIRED   CURRENT   READY     AGE       LABELS
 rs/fortune-backend-5cc4b6dc6    2         2         2         9m        app=fortune-backend,deployment=pks-workshop,pod-template-hash=177062872
 rs/fortune-backend-7c54577f6c   0         0         0         35m       app=fortune-backend,deployment=pks-workshop,pod-template-hash=3710133927
 rs/fortune-backend-7c87696b97   0         0         0         11m       app=fortune-backend,deployment=pks-workshop,pod-template-hash=3743252653

 NAME                                 READY     STATUS    RESTARTS   AGE       LABELS
 po/fortune                           3/3       Running   0          5h        app=fortune,deployment=pks-workshop
 po/fortune-backend-5cc4b6dc6-nhx7p   1/1       Running   0          1m        app=fortune-backend,deployment=pks-workshop,pod-template-hash=177062872
 po/fortune-backend-5cc4b6dc6-vsb22   1/1       Running   0          1m        app=fortune-backend,deployment=pks-workshop,pod-template-hash=177062872

 NAME                          TYPE           CLUSTER-IP       EXTERNAL-IP     PORT(S)                                      AGE       LABELS
 svc/fortune-backend-service   LoadBalancer   10.100.200.113   35.190.163.92   9080:31848/TCP                               35m       app=fortune-backe
 nd-service,deployment=pks-workshop
 svc/fortune-service           LoadBalancer   10.100.200.11    35.227.19.28    80:30312/TCP,9080:31274/TCP,6379:32468/TCP   5h        app=fortune-servi
 ce,deployment=pks-workshop
```
