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
package io.helidon.labs.apps.data.se.imperative.service;

import io.helidon.common.media.type.MediaTypes;
import io.helidon.http.BadRequestException;
import io.helidon.labs.apps.data.se.imperative.model.Owner;
import io.helidon.labs.apps.data.se.imperative.repository.OwnerRepository;
import io.helidon.service.registry.Services;
import io.helidon.transaction.Tx;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

/**
 * Service class responsible for handling HTTP requests related to {@link io.helidon.labs.apps.data.se.imperative.model.Owner} entities.
 * <p>
 * This class implements the {@link HttpService} interface and provides endpoint handlers
 * for operations on {@link io.helidon.labs.apps.data.se.imperative.model.Owner} entities.
 *
 * @see OwnerRepository
 */
public class OwnerService implements HttpService {

    // Helidon Data repository interface providing Owner entity operations.
    private final OwnerRepository ownerRepository = Services.get(OwnerRepository.class);

    // Owner endpoint routing rules
    @Override
    public void routing(HttpRules rules) {
        rules.get("/", this::index)
                .get("/all", this::all)
                .get("/names/{breed}", this::namesByBreed)
                .post("/{name}", this::insert)
                .delete("/{id}", this::delete);
    }

    /**
     * Handles the {@code GET /} request and returns the index page with endpoint information.
     *
     * @param request  the server request
     * @param response the server response
     */
    private void index(ServerRequest request, ServerResponse response) {
        response.headers().contentType(MediaTypes.TEXT_PLAIN);
        response.send("""
                              Owner entity endpoint:
                                   GET /owner/all           - List all owners
                                   GET /owner/names/{breed} - List all owners names with pet of specific breed
                                   POST /owner/{name}       - Insert new owner with provided name
                                   DELETE /owner/{id}       - Delete owner with provided ID
                         """);
    }

    /**
     * Handles the {@code GET /all} request and returns a list of all {@link io.helidon.labs.apps.data.se.imperative.model.Owner} entities.
     *
     * @param request  the server request
     * @param response the server response
     */
    private void all(ServerRequest request, ServerResponse response) {
        response.send(ownerRepository.ownersOrderedByName());
    }

    /**
     * Handles the {@code GET /names/{breed}} request and returns a list of owner names whose pets
     * have a breed with the specified name.
     *
     * @param request  the server request
     * @param response the server response
     */
    private void namesByBreed(ServerRequest request, ServerResponse response) {
        String breed = request.path().pathParameters().get("breed");
        response.send(ownerRepository.queryNameByPetBreed(breed));
    }

    /**
     * Handles the {@code POST /{name}} request and inserts a new {@link io.helidon.labs.apps.data.se.imperative.model.Owner} entity with
     * the provided name.
     *
     * @param request  the server request
     * @param response the server response
     */
    private void insert(ServerRequest request, ServerResponse response) {
        String name = request.path().pathParameters().get("name");
        response.send(Tx.transaction(() -> ownerRepository.insert(new Owner(name))));
    }

    /**
     * Handles the {@code DELETE /{id}} request and deletes the {@link Owner} entity with
     * the specified ID.
     *
     * @param request  the server request
     * @param response the server response
     */
    private void delete(ServerRequest request, ServerResponse response) {
        int id = request.path()
                .pathParameters()
                .first("id").map(Integer::parseInt)
                .orElseThrow(() -> new BadRequestException("No pet id"));
        // Rely on internal transaction handling for this simple data modification task
        response.send(new DmlResult(ownerRepository.deleteById(id), "DELETE"));
    }

}
