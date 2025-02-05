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

CLUSTER=$5
REGION=$7

TOKEN_FILE=$HOME/.kube/TOKEN-$CLUSTER

if ! test -f "$TOKEN_FILE" || test $(( `date +%s` - `stat -L -c %Y $TOKEN_FILE` )) -gt 240; then
  umask 022
  oci ce cluster generate-token --cluster-id "$CLUSTER" --region "$REGION" > $TOKEN_FILE
fi

cat $TOKEN_FILE