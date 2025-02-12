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

if [ $dir != "all" ] && [ ! -d $dir ] ; then
  echo "$dir is not a directory"
  exit 1
fi

case $dir in
  coherence)
    mvn -pl coherence clean install -DskipTests
    ;;
  backend)
    mvn -pl backend clean install -DskipTests
    ;;
  frontend)
    mvn -pl frontend package -DskipTests
    ;;
  all)
    ./build.sh coherence
    ./build.sh backend
    ./build.sh frontend
  ;;
esac