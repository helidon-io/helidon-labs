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
export TERRAFORM_TFSTATE=${SCRIPT_DIR}/terraform.tfstate
# shellcheck disable=SC1091
source "${SCRIPT_DIR}"/../utils/get_common.sh

# Command Choices:
COMPARTMENT_ID_COMMAND=compartment_id
COMPARTMENT_NAME_COMMAND=compartment_name
ALL_COMMAND=all

get_compartment_id() {
  get_resource_value ${COMPARTMENT_ID_COMMAND}
  echo
}

get_compartment_name() {
  get_resource_value ${COMPARTMENT_NAME_COMMAND}
  echo
}

# Display usage information for this tool.
display_help()
{
  local left_justified_size=9
  echo "Usage: $(basename "$0") {${COMPARTMENT_ID_COMMAND}|${COMPARTMENT_NAME_COMMAND}|${ALL_COMMAND}}"
  echo
  print_command_detail ${COMPARTMENT_ID_COMMAND} "displays compartment id" ${left_justified_size}
  print_command_detail ${COMPARTMENT_NAME_COMMAND} "displays compartment name" ${left_justified_size}
  print_command_detail ${ALL_COMMAND} "displays ${COMPARTMENT_ID_COMMAND}, ${COMPARTMENT_NAME_COMMAND}" ${left_justified_size}
  echo
}

# Main routine
case "$1" in
  "${COMPARTMENT_ID_COMMAND}")
    get_compartment_id
    ;;
  "${COMPARTMENT_NAME_COMMAND}")
    get_compartment_name
    ;;
  "${ALL_COMMAND}")
    left_justified_size=16
    print_resource ${COMPARTMENT_ID_COMMAND} "$(get_compartment_id)" ${left_justified_size}
    print_resource ${COMPARTMENT_NAME_COMMAND} "$(get_compartment_name)" ${left_justified_size}
    ;;
  *)
    display_help
    ;;
esac
