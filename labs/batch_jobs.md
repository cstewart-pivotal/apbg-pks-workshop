## Batch Jobs


Now that an independently scalable and durable Redis server is running, we'll utilize a batch job to bootstrap the cluster with preloaded data. We'll also update the Fortune Backend to use the new Redis Server.

### Update the Fortune Backend Application to use the Redis StatefulSet

1. Update the REDIS_HOST environment variable that defines the location of the backend's redis server using the kubectl set env command. Use the value of the service created for the stateful set, fortune-redis-service:

```bash
$ kubectl set env deployment/fortune-backend REDIS_HOST=fortune-redis-service.default.svc.cluster.local
deployment "fortune-backend" env updated
```

2. This will automatically trigger an update to the Fortune Backend deployment and cause all Pods to be recreated. This can be observed by retrieving the resource associated with the fortune-backend tag and inspecting the Age value:

```bash
$ kubectl get all -l app=fortune-backend
 NAME                     DESIRED   CURRENT   UP-TO-DATE   AVAILABLE   AGE
 deploy/fortune-backend   2         2         2            2           1d

 NAME                           DESIRED   CURRENT   READY     AGE
 rs/fortune-backend-5cc4b6dc6   0         0         0         1d
 rs/fortune-backend-684b5c4b8   0         0         0         6h
 rs/fortune-backend-974575598   2         2         2         43m

 NAME                     DESIRED   CURRENT   UP-TO-DATE   AVAILABLE   AGE
 deploy/fortune-backend   2         2         2            2           1d

 NAME                           DESIRED   CURRENT   READY     AGE
 rs/fortune-backend-5cc4b6dc6   0         0         0         1d
 rs/fortune-backend-684b5c4b8   0         0         0         6h
 rs/fortune-backend-974575598   2         2         2         43m

 NAME                                 READY     STATUS    RESTARTS   AGE
 po/fortune-backend-974575598-64sk7   1/1       Running   0          3s
 po/fortune-backend-974575598-dzp55   1/1       Running   0          5s
```

### Create Job to Load data into Redis

1. Create an additional empty _yml_ file named demo-job.yml. This file will be used to create a Job that will execute once to load a dataset into Redis. As before, add the api version, kind, and basic metadata to the empty yml file:

```yaml
apiVersion: batch/v1
kind: Job
metadata:
  name: fortune-bootstrap
```

2. The main content of a the Job definition is the spec which contains for the containers to be created. Add the initial spec and template definition (which also contains a sub-spec):

```yaml
spec:
  template:
    metadata:
      name: fortune-bootstrap
    spec:
      restartPolicy: OnFailure
```

3. The template spec section defines containers to create within the job. Add the containers definition under the restartPolicy as a sibling:

```yaml
containers:
- name: bootstrap
  image: azwickey/fortune-backend:latest
  imagePullPolicy: Always
  command:
  - /bin/ash
  - /config/import.sh
```

4. Add the same same labels to the metadata section of both the Job metadata and the spec template metadata:

```yaml
labels:
  app: fortune-bootstrap
  deployment: pks-workshop
```

5. The job config is injected via a ConfigMap. Within the same yml file, create another yml directive at the bottom of the file. Within this new directive add the resource definition for a ConfigMap API object as follows:

```yaml
---
apiVersion: v1
kind: ConfigMap
metadata:
  name: fortune-bootstrap
  labels:
    app: fortune-bootstrap
    deployment: pks-workshop
```

6. Expose a file name import.sh within the ConfigMap by defining the following data section within the ConfigMap resource definition:

```yaml
data:
  import.sh: |
    #!/bin/ash

    curl -X PUT -H "Content-Type: application/json" -d '{"text":"People are naturally attracted to you."}' http://fortune-backend-service:9080/fortune-backend-jee/app/fortune
    curl -X PUT -H "Content-Type: application/json" -d '{"text":"You learn from your mistakes... You will learn a lot today."}' http://fortune-backend-service:9080/fortune-backend-jee/app/fortune
    curl -X PUT -H "Content-Type: application/json" -d '{"text":"If you have something good in your life, do not let it go!"}' http://fortune-backend-service:9080/fortune-backend-jee/app/fortune
    curl -X PUT -H "Content-Type: application/json" -d '{"text":"What ever your goal is in life, embrace it visualize it, and for it will be yours."}' http://fortune-backend-service:9080/fortune-backend-jee/app/fortune
    curl -X PUT -H "Content-Type: application/json" -d '{"text":"Your shoes will make you happy today."}' http://fortune-backend-service:9080/fortune-backend-jee/app/fortune
    curl -X PUT -H "Content-Type: application/json" -d '{"text":"You cannot love life until you live the life you love."}' http://fortune-backend-service:9080/fortune-backend-jee/app/fortune
    curl -X PUT -H "Content-Type: application/json" -d '{"text":"Be on the lookout for coming events; They cast their shadows beforehand."}' http://fortune-backend-service:9080/fortune-backend-jee/app/fortune
    curl -X PUT -H "Content-Type: application/json" -d '{"text":"Land is always on the mind of a flying bird."}' http://fortune-backend-service:9080/fortune-backend-jee/app/fortune
    curl -X PUT -H "Content-Type: application/json" -d '{"text":"The man or woman you desire feels the same about you."}' http://fortune-backend-service:9080/fortune-backend-jee/app/fortune
    curl -X PUT -H "Content-Type: application/json" -d '{"text":"Meeting adversity well is the source of your strength."}' http://fortune-backend-service:9080/fortune-backend-jee/app/fortune
    curl -X PUT -H "Content-Type: application/json" -d '{"text":"A dream you have will come true."}' http://fortune-backend-service:9080/fortune-backend-jee/app/fortune
    curl -X PUT -H "Content-Type: application/json" -d '{"text":"Our deeds determine us, as much as we determine our deeds."}' http://fortune-backend-service:9080/fortune-backend-jee/app/fortune            curl -X PUT -H "Content-Type: application/json" -d '{"text":"Never give up. You are not a failure if you do not give up."}' http://fortune-backend-service:9080/fortune-backend-jee/app/fortune
    curl -X PUT -H "Content-Type: application/json" -d '{"text":"You will become great if you believe in yourself."}' http://fortune-backend-service:9080/fortune-backend-jee/app/fortune
    curl -X PUT -H "Content-Type: application/json" -d '{"text":"There is no greater pleasure than seeing your loved ones prosper."}' http://fortune-backend-service:9080/fortune-backend-jee/app/fortune
    curl -X PUT -H "Content-Type: application/json" -d '{"text":"You will marry your lover."}' http://fortune-backend-service:9080/fortune-backend-jee/app/fortune
    curl -X PUT -H "Content-Type: application/json" -d '{"text":"A very attractive person has a message for you."}' http://fortune-backend-service:9080/fortune-backend-jee/app/fortune
```

7. The completed ConfigMap declaration should look like this:

```yaml
---
apiVersion: v1
kind: ConfigMap
metadata:
  name: fortune-bootstrap
  labels:
    app: fortune-bootstrap
    deployment: pks-workshop
  data:
    import.sh: |
      #!/bin/ash

      curl -X PUT -H "Content-Type: application/json" -d '{"text":"People are naturally attracted to you."}' http://fortune-backend-service:9080/fortune-backend-jee/app/fortune
      curl -X PUT -H "Content-Type: application/json" -d '{"text":"You learn from your mistakes... You will learn a lot today."}' http://fortune-backend-service:9080/fortune-backend-jee/app/fortune
      curl -X PUT -H "Content-Type: application/json" -d '{"text":"If you have something good in your life, do not let it go!"}' http://fortune-backend-service:9080/fortune-backend-jee/app/fortune
      curl -X PUT -H "Content-Type: application/json" -d '{"text":"What ever your goal is in life, embrace it visualize it, and for it will be yours."}' http://fortune-backend-service:9080/fortune-backend-jee/app/fortune              curl -X PUT -H "Content-Type: application/json" -d '{"text":"Your shoes will make you happy today."}' http://fortune-backend-service:9080/fortune-backend-jee/app/fortune
      curl -X PUT -H "Content-Type: application/json" -d '{"text":"You cannot love life until you live the life you love."}' http://fortune-backend-service:9080/fortune-backend-jee/app/fortune
      curl -X PUT -H "Content-Type: application/json" -d '{"text":"Be on the lookout for coming events; They cast their shadows beforehand."}' http://fortune-backend-service:9080/fortune-backend-jee/app/fortune
      curl -X PUT -H "Content-Type: application/json" -d '{"text":"Land is always on the mind of a flying bird."}' http://fortune-backend-service:9080/fortune-backend-jee/app/fortune
      curl -X PUT -H "Content-Type: application/json" -d '{"text":"The man or woman you desire feels the same about you."}' http://fortune-backend-service:9080/fortune-backend-jee/app/fortune
      curl -X PUT -H "Content-Type: application/json" -d '{"text":"Meeting adversity well is the source of your strength."}' http://fortune-backend-service:9080/fortune-backend-jee/app/fortune
      curl -X PUT -H "Content-Type: application/json" -d '{"text":"A dream you have will come true."}' http://fortune-backend-service:9080/fortune-backend-jee/app/fortune              curl -X PUT -H "Content-Type: application/json" -d '{"text":"Our deeds determine us, as much as we determine our deeds."}' http://fortune-backend-service:9080/fortune-backend-jee/app/fortune
      curl -X PUT -H "Content-Type: application/json" -d '{"text":"Never give up. You are not a failure if you do not give up."}' http://fortune-backend-service:9080/fortune-backend-jee/app/fortune
      curl -X PUT -H "Content-Type: application/json" -d '{"text":"You will become great if you believe in yourself."}' http://fortune-backend-service:9080/fortune-backend-jee/app/fortune
      curl -X PUT -H "Content-Type: application/json" -d '{"text":"There is no greater pleasure than seeing your loved ones prosper."}' http://fortune-backend-service:9080/fortune-backend-jee/app/fortune
      curl -X PUT -H "Content-Type: application/json" -d '{"text":"You will marry your lover."}' http://fortune-backend-service:9080/fortune-backend-jee/app/fortune
      curl -X PUT -H "Content-Type: application/json" -d '{"text":"A very attractive person has a message for you."}' http://fortune-backend-service:9080/fortune-backend-jee/app/fortune
```

8. This config file needs to be added to our container. Within the container definition section of the template spec for the Job add a definition of the volumes that must be created. This definition is under the _containers_ yml fragment but is a sibling to the array of defined containers:

```yaml
volumes:
- name: config
  configMap:
    name: fortune-bootstrap
```

9. Add a volumeMounts array within the array element for the defined container. This refers to the volume "config" that we just defined, providing a location where the volume will be mounted in the container:

```yaml
volumeMounts:
- name: config
  mountPath: /config
```

10. The full resource configuration for the Job and ConfigMap should appear as follows:

