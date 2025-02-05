#!/bin/bash
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

dir=$1
if [ $# -ne 1 ]; then
  echo "Please provide directory"
  exit 1
fi

if [ ! -d $dir ]; then
  echo "$dir is not a directory"
  exit 1
fi

DEBUG="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"

case $dir in
  coherence)
    cd coherence && java -Doracle.jdbc.provider.traceEventListener=open-telemetry-trace-event-listener-provider -Doracle.jdbc.provider.opentelemetry.sensitive-enabled=true -Dotel.export.name=todo.coherence -Dotel.sdk.disabled=false -Dotel.service.name=todo.coherence -Dotel.traces.exporter=otlp -Dotel.java.global-autoconfigure.enabled=true -Dcoherence.tracing.ratio=1 -Dotel.metrics.exporter=none -Dcoherence.hibernate.config=$HIBERNATE_CFG_XML -Dcoherence.wka=127.0.0.1 -Dcoherence.pof.enabled=true -Dcoherence.cluster=todo -jar target/helidon-labs-todo-coherence.jar
    ;;
  backend)
    cd backend && java -Xmx512m -Xms512m -Dotel.java.global-autoconfigure.enabled=true -Dotel.service.name=todo.backend -Dcoherence.wka=127.0.0.1 -Dcoherence.pof.enabled=true -Dcoherence.ttl=0 -Dcoherence.cluster=todo -Dcoherence.distributed.localstorage=false -Dcoherence.tracing.ratio=1 -jar target/helidon-labs-todo-backend.jar
    ;;
  frontend)
    cd frontend && java -Dotel.java.global-autoconfigure.enabled=true -Dotel.service.name=todo.frontend -Dotel.metrics.exporter=none -Dconfig.profile=local -Xmx512m -Xms512m -jar target/helidon-labs-todo-frontend.jar
esac