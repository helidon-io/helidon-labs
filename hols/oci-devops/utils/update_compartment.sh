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
SCRIPT_DIR=$(dirname "$0")
INIT_DIR=${SCRIPT_DIR}/../init

INIT_TERRAFORM_TFSTATE=${INIT_DIR}/terraform.tfstate
if ! test -f "${INIT_TERRAFORM_TFSTATE}"; then
  echo "Error: Terraform state (\"${INIT_TERRAFORM_TFSTATE}\") does not exist"
  exit 1
fi

COMPARTMENT_ID=$("${INIT_DIR}"/get.sh compartment_id)
if [[ "${COMPARTMENT_ID}" == *"Requested oci resource does not exist"* ]]; then
  echo "${COMPARTMENT_ID}"
  exit 1
fi

TERRAFORM_TFVARS=${SCRIPT_DIR}/../terraform.tfvars
sed -i -e 's/.*compartment_ocid.*/compartment_ocid = '"\"${COMPARTMENT_ID}\""'/' "${TERRAFORM_TFVARS}"
echo "compartment_ocid in ${TERRAFORM_TFVARS} was updated"
