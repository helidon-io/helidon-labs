#!/bin/bash -e
#
# Copyright (c) 2025 Oracle and/or its affiliates.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
RUN_ID=$(date +%s)
WORK_DIR=/tmp
WARMUP_CACHEBUST=1
WARMUP_DURATION=15s
FIRST_RUN=5s
SECOND_RUN=15s

while [ $# -gt 0 ]; do
  case "$1" in
    -warmup=no_cache*)
      WARMUP_CACHEBUST=$(date +%s)
      ;;
    -dir=*)
      WORK_DIR="${1#*=}"
      ;;
    -warmup=*)
      WARMUP_DURATION="${1#*=}"
      ;;
    -first-run=*)
      FIRST_RUN="${1#*=}"
      ;;
    -second-run=*)
      SECOND_RUN="${1#*=}"
      ;;
    *)
      echo "Bad argument, possible arguments are:"
      echo "-warmup=no_cache    # Resets cached warmup(default uses cached warmup)"
      echo "-dir=/tmp           # /tmp is default"
      echo "-warmup=15s         # How long takes the warmup benchmark(default is 15 sec, resets most of the caches)"
      echo "-first-run=5s       # How long takes the cold start benchmark(default is 5 sec)"
      echo "-second-run=15s     # How long takes the second benchmark(default is 15 sec)"
      exit 1
  esac
  shift
done

RUN_DIR=$WORK_DIR/startup-benchmark-$RUN_ID
mkdir -p $RUN_DIR
RESULTS_FILE=$RUN_DIR/results.csv

# Base image with common pre-requisites for all the tests
docker build -t helidon/benchmark-startup-base -f Dockerfile.base .

# Runs single benchmark, ex:
# runBenchmark se leyden
function runBenchmark() {
    FLAVOR=$1
    NAME=$2
    IMAGE_NAME="helidon/benchmark-startup-$NAME-$FLAVOR"
    NAME_CAP=$(echo $NAME | awk '{$1=toupper(substr($1,0,1))substr($1,2)}1')
    LOG_FILE=$RUN_DIR/$NAME-$FLAVOR-run.log

    printf "docker build --build-arg FLAVOR=$FLAVOR "
    printf "--build-arg WARMUP_CACHEBUST=$WARMUP_CACHEBUST "
    printf "--build-arg WARMUP_DURATION=$WARMUP_DURATION "
    printf "-t $IMAGE_NAME -f Dockerfile.$NAME .\n"
    docker build --build-arg FLAVOR=$FLAVOR \
    --build-arg WARMUP_CACHEBUST=$WARMUP_CACHEBUST \
    --build-arg WARMUP_DURATION=$WARMUP_DURATION \
    -t $IMAGE_NAME -f Dockerfile.$NAME .

    echo -n "$NAME_CAP $FLAVOR," >> $RESULTS_FILE
    echo "docker run --rm -e WRK_RUN_1_DURATION=${FIRST_RUN} -e WRK_RUN_2_DURATION=${SECOND_RUN} -i $IMAGE_NAME"
    docker run --rm -e WRK_RUN_1_DURATION=${FIRST_RUN} -e WRK_RUN_2_DURATION=${SECOND_RUN} -i $IMAGE_NAME |& tee $LOG_FILE
    tail -1 $LOG_FILE >> $RESULTS_FILE
}

# Check all docker files within current folder and run se and mp tests
for FLAVOR in se mp ; do
  for f in Dockerfile.*; do
    TEST_NAME=$(echo $f | awk -F. '{printf $2}');
    [ "$TEST_NAME" = "base" ] && continue
    runBenchmark $FLAVOR $TEST_NAME
  done;
done;

# Nice table with results
ROW_PATTERN="%-25s| %9s| %12s| %12s| %12s| %13s\n"
HEADER="Name,Warmup ms,Warmup req/s,Startup ms,${FIRST_RUN}s run req/s,${SECOND_RUN}s run req/s"
AWK_TMPL="{printf \"$ROW_PATTERN\", \$1, \$2, \$3, \$4, \$5, \$6}"
HEADER_LENGTH=$(echo $HEADER | awk -F, "$AWK_TMPL" | wc -c)
SEPARATOR=$(printf "%.${HEADER_LENGTH}s\n" $(printf -- '-%.0s' {1..100}))

echo $HEADER | awk -F, "$AWK_TMPL"
echo $SEPARATOR
awk -F, "$AWK_TMPL" $RESULTS_FILE
echo $SEPARATOR
echo "Results stored in $RESULTS_FILE"