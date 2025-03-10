# Grafana Dashboards

This project contains updated Helidon grafana dashboards and the instructions on how to 
configure and enable additional metrics for Helidon SE and Helidon MP.

> Note: This document and it's contents are work in progress only and not complete. 
> The information and dashboards may change or be removed.

## Table of Contents

* [Software Pre-requisites](#software-pre-requisites)
* [References](#references)
* [Helidon Setup](#helidon-setup)
  + [Application Identification](#application-identification)
  + [Required Dependencies](#required-dependencies)
  + [Enabling Metrics](#enabling-metrics)
* [Prometheus Setup](#prometheus-setup)
* [Grafana Setup](#grafana-setup)
* [grafana Dashboard Images](#grafana-dashboard-images)

## Software Pre-requisites

* JDK21 or JDK24
* Grafana 10.0.13+

## References

See the following for detailed information in Helidon metrics:

* [SE Metrics](https://helidon.io/docs/latest/se/guides/metrics#basic-and-extended-kpi)
* [Web Client Metrics](https://helidon.io/docs/latest/se/guides/webclient#WebClient-Metrics#WebClient-Metrics)
* [MP Metrics](https://helidon.io/docs/latest/mp/metrics/metrics)
* [MP REST Metrics](https://helidon.io/docs/latest/mp/guides/metrics#controlling-rest-request-metrics)
* [Helidon Labs Virtual Threads - JDK24](https://github.com/helidon-io/helidon-labs/tree/main/incubator/virtual-threads-metrics)

## Helidon Setup

### Application Identification

To ensure we can identify individual helidon applications as well as allowing drill-through, you should add the
following labels to your generated metrics using the system property or `application.yaml` (SE) or `microprofile-config.properties` (MP):

**Helidon SE**

* System Property: `-Dmetrics.tags="se_app=my-se-app"`
* `aplication.yaml`: 
  ```bash
  metrics:
    tags: "se_app=my-se-app"
  ```
  
This will add the label to prometheus generated metrics, e.g.:

```bash
jvm_uptime_seconds{instance="host.docker.internal:7001", job="helidon", scope="base", se_app="my-se-app"}
```

**Helidon MP**

* System Property: `-Dmp.metrics.appName=my-mp-app`
* `microprofile-config.properties` `
   ```bash
   mp.metrics.tags=mp_app=my-mp-app
   ```

This will add the label to prometheus generated metrics, e.g.:

```bash
REST_request_seconds{... mp_app="my-mp-add", mp_scope="base", quantile="0.5"}
```

### Required Dependencies

**SE**

```xml
<dependency>
    <groupId>io.helidon.webclient</groupId>
    <artifactId>helidon-webclient-metrics</artifactId>
</dependency>

<dependency>
    <groupId>io.helidon.webserver.observe</groupId>
    <artifactId>helidon-webserver-observe-metrics</artifactId>
</dependency>

<dependency>
    <groupId>io.helidon.metrics</groupId>
    <artifactId>helidon-metrics-system-meters</artifactId>
    <scope>runtime</scope>
</dependency>
```

**MP**

Include the following:
```xml
<dependency>
    <groupId>io.helidon.microprofile.metrics</groupId>
    <artifactId>helidon-microprofile-metrics</artifactId>
</dependency>

<dependency>
    <groupId>io.helidon.webclient</groupId>
    <artifactId>helidon-webclient-metrics</artifactId>
</dependency>

<dependency>
    <groupId>io.helidon.metrics</groupId>
    <artifactId>helidon-metrics-system-meters</artifactId>
    <scope>runtime</scope>
</dependency>
```

To include new experimental virtual threads metrics, available only in JDK 24, you can clone Helidon Labs at
https://github.com/helidon-io/helidon-labs.git and build and include the following:

```xml
<dependency>
    <groupId>io.helidon.labs.incubator</groupId>
    <artifactId>helidon-labs-incubator-virtual-threads-metrics</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Enabling Metrics

Additional properties are required to ensure we get the full range of metrics:

> Note: These are shown as system properties by can be set using the appropriate way in SE or MP.

**Common Settings**

* `-Dmetrics.key-performance-indicator.extended=true`
* `-Dmetrics.virtual-threads.count.enabled=true` 
* `-Dmetrics.built-in-meter-name-format=CAMEL` 
* `-Dmetrics.gc-time-type=GAUGE`

**MP Specific**

* `-Dmetrics.rest-request.enabled=true` - enables REST metrics. See [here](https://helidon.io/docs/latest/mp/guides/metrics#controlling-rest-request-metrics) for more details.


## Prometheus Setup

* SE Apps http://host:port/observe/metrics
* MP Apps http://host:port/metrics

## Grafana Setup

TBC

## Troubleshooting

TBC

## Grafana Dashboard Images

The section below outlines the initial "work-in-progress" Grafana dashboards.

### Main DashBoard

![Coherence Demo](images/helidon-dashboard-main.png)

### SE Details Dashboard

![Coherence Demo](images/helidon-se-details-1.png)

### SE Details Dashboard (Virtual Threads)

![Coherence Demo](images/helidon-se-details-threads.png)

### MP Details Dashboard

![Coherence Demo](images/helidon-mp-details.png)

### MP Details Dashboard (Threads and REST)

![Coherence Demo](images/helidon-mp-details-thread-rest.png)

