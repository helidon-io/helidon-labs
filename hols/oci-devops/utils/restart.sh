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

SCRIPT_DIR=$(dirname "$0")
PUBLIC_IP=$(~/oci-devops-helidon-example/main/get.sh public_ip)
GET_SH=${SCRIPT_DIR}/../main/get.sh

# Generate private key
${GET_SH} create_ssh_private_key
ssh -i private.key opc@"${PUBLIC_IP}" "sudo -u ocarun bash" < "${SCRIPT_DIR}"/restart_on_server.sh
# remove private key
echo "Cleaning up ssh private.key"
rm -f private.key
