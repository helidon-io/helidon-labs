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
ARTIFACT_REPO_NAME=artifact-repo-helidon-demo
BUCKET_NAME=$("${SCRIPT_DIR}"/get.sh bucket_name)

resources=$(jq -r '.resources[] | select(.type == "oci_artifacts_repository").instances[].attributes | select(.display_name == "'"${ARTIFACT_REPO_NAME}"'") | .compartment_id,.id' terraform.tfstate)
readarray -t resources <<<"$resources"
COMPARTMENT_ID=${resources[0]}
ARTIFACT_REPO_ID=${resources[1]}

# All artifacts in the repository should be deleted
artifact_ids=$(oci artifacts generic artifact list --compartment-id "${COMPARTMENT_ID}" --repository-id "${ARTIFACT_REPO_ID}" --all --query 'data.items[*].id' --raw-output | jq -r '.[]')
echo "Deleting all artifacts from '${ARTIFACT_REPO_NAME}'"
i=0
for artifact_id in "${artifact_ids[@]}" ; do
  oci artifacts generic artifact delete --artifact-id "${artifact_id}" --force
  ((i=i+1))
done
echo "Deleted $i artifacts"
echo

# All objects in the bucket should be deleted
object_names=$(oci os object list --bucket-name "${BUCKET_NAME}" --all --query 'data[*].name' --raw-output | jq -r '.[]')
echo "Deleting all objects from '${BUCKET_NAME}'"
i=0
for object_name in "${object_names[@]}"; do
  oci os object delete --bucket-name "${BUCKET_NAME}" --object-name "${object_name}" --force
  ((i=i+1))
done
echo "Deleted $i objects"
echo

# Clean up Kubernetes resources if they exist
if kubectl --kubeconfig="${SCRIPT_DIR}"/generated/kubeconfig delete deployment oci-mp-server &>/dev/null; then
  echo "oci-mp-server deployment was deleted"
fi
if kubectl --kubeconfig="${SCRIPT_DIR}"/generated/kubeconfig delete service oci-mp-server &>/dev/null; then
  echo "oci-mp-server LoadBalancer service was deleted"
fi

echo "Begin Terraform destroy..."
echo
terraform destroy -auto-approve
