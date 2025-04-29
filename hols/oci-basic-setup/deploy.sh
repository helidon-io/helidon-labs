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

# Use this to specify the JDK Installer version in tar_gz format
JDK_TAR_GZ_INSTALLER="https://download.oracle.com/java/21/latest/jdk-21_linux-x64_bin.tar.gz"

HELIDON_MP_APP_ZIP=oci-mp-server.zip
DEFAULT_PROJECT_PATH=~/oci-mp
SCRIPT_DIR=$(dirname $0)
source "${SCRIPT_DIR}"/get_common.sh

# Main routine
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
CURRENT_DIR=$(pwd)
SERVER_BIN_DIR="${PROJECT_PATH}/server/target"
cd "${SERVER_BIN_DIR}" || exit 1

# Assemble the application zip
zip -r "${CURRENT_DIR}/${HELIDON_MP_APP_ZIP}" libs oci-mp-server.jar
cd "${CURRENT_DIR}" || exit 1

# Generate private key file that will be use to ssh or scp to the instance
"${SCRIPT_DIR}"/get.sh create_ssh_private_key
# Get instance public IP
PUBLIC_IP=$("${SCRIPT_DIR}"/get.sh public_ip)
# Upload the application zip
scp -o StrictHostKeyChecking=accept-new -i private.key oci-mp-server.zip opc@"${PUBLIC_IP}":/home/opc

# Download & install jdk and run app
ssh -i private.key opc@"${PUBLIC_IP}" "bash -s ${HELIDON_MP_APP_ZIP} ${JDK_TAR_GZ_INSTALLER}" << 'EOF'
HELIDON_MP_APP_ZIP=${1}
JDK_TAR_GZ_INSTALLER=${2}

# Unzip the application binary
unzip -o "${HELIDON_MP_APP_ZIP}"

#  Download and extract JDK
JDK_TAR_GZ_INSTALLER_BASE=$(basename ${JDK_TAR_GZ_INSTALLER})
if ! ls "${JDK_TAR_GZ_INSTALLER_BASE}" ; then
    rm -f "*jdk*.tar.gz"
    rm -rf jdk*
    # Download JDK
    curl -O "${JDK_TAR_GZ_INSTALLER}" && echo "JDK downloaded successfully"
    tar xzf ${JDK_TAR_GZ_INSTALLER_BASE} && echo "JDK installed successfully"
fi

# export PATH=~/jdk-21.0.7/bin:$PATH
# nohup java -jar oci-mp-server.jar &> oci-mp-server.log &

# Create Service file
export JAVA_BIN=$(ls -d "$(pwd)"/jdk*/)bin
cat << EOF_INNER > helidon-app.service.new
[Unit]
Description=Helidon OCI-MP application service
After=syslog.target network.target

[Service]
User=opc
Type=simple
WorkingDirectory=/home/opc
ExecStart=/bin/bash -c '${JAVA_BIN}/java -jar oci-mp-server.jar &> helidon-app.log'

[Install]
WantedBy=multi-user.target
EOF_INNER

# Set appropriate SELinux Context for helidon-app.service if SELinux is in enforcing mode
SELINUX_ENFORCE_STATUS=$(getenforce)
if [ "${SELINUX_ENFORCE_STATUS}" == "Enforcing" ]; then
    echo "Setting context for systemd service file"
    chcon system_u:object_r:systemd_unit_file_t:s0 helidon-app.service.new
fi

# Start the Helidon application service
if ! systemctl is-enabled helidon-app.service; then
    echo "Enabling helidon-app.service"
    mv -f helidon-app.service.new helidon-app.service
    sudo systemctl enable /home/opc/helidon-app.service
else
    # reload systemd manager configuration as helidon-app.service has changed
    if ! diff helidon-app.service helidon-app.service.new; then
        echo "Reloading systemd manager configuration"
        mv -f helidon-app.service.new helidon-app.service
        sudo systemctl daemon-reload
    # ignore the new helidon-app.service as it has not changed
    else
        rm -f helidon-app.service.new
    fi
fi
echo "Starting helidon-app.service"
sudo systemctl restart helidon-app.service

# Check if Helidon is ready in 60 seconds using the readiness healthcheck endpoint of the app.
TIMEOUT_SEC=60
start_time="$(date -u +%s)"
while true; do
curl -s http://localhost:8080/health/ready | grep -q '"status":"UP"'
if [ $? -eq 0 ]; then
  echo "Helidon app is now running with pid $(ps -fe | grep oci-mp-server | grep -v -e bash -e grep | awk '{print $2}')"
  break
fi
current_time="$(date -u +%s)"
elapsed_seconds=$(($current_time-$start_time))
if [ $elapsed_seconds -gt $TIMEOUT_SEC ]; then
  echo "Error: Helidon app failed to run successfully. Printing the logs..."
  cat oci-mp-server.log
  exit 1
fi
  sleep 1
done
EOF

# delete private.key and application zip
rm -f private.key
rm -f "${HELIDON_MP_APP_ZIP}"
