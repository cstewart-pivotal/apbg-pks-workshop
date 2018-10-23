## PKS & PAS - Separate Abstractions Working in Harmony

### Goal
Many types of workloads should be deployed into Pivotal Application Service (PAS) while others should prefer PKS. Our Fortune UI application is a good example of this as it is something we may want to iterate upon adding features, and because it simply requires a web server to service status HTML and Javascript content. Lets migrate our Frontend UI into the appropriate platform. The end result will resemble the following diagram:

<img src="/images/03-1.png"  width="1000" height="500">

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
$ cf login -a api.sys.apbg.apcf.io --skip-ssl-validation
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

2. 
