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

# Extracts resource value from terraform state
get_resource_value() {
  parse_tf_output "$1"
}

# Parses resource from terraform state output
parse_tf_output() {
  local resource
  resource=$(jq -r '.outputs.'"${1}"'.value' "${TERRAFORM_TFSTATE}")
  evaluate_parsed_resource "${resource}"
}

# Evaluate if parsed resource is empty or not
evaluate_parsed_resource() {
  if [[ -n "${1}" && "${1}" != "null" ]]; then
    echo -n "${1}"
  else
    echo -n "Requested oci resource does not exist"
  fi
}

print_command_detail() {
  local key=${1}
  local description=${2}
  local key_left_justified_size=${3}
  printf '   %-'"${key_left_justified_size}"'s' "${key}"
  echo "${description}"
}

print_resource() {
  local key=${1}
  local description=${2}
  local key_left_justified_size=${3}
  printf '%-'"${key_left_justified_size}"'s: ' "${key}"
  echo "${description}"
}

if ! test -f "${TERRAFORM_TFSTATE}"; then
  echo "Error: Terraform state (\"${TERRAFORM_TFSTATE}\") does not exist which means the oci resource(s) have not been provisioned yet"
  exit 1
fi


