## Introducing State, Configuration

Until now, our Redis data service is still running within the original Pod (co-located with the original backend and frontend ui). This single, non-durable container running does not provide the durability and availability required of a real-world persistent application. Its to improve this by migrating Redis into a StatefulSet.

### Build Stateful Set

1. Create an additional empty _yml_ file named demo-ss.yml. This file will be used to deploy the Redis database as a _StatefulSet_. As before, add the api version, kind, and basic metadata to the empty yml file:

```yaml
apiVersion: apps/v1beta2
kind: StatefulSet
metadata:
  name: fortune-redis
```

2. Next, add the beginning of the spec for the _StatefulSet_. This will include the number of replicas, a label selector, the name of the service (which we'll eventually create), and the declaration of the the template and metadata for the spec:

```yaml
spec:
  replicas: 1
  selector:
    matchLabels:
      app: fortune-redis
  serviceName: fortune-redis-service
  template:
    metadata:
      name: fortune-redis
      labels:
        app: fortune-redis
        deployment: pks-workshop
```

3. Continue adding to the StatefulSet template by defining the spec for the containers that are part of the template. That following is part of the _template_ fragment in the YML document:

```yaml
spec:
  containers:
  - name: fortune-redis
    image: redis
    ports:
    - containerPort: 6379
      name: redis
```

4. The completed StatefulSet declaration should look like this:

```yaml
apiVersion: apps/v1beta2
kind: StatefulSet
metadata:
  name: fortune-redis
spec:
  replicas: 1
  selector:
    matchLabels:
      app: fortune-redis
  serviceName: fortune-redis-service
  template:
    metadata:
      name: fortune-redis
      labels:
        app: fortune-redis
        deployment: pks-workshop
    spec:
      containers:
      - name: fortune-redis
        image: redis
        ports:
        - containerPort: 6379
          name: redis

```

5. Create the API Resource Object using kubectl create:

```bash
$ kubectl create -f demo-ss.yml
statefulset "fortune-redis" created
```

### Test the Initial Redis StatefulSet

1. Since there is no service exposing Redis at this point (we'll get to that in a bit), we'll test the Redis instance by ssh'ing into the container and using the Redis CLI that is installed into the Redis container that has been started. First we need to find the name of the Pod created. Execute the following kubectl command:

```bash
$ kubectl get all -l app=fortune-redis
NAME                         DESIRED   CURRENT   AGE
statefulsets/fortune-redis   1         1         1m

NAME                 READY     STATUS    RESTARTS   AGE
po/fortune-redis-0   1/1       Running   0          1m
```

2. From this output we see that a StatefulSet has been created, named fortune-redis, along with a supporting Pod, name fortune-redis-0. SSH into the Redis container using the kubectl exec command passing it the flags to obtain and interactive shell:

```bash
$ kubectl exec -it fortune-redis-0 /bin/bash

root@fortune-redis-0:/data#
```

3. This is an interactive shell into the container running Redis. Test Redis out by connecting using the redis-cli and adding and retrieving a few pieces of data:

```bash
root@fortune-redis-0:/data# redis-cli
 127.0.0.1:6379> PING "Hello World"
 "Hello World"

 127.0.0.1:6379> SET 1 '{"id":1,"text":"This is my first fortune"}'
 OK

 127.0.0.1:6379> SET 2 '{"id":2,"text":"Things are looking up"}'
 OK

 127.0.0.1:6379> KEYS *
 1) "1"
 2) "2"
```

4. In another terminal window, use the kubectl delete command to remove the Pod. Make sure you delete the Pod and not the StatefulSet. After we execute this command we'll see that the StatefulSet automatically recreates that missing Pod. You'll also see that your shell session into the original container is terminated.

```bash
$ kubectl delete pod fortune-redis-0
pod "fortune-redis-0" deleted

$ kubectl get all -l app=fortune-redis
NAME                         DESIRED   CURRENT   AGE
statefulsets/fortune-redis   1         1         15m

NAME                 READY     STATUS    RESTARTS   AGE
po/fortune-redis-0   1/1       Running   0          12s   <-- Notice age is 12 seconds!
```

5. SSH back into Redis using the same kubectl exec command. Once connected list all the keys in the Redis server:

```bash
$ kubectl exec -it fortune-redis-0 /bin/bash

root@fortune-redis-0:/data# redis-cli

127.0.0.1:6379> KEYS *
(empty list or set)
```

6. What happened to our data? Though the StatefulSet recreated the Redis instance all resources, including disk volumes, were ephemeral. StatefulSets are responsible for always attaching the correct volumes to the containers; we just need to define them! We'll do that in the next step.

### Add a Persistent Volume to the Instance

1. Within the Spec for the StatefulSet begin defining the volumeClaimTemplates for the dynamic volume mount. The definition is comprised of the a metadata section and a spec for the volume. We'll start by creating the volumeClaimTemplates and defining the metadata section. Make sure the definition of the volumeClaimTemplates under the spec of the StatfulSet; a sibling to the _template_ and _selector_ attributes:

```yaml
volumeClaimTemplates:
- metadata:
    name: data
    labels:
      app: fortune-redis
      deployment: pks-workshop
    annotations:
      volume.beta.kubernetes.io/storage-class: standard
```

2. Next add the spec for the volume request. This attribute should be a under the metadata fragment in the volumeClaimTemplates definition:

```yaml
spec:
  accessModes: [ "ReadWriteOnce" ]
  resources:
    requests:
      storage: 5Gi
```

3. Lastly, within the original spec for the fortune-redis container add a definition for an array of volume mounts. The name _data_ corresponds to the name of the volumeClaimTemplates entry:

```yaml
volumeMounts:
- name: data
  mountPath: /redis-data
```

4. The completed StatefulSet declaration should look like this:

```yaml
apiVersion: apps/v1beta2
kind: StatefulSet
metadata:
  name: fortune-redis
spec:
  replicas: 1
  selector:
    matchLabels:
      app: fortune-redis
  serviceName: fortune-redis-service
  template:
    metadata:
      name: fortune-redis
      labels:
        app: fortune-redis
        deployment: pks-workshop
    spec:
      containers:
      - name: fortune-redis
        image: redis
        ports:
        - containerPort: 6379
          name: redis
        volumeMounts:
        - name: data
          mountPath: /redis-data
  volumeClaimTemplates:
  - metadata:
      name: data
      labels:
        app: fortune-redis
        deployment: pks-workshop
      annotations:
        volume.beta.kubernetes.io/storage-class: standard
    spec:
      accessModes: [ "ReadWriteOnce" ]
      resources:
        requests:
          storage: 5Gi
```

### Create a ConfigMap to Customize Redis Config via and Additional Volume Mount

1. Within the same yml file, create another yml directive at the bottom of the file. Within this new directive add the resource definition for a ConfigMap API object as follows:

```yaml
---
apiVersion: v1
kind: ConfigMap
metadata:
  name: redis-config
  labels:
    app: fortune-redis
    deployment: pks-workshop
```

2. Expose a file name redis.conf within the ConfigMap by defining the following data section within the ConfigMap resource definition. This file defines a few configuration options for the Redis server such as the persistent data location and the intervals at which data is written from memory to disk. This attribute should be part of the root attributes on the yml object:

```yaml
data:
  redis.conf: |
    bind 0.0.0.0
    port 6379

    dir /redis-data

    save 5 1
    save 60 3
```

3. The completed ConfigMap declaration should look like this:

```yaml
---
apiVersion: v1
kind: ConfigMap
metadata:
  name: redis-config
  labels:
    app: fortune-redis
    deployment: pks-workshop
data:
  redis.conf: |
    bind 0.0.0.0
    port 6379

    dir /redis-data

    save 5 1
    save 60 3
```

4. Moving back to the StatefulSet template, add a definition of the volumes that must be created on the stateful set Pod(s). This definition is under the _containers_ yml fragment but is a sibling to the array of defined containers:

```yaml
volumes:
- name: config
  configMap:
    name: redis-config
```

5. Add a second entry into the volumeMounts array for the fortune-redis container that refers to the volume "config" that we just defined, providing a location where the volume will be mounted in the container:

```yaml
volumeMounts:
- name: data
  mountPath: /redis-data
- name: config
  mountPath: /redis-config
```

6. Lastly, override the Redis server start command to utilize the configuration file, redis.conf, that will be located within the new config volume mounted to the container. That start command can be specified in the _command_ attribute within the container definition:

```yaml
 command: [sh, -c, redis-server /redis-config/redis.conf]
 ```

 7. The completed StatefulSet declaration should look like this:

 ```yaml
 apiVersion: apps/v1beta2
  kind: StatefulSet
  metadata:
   name: fortune-redis
  spec:
   replicas: 1
   selector:
     matchLabels:
       app: fortune-redis
   serviceName: fortune-redis-service
   template:
     metadata:
       name: fortune-redis
       labels:
         app: fortune-redis
         deployment: pks-workshop
     spec:
       containers:
       - name: fortune-redis
         image: redis
         command: [sh, -c, redis-server /redis-config/redis.conf]
         ports:
         - containerPort: 6379
           name: redis
         volumeMounts:
         - name: data
           mountPath: /redis-data
         - name: config
           mountPath: /redis-config
        volumes:
        - name: config
           configMap:
           name: redis-config
   volumeClaimTemplates:
   - metadata:
       name: data
       labels:
         app: fortune-redis
         deployment: pks-workshop
       annotations:
          volume.beta.kubernetes.io/storage-class: standard
      spec:
        accessModes: [ "ReadWriteOnce" ]
        resources:
          requests:
            storage: 5Gi
```

8. Since there are updates made to the stateful set outside the template (the volumeClaimTemplates) the update cannot simply be made using the kubectl apply. First delete the existing StatefulSet and recreate using the delete and create commands:

```bash
$ kubectl delete -f demo-ss.yml                                                                                                                 1 ↵
statefulset "fortune-redis" deleted

$ kubectl create -f demo-ss.yml
statefulset "fortune-redis" created
configmap "redis-config" created
```

### Create a Service to Expose the Redis StatefulSet

1. Within the same yml file, create another yml directive at the top of the file. Within this new directive add the resource definition for a Service API object as follows:

```yaml
apiVersion: v1
kind: Service
metadata:
  name: fortune-redis-service
---
```

2. As before, add the spec for the ports that need to exposed by our application:

```yaml
spec:
  ports:
  - port: 6379
    name: redis
```

3. Create a resource selector linking the service to the fortune-redis StatefulSet:

```yaml
selector:
  app: fortune-redis
```

4. As with the all other API objects, add a labels attribute to the metadata section of the service resource configuration:

```yaml
labels:
  app: fortune-redis-service
  deployment: pks-workshop
```

5. The completed configuration for the Service, StatefulSet, and ConfigMap API objects should appear as follows:

```yaml
apiVersion: v1
kind: Service
metadata:
  labels:
    app: fortune-redis
    deployment: pks-workshop
  name: fortune-fortune-redis-service
spec:
  ports:
  - port: 6379
    name: redis
  selector:
    app: fortune-redis
---
apiVersion: apps/v1beta2
kind: StatefulSet
metadata:
  name: fortune-redis
spec:
  replicas: 1
  selector:
    matchLabels:
      app: fortune-redis
  serviceName: fortune-redis-service
  template:
    metadata:
      name: fortune-redis
      labels:
        app: fortune-redis
        deployment: pks-workshop
    spec:
      containers:
      - name: fortune-redis
        image: redis
        command: [sh, -c, redis-server /redis-config/redis.conf]
        ports:
        - containerPort: 6379
          name: redis
        volumeMounts:
        - name: data
          mountPath: /redis-data
        - name: config
          mountPath: /redis-config
      volumes:
      - name: config
        configMap:
          name: redis-config
  volumeClaimTemplates:
  - metadata:
      name: data
      labels:
        app: fortune-redis
        deployment: pks-workshop
      annotations:
        volume.beta.kubernetes.io/storage-class: standard
    spec:
      accessModes: [ "ReadWriteOnce" ]
      resources:
        requests:
          storage: 5Gi
---
apiVersion: v1
kind: ConfigMap
metadata:
  name: redis-config
  labels:
    app: fortune-redis
    deployment: pks-workshop
data:
  redis.conf: |
    bind 0.0.0.0
    port 6379

    dir /redis-data

    save 5 1
    save 60 3
```

6. Create the API Resource Object using kubectl apply.

```bash
$ kubectl apply -f demo-ss.yml
service "fortune-redis" created
statefulset "fortune-redis" configured
configmap "redis-config" unchanged
```

### Test Data Persistence on New Container Volume Mounts

1. SSH back into Redis using the same kubectl exec command. Once connected list all the keys in the Redis to verify it is empty and a few data elements to Redis:

```bash
$ kubectl exec -it fortune-redis-0 /bin/bash

root@fortune-redis-0:/data# redis-cli

127.0.0.1:6379> KEYS *
(empty list or set)

127.0.0.1:6379> SET 1 '{"id":1,"text":"You have a bright future"}'
OK

127.0.0.1:6379> SET 2 '{"id":2,"text":"Old acquaintences will reappear"}'
OK

127.0.0.1:6379> SET 3 '{"id":3,"text":"Your kindness will pay itself tenfold"}'
OK

127.0.0.1:6379> KEYS *
1) "3"
2) "2"
3) "1"
```
2. Follow the same process as before to kill the Redis Pod. Use the kubectl delete command to remove the Pod. Make sure you delete the Pod and not the StatefulSet.

```bash
$ kubectl delete pod fortune-redis-0
pod "fortune-redis-0" deleted
```

3. SSH back into Redis using the same kubectl exec command. Once connected list all the keys in the Redis server and retrieve one of the keys. This time our data is persisted across Pod failures!

```bash
$ kubectl exec -it fortune-redis-0 /bin/bash

root@fortune-redis-0:/data# redis-cli

127.0.0.1:6379> KEYS *
1) "3"
2) "1"
3) "2"

127.0.0.1:6379> GET 3
"Your kindness will pay itself tenfold"
```
