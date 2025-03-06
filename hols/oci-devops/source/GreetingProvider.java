/*
 * Copyright (c) 2025 Oracle and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package demo.mp.oci.server;

import java.io.ByteArrayInputStream;
import java.util.logging.Logger;

import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.requests.GetNamespaceRequest;
import com.oracle.bmc.objectstorage.requests.GetObjectRequest;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.oracle.bmc.objectstorage.responses.GetNamespaceResponse;
import com.oracle.bmc.objectstorage.responses.GetObjectResponse;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Provider for greeting message.
 */
@ApplicationScoped
public class GreetingProvider {
    private static final Logger LOGGER = Logger.getLogger(GreetingProvider.class.getName());
    private ObjectStorage objectStorageClient;
    private String namespaceName;
    private String bucketName;
    private final String objectName = "hello.txt";

    /**
     * Create a new greeting provider, reading the message from configuration.
     *
     * @param message greeting to use
     */
    @Inject
    public GreetingProvider(@ConfigProperty(name = "app.greeting") String message,
                            ObjectStorage objectStorageClient,
                            @ConfigProperty(name = "oci.bucket.name") String bucketName) {
        try {
            this.bucketName = bucketName;
            GetNamespaceResponse namespaceResponse =
                    objectStorageClient.getNamespace(GetNamespaceRequest.builder().build());
            this.objectStorageClient = objectStorageClient;
            this.namespaceName = namespaceResponse.getValue();
            LOGGER.info("Object storage namespace: " + namespaceName);

            if (getMessage() == null) {
                setMessage(message);
            }
        } catch (Exception e) {
            LOGGER.warning("Error invoking getNamespace from Object Storage: " + e);
        }
    }

    String getMessage() {
        try {
            GetObjectResponse getResponse =
                    objectStorageClient.getObject(
                            GetObjectRequest.builder()
                                    .namespaceName(namespaceName)
                                    .bucketName(bucketName)
                                    .objectName(objectName)
                                    .build());
            return new String(getResponse.getInputStream().readAllBytes());
        } catch (Exception e) {
            LOGGER.warning("Error invoking getObject from Object Storage: " + e);
            return null;
        }
    }

    void setMessage(String message) {
        try {
            byte[] contents = message.getBytes();
            PutObjectRequest putObjectRequest =
                    PutObjectRequest.builder()
                            .namespaceName(namespaceName)
                            .bucketName(bucketName)
                            .objectName(objectName)
                            .putObjectBody(new ByteArrayInputStream(message.getBytes()))
                            .contentLength(Long.valueOf(contents.length))
                            .build();
            objectStorageClient.putObject(putObjectRequest);
        } catch (Exception e) {
            LOGGER.warning("Error invoking putObject from Object Storage: " + e);
        }
    }
}
