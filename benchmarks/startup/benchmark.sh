#!/bin/bash -e

RESULTS_FILE=/tmp/startup-benchmark-results-$(date +%s).csv

mvn package

docker build -t helidon/benchmark-startup-base -f Dockerfile.base .

function runBenchmark() {
    FLAVOR=$1
    NAME=$2
    NAME_CAP=$(echo $NAME | awk '{$1=toupper(substr($1,0,1))substr($1,2)}1')
    echo "docker build --build-arg FLAVOR=$FLAVOR -t helidon/benchmark-startup-$NAME-$FLAVOR -f Dockerfile.$NAME ."
    docker build --build-arg FLAVOR=$FLAVOR -t helidon/benchmark-startup-$NAME-$FLAVOR -f Dockerfile.$NAME .
    echo "docker run --rm -it helidon/benchmark-startup-$NAME-$FLAVOR | tail -1"
    echo -n "$NAME_CAP $FLAVOR," >> $RESULTS_FILE
    docker run --rm -it helidon/benchmark-startup-$NAME-$FLAVOR | tail -1 >> $RESULTS_FILE
}

runBenchmark se vanilla
runBenchmark se crac
runBenchmark se leyden
runBenchmark se nativeimage
runBenchmark se nativeimage-pgo
runBenchmark mp vanilla
runBenchmark mp crac
runBenchmark mp leyden
runBenchmark mp nativeimage
runBenchmark mp nativeimage-pgo

rows="%-20s| %11s| %11s| %13s| %13s\n"
printf "$rows" "Name" "Warmup ms" "Startup ms" "Warmup req/s" "Run req/s"
printf "%.75s\n" "$(printf -- '-%.0s' {1..100})"
awk -F, "{printf \"${rows}\", \$1, \$2, \$3, \$4, \$5}" $RESULTS_FILE
printf "%.75s\n" "$(printf -- '-%.0s' {1..100})"

echo "Results stored in $RESULTS_FILE"