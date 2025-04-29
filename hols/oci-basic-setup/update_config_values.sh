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

set -e

MONITORING_NAMESPACE=helidon_metrics
DEFAULT_PROJECT_PATH=~/oci-mp

if [ -z "${1}" ]; then
    read -r -p "Enter the Helidon MP project's root directory (default: ${DEFAULT_PROJECT_PATH}): " PROJECT_PATH
    # Use eval to expand ~ if it is part of the input
    PROJECT_PATH=$(eval echo -n "${PROJECT_PATH:-${DEFAULT_PROJECT_PATH}}")
    echo "$PROJECT_PATH"
else
     PROJECT_PATH=${1}
fi
if [ ! -d "${PROJECT_PATH}" ]; then
    echo "Error: \"${PROJECT_PATH}\" is not a valid directory"
    exit 1
fi

GET_SH=$(dirname "$0")/get.sh
COMPARTMENT_ID=$(${GET_SH} compartment_id)
APPLICATION_LOG_ID=$(${GET_SH} app_log_id)

APPLICATION_YAML=${PROJECT_PATH}/server/src/main/resources/application.yaml
sed -i 's/compartmentId: <your monitoring compartment id>/compartmentId: '"${COMPARTMENT_ID}"'/' "${APPLICATION_YAML}"
sed -i 's/namespace: <your monitoring namespace e.g. helidon_oci>/namespace: '"${MONITORING_NAMESPACE}"'/' "${APPLICATION_YAML}"
echo "Properties compartmentId and namespace under ocimetrics in ${APPLICATION_YAML} were updated"

MICROPROFILE_CONFIG_PROFILE=${PROJECT_PATH}/server/src/main/resources/META-INF/microprofile-config.properties
sed -i 's/oci.monitoring.compartmentId=<your monitoring compartment id>/oci.monitoring.compartmentId='"${COMPARTMENT_ID}"'/' "${MICROPROFILE_CONFIG_PROFILE}"
sed -i 's/oci.monitoring.namespace=<your monitoring namespace e.g. helidon_oci>/oci.monitoring.namespace=helidon_application/' "${MICROPROFILE_CONFIG_PROFILE}"
sed -i 's/oci.logging.id=<your oci custom log id>/oci.logging.id='"${APPLICATION_LOG_ID}"'/' "${MICROPROFILE_CONFIG_PROFILE}"
echo "Properties oci.monitoring.compartmentId, oci.monitoring.namespace and oci.logging.id in ${MICROPROFILE_CONFIG_PROFILE} were updated"
