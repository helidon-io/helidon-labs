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

# 1st stage, build the app
FROM container-registry.oracle.com/java/jdk-no-fee-term:21 as build

# Install maven
WORKDIR /usr/share
RUN set -x && \
    curl -O https://archive.apache.org/dist/maven/maven-3/3.8.4/binaries/apache-maven-3.8.4-bin.tar.gz && \
    tar -xvf apache-maven-*-bin.tar.gz  && \
    rm apache-maven-*-bin.tar.gz && \
    mv apache-maven-* maven && \
    ln -s /usr/share/maven/bin/mvn /bin/

WORKDIR /helidon

# Create a first layer to cache the "Maven World" in the local repository.
# Incremental docker builds will always resume after that, unless you update
# the pom
ADD pom.xml pom.xml
ADD client/pom.xml client/pom.xml
ADD server/pom.xml server/pom.xml
RUN mvn package -Dmaven.test.skip -Declipselink.weave.skip -DskipOpenApiGenerate

# Do the Maven build!
# Incremental docker builds will resume here when you change sources
ADD client/src client/src
ADD server/src server/src
ADD spec/api.yaml spec/api.yaml
RUN mvn package -DskipTests

RUN echo "done!"

# 2nd stage, build the runtime image
FROM container-registry.oracle.com/java/jdk-no-fee-term:21
WORKDIR /helidon

# Copy the binary built in the 1st stage
COPY --from=build /helidon/server/target/oci-mp-server.jar ./
COPY --from=build /helidon/server/target/libs ./libs

CMD ["java", "--enable-preview", "-jar", "oci-mp-server.jar"]

EXPOSE 8080
