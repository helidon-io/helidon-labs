# Helidon Labs Incubator - Metrics for Virtual Threads

Starting in Java 24, a new management bean [`VirtualThreadSchedulerMXBean`](https://download.java.net/java/early_access/jdk24/docs/api/jdk.management/jdk/management/VirtualThreadSchedulerMXBean.html) makes some statistics related to virtual threads available programmatically.

This Helidon Labs incubating component is a library which, when added as a dependency to a Helidon application, exposes several new Helidon built-in meters in the `base` scope derived from the values from `VirtualThreadSchedulerMXBean`. 

This component works with Helidon 4.x applications, but only if you build and run this library and your application with Java 24. 

## Build and use the component

### Build the library:
Make sure to use JDK 24 or the build will fail.
```shell
mvn clean install
```

### Modify the app to use the component
Edit your project's `pom.xml` file to add a dependency on this component:
```xml
<dependency>
    <groupId>io.helidon.labs.incubator</groupId>
    <artifactId>helidon-labs-incubator-virtual-threads-metrics</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```
For example, you can add the above dependency to your application or to the [Helidon SE QuickStart](https://github.com/helidon-io/helidon-examples/tree/helidon-4.x/examples/quickstarts/helidon-quickstart-se) or [Helidon MP QuickStart](https://github.com/helidon-io/helidon-examples/tree/helidon-4.x/examples/quickstarts/helidon-quickstart-mp) application.
Make sure to use JDK 24 to build the app.

### Build and start the app
Build and start the server using JDK 24:
```shell
mvn package
java -jar target/whatever-app-you-build.jar
```

### Retrieve metrics

The metrics endpoint is different for SE and MP apps.

For SE:
```shell
curl http://localhost:8080/observe/metrics -H "Accept: application/json" | jq | grep vthread
```
The preceding command uses [`jq`](https://jqlang.github.io/jq/) to format the retrieved JSON. You do not need it but it formats its output nicely for ease of reading and searching.

Output:
```list
    "vthreads.scheduler.pool-size": 1,
    "vthreads.scheduler.queued-virtual-thread-count": 0,
    "vthreads.scheduler.parallelism": 8,
    "vthreads.scheduler.mounted-virtual-thread-count": 1,
```

For MP:
```shell
curl http://localhost:8080/metrics | grep vthread
```
Output:
```list
# HELP vthreads_scheduler_mounted_virtual_thread_count Estimate of the number of virtual threads that are currently mounted by the scheduler; -1 if not known.
# TYPE vthreads_scheduler_mounted_virtual_thread_count gauge
vthreads_scheduler_mounted_virtual_thread_count{mp_scope="base",} 1.0
# HELP vthreads_scheduler_queued_virtual_thread_count Estimate of the number of virtual threads that are queued to the scheduler to start or continue execution; -1 if not known.
# TYPE vthreads_scheduler_queued_virtual_thread_count gauge
vthreads_scheduler_queued_virtual_thread_count{mp_scope="base",} 0.0
# HELP vthreads_scheduler_parallelism Scheduler's target parallelism.
# TYPE vthreads_scheduler_parallelism gauge
vthreads_scheduler_parallelism{mp_scope="base",} 8.0
# HELP vthreads_scheduler_pool_size Current number of platform threads that the scheduler has started but have not terminated; -1 if not known.
# TYPE vthreads_scheduler_pool_size gauge
vthreads_scheduler_pool_size{mp_scope="base",} 1.0
```
