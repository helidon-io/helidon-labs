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
import io.helidon.data.PageRequest;
import io.helidon.labs.apps.data.se.imperative.model.Breed;
import io.helidon.labs.apps.data.se.imperative.model.Owner;
import io.helidon.labs.apps.data.se.imperative.model.Pet;
import io.helidon.labs.apps.data.se.imperative.repository.BreedRepository;
import io.helidon.labs.apps.data.se.imperative.repository.OwnerRepository;
import io.helidon.labs.apps.data.se.imperative.repository.PetRepository;
import io.helidon.http.BadRequestException;
import io.helidon.service.registry.Services;
import io.helidon.transaction.Tx;
import io.helidon.webserver.http.Handler;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

/**
 * Service class responsible for handling HTTP requests related to {@link io.helidon.labs.apps.data.se.imperative.model.Pet} entities.
 * <p>
 * This class implements the {@link HttpService} interface and provides endpoint handlers
 * for operations on {@link io.helidon.labs.apps.data.se.imperative.model.Pet} entities.
 *
 * @see PetRepository
 * @see BreedRepository
 * @see OwnerRepository
 */
public class PetService implements HttpService {

    // Helidon Data repository interface providing Pet entity operations.
    private final PetRepository petRepository = Services.get(PetRepository.class);
    // Helidon Data repository interface providing Breed entity operations.
    private final BreedRepository breedRepository = Services.get(BreedRepository.class);
    // Helidon Data repository interface providing Owner entity operations.
    private final OwnerRepository ownerRepository = Services.get(OwnerRepository.class);

    // Pet endpoint routing rules
    @Override
    public void routing(HttpRules rules) {
        rules.get("/", this::index)
                .get("/all", this::all)
                .get("/all/{page}", this::allPage)
                .get("/breed/{name}", this::breed)
                .get("/get/{name}", this::pet)
                .post("/", Handler.create(PetDto.class, this::insert))
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
                              Pet entity endpoint:
                                   GET /pet/all            - List all pets
                                   GET /pet/all/{page}     - List all pets as pages of size 5
                                   GET /pet/breed/{name}   - List all pets of provided breed
                                   GET /pet/get/{name}     - Retrieve pet with provided name
                                   POST /pet               - Insert new pet: { "name":<name>,
                                                                               "weight":<weight>,
                                                                               "birth":<birth_date>,
                                                                               "owner":<owner_name>,
                                                                               "breed":<breed_name> }
                                   DELETE /pet/{id}        - Delete pet with provided ID
                         """);
    }

    /**
     * Handles the {@code GET /all} request and returns a list of all {@link io.helidon.labs.apps.data.se.imperative.model.Pet} entities.
     *
     * @param request  the server request
     * @param response the server response
     */
    private void all(ServerRequest request, ServerResponse response) {
        response.send(petRepository.listOrderByName());
    }

    /**
     * Handles the {@code GET /all/{page}} request and returns a paginated list of all {@link io.helidon.labs.apps.data.se.imperative.model.Pet}
     * entities.
     * <p>
     * The page size is fixed at 5. The page index starts from 0.
     *
     * @param request  the server request
     * @param response the server response
     */
    private void allPage(ServerRequest request, ServerResponse response) {
        int page = request.path()
                .pathParameters()
                .first("page").map(Integer::parseInt)
                .orElseThrow(() -> new BadRequestException("No page number"));
        response.send(
                PageDto.create(
                        petRepository.listOrderByName(PageRequest.create(page, 5))));
    }

    /**
     * Handles the {@code GET /breed/{name}} request and returns a list of {@link io.helidon.labs.apps.data.se.imperative.model.Pet} entities
     * associated with a specific breed name.
     *
     * @param request  the server request
     * @param response the server response
     */
    private void breed(ServerRequest request, ServerResponse response) {
        String name = request.path().pathParameters().get("name");
        response.send(petRepository.listByBreed_Name(name));
    }

    /**
     * Handles the {@code GET /get/{name}} request and returns a {@link io.helidon.labs.apps.data.se.imperative.model.Pet} entity by its name.
     *
     * @param request  the server request
     * @param response the server response
     */
    private void pet(ServerRequest request, ServerResponse response) {
        String name = request.path().pathParameters().get("name");
        response.send(petRepository.findByName(name));
    }

    /**
     * Handles the {@code POST /} request and inserts a new {@link io.helidon.labs.apps.data.se.imperative.model.Pet} entity.
     * <p>
     * Pet entity content is supplied as JSON object and translated to {@link PetDto} class by HTTP
     * request processing layer.
     * Pet breed and owner records must already exist. Pet entity must not exist in the database.
     *
     * @param petDto   the pet to insert into the database
     * @param response the server response
     */
    private void insert(PetDto petDto, ServerResponse response) {
        // Pet data modification task is executed as single database transaction
        response.send(Tx.transaction(() -> {
            // Retrieve Breed and Owner entities for provided names
            Breed breed = breedRepository.getByName(petDto.breed());
            Owner owner = ownerRepository.ownerByName(petDto.owner());
            // Prepare and insert new Pet entity into the database
            Pet petEntity = new Pet(petDto.name(),
                                    petDto.weight(),
                                    petDto.birth(),
                                    owner,
                                    breed);
            return petRepository.insert(petEntity);
        }));
    }

    /**
     * Handles the {@code DELETE /{id}} request and deletes a {@link Pet} entity by its ID.
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
        response.send(new DmlResult(petRepository.deleteById(id), "DELETE"));
    }

}