```yaml
apiVersion: batch/v1
kind: Job
metadata:
  name: fortune-bootstrap
  labels:
    app: fortune-bootstrap
    deployment: pks-workshop
spec:
  template:
    metadata:
      name: fortune-bootstrap
      labels:
        app: fortune-bootstrap
        deployment: pks-workshop
    spec:
      restartPolicy: OnFailure
      containers:
      - name: bootstrap
        image: azwickey/fortune-backend:latest
        imagePullPolicy: Always
        command:
        - /bin/ash
        - /config/import.sh
        volumeMounts:
        - name: config
          mountPath: /config
      volumes:
      - name: config
        configMap:
          name: fortune-bootstrap
---
apiVersion: v1
kind: ConfigMap
metadata:
  name: fortune-bootstrap
  labels:
    app: fortune-bootstrap
    deployment: pks-workshop
data:
  import.sh: |
    #!/bin/ash

    curl -X PUT -H "Content-Type: application/json" -d '{"text":"People are naturally attracted to you."}' http://fortune-backend-service:9080/fortune-backend-jee/app/fortune
    curl -X PUT -H "Content-Type: application/json" -d '{"text":"You learn from your mistakes... You will learn a lot today."}' http://fortune-backend-service:9080/fortune-backend-jee/app/fortune
    curl -X PUT -H "Content-Type: application/json" -d '{"text":"If you have something good in your life, do not let it go!"}' http://fortune-backend-service:9080/fortune-backend-jee/app/fortune
    curl -X PUT -H "Content-Type: application/json" -d '{"text":"What ever your goal is in life, embrace it visualize it, and for it will be yours."}' http://fortune-backend-service:9080/fortune-backend-jee/app/fortune
    curl -X PUT -H "Content-Type: application/json" -d '{"text":"Your shoes will make you happy today."}' http://fortune-backend-service:9080/fortune-backend-jee/app/fortune
    curl -X PUT -H "Content-Type: application/json" -d '{"text":"You cannot love life until you live the life you love."}' http://fortune-backend-service:9080/fortune-backend-jee/app/fortune
    curl -X PUT -H "Content-Type: application/json" -d '{"text":"Be on the lookout for coming events; They cast their shadows beforehand."}' http://fortune-backend-service:9080/fortune-backend-jee/app/fortune
    curl -X PUT -H "Content-Type: application/json" -d '{"text":"Land is always on the mind of a flying bird."}' http://fortune-backend-service:9080/fortune-backend-jee/app/fortune
    curl -X PUT -H "Content-Type: application/json" -d '{"text":"The man or woman you desire feels the same about you."}' http://fortune-backend-service:9080/fortune-backend-jee/app/fortune
    curl -X PUT -H "Content-Type: application/json" -d '{"text":"Meeting adversity well is the source of your strength."}' http://fortune-backend-service:9080/fortune-backend-jee/app/fortune
    curl -X PUT -H "Content-Type: application/json" -d '{"text":"A dream you have will come true."}' http://fortune-backend-service:9080/fortune-backend-jee/app/fortune
    curl -X PUT -H "Content-Type: application/json" -d '{"text":"Our deeds determine us, as much as we determine our deeds."}' http://fortune-backend-service:9080/fortune-backend-jee/app/fortune
    curl -X PUT -H "Content-Type: application/json" -d '{"text":"Never give up. You are not a failure if you do not give up."}' http://fortune-backend-service:9080/fortune-backend-jee/app/fortune
    curl -X PUT -H "Content-Type: application/json" -d '{"text":"You will become great if you believe in yourself."}' http://fortune-backend-service:9080/fortune-backend-jee/app/fortune
    curl -X PUT -H "Content-Type: application/json" -d '{"text":"There is no greater pleasure than seeing your loved ones prosper."}' http://fortune-backend-service:9080/fortune-backend-jee/app/fortune
    curl -X PUT -H "Content-Type: application/json" -d '{"text":"You will marry your lover."}' http://fortune-backend-service:9080/fortune-backend-jee/app/fortune
    curl -X PUT -H "Content-Type: application/json" -d '{"text":"A very attractive person has a message for you."}' http://fortune-backend-service:9080/fortune-backend-jee/app/fortune
```

11. Create and execute the batch job by using the kubectl create command:

```bash
$ kubectl create -f demo-job.yml
job "fortune-bootstrap" created
configmap "fortune-bootstrap" created
```

12. The job should run fairly quickly. You can view the status and successful completion using the kubectl describe job command:

```bash
$ kubectl describe job -l app=fortune-bootstrap
Name:           fortune-bootstrap
Namespace:      default
Selector:       controller-uid=1fdc7912-061c-11e8-accf-42010a00100a
Labels:         app=fortune-bootstrap
                deployment=pks-workshop
Annotations:    <none>
Parallelism:    1
Completions:    1
Start Time:     Tue, 30 Jan 2018 19:17:29 -0500
Pods Statuses:  0 Running / 1 Succeeded / 0 Failed
Pod Template:
  Labels:  app=fortune-bootstrap
           controller-uid=1fdc7912-061c-11e8-accf-42010a00100a
           deployment=pks-workshop
           job-name=fortune-bootstrap
  Containers:
   bootstrap:
    Image:  azwickey/fortune-backend:latest
    Port:   <none>
    Command:
      /bin/ash
      /config/import.sh
    Environment:  <none>
    Mounts:
      /config from config (rw)
  Volumes:
   config:
    Type:      ConfigMap (a volume populated by a ConfigMap)
    Name:      fortune-bootstrap
    Optional:  false
Events:
  Type    Reason            Age   From            Message
  ----    ------            ----  ----            -------
  Normal  SuccessfulCreate  2m    job-controller  Created pod: fortune-bootstrap-r6n8v
```

13. Lastly, refresh your Fortune UI and view the list of all fortunes. You should see a longer list of preloaded data:

<img src="/images/05-1.png"  width="750" height="500">
