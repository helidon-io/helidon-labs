## Helidon Startup Benchmark

```
curl https://host.com/path/to/the.patch | git apply -v
/usr/bin/time -f "%e" -o /tmp/aot-time mvn package && sleep 5 


waitForUrl() {
    endpoint=$1
    timeout --foreground -s TERM 60s bash -c \
        'while [[ "$(curl -s -o /dev/null -m 3 -L -w ''%{http_code}'' ${0})" != "200" ]];\
        do sleep 0.01;\
        done' $endpoint

```