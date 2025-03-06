#!/bin/bash
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

cd /var/lib/ocarun || exit

# shellcheck disable=SC2010
HELIDON_APP_NAME=$(ls -- *.jar | grep oci-mp)
pid=$(pgrep -f "${HELIDON_APP_NAME}")
if [ -n "$pid" ]; then
  echo "Stopping ${HELIDON_APP_NAME} with pid $pid"
fi
echo "Starting ${HELIDON_APP_NAME} from helidon-app.service"
sudo systemctl restart helidon-app.service

# Check if Helidon is ready in 60 seconds using the readiness healthcheck endpoint of the app.
TIMEOUT_SEC=60
start_time="$(date -u +%s)"
while true; do
if curl -s http://localhost:8080/health/ready | grep -q '"status":"UP"'; then
  echo "Helidon app is now running with pid $(pgrep -f "${HELIDON_APP_NAME}")"
  break
fi
current_time="$(date -u +%s)"
elapsed_seconds=$((current_time - start_time))
if [ $elapsed_seconds -gt $TIMEOUT_SEC ]; then
  echo "Error: Helidon app failed to run successfully. Printing the logs..."
  cat helidon-app.log
  exit 1
fi
  sleep 1
done
