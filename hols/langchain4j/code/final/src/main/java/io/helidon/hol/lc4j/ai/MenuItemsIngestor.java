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
package io.helidon.hol.lc4j.ai;

import java.util.logging.Logger;

import io.helidon.common.config.Config;
import io.helidon.hol.lc4j.data.MenuItem;
import io.helidon.hol.lc4j.data.MenuItemsService;
import io.helidon.service.registry.Service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;

/**
 * A simple ingestor that populates the embedding store with menu items.
 *
 * This service reads menu items from a JSON file, converts them into text-based
 * representations, generates embeddings using an {@link EmbeddingModel}, and
 * stores them in the specified {@link EmbeddingStore}.
 */
@Service.Singleton
public class MenuItemsIngestor {
    private static final Logger LOGGER = Logger.getLogger(MenuItemsIngestor.class.getName());

    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;
    private final MenuItemsService menuItemsService;

    /**
     * Constructs a {@code MenuItemsIngestor} instance.
     *
     * @param config            the application configuration
     * @param embeddingStore    the embedding store where generated embeddings are stored
     * @param embeddingModel    the embedding model used for generating embeddings
     * @param menuItemsService  the service for retrieving menu items from a JSON file
     */
    @Service.Inject
    MenuItemsIngestor(Config config,
                      EmbeddingStore<TextSegment> embeddingStore,
                      EmbeddingModel embeddingModel,
                      MenuItemsService menuItemsService) {
        this.embeddingStore = embeddingStore;
        this.embeddingModel = embeddingModel;
        this.menuItemsService = menuItemsService;
    }

    /**
     * Initializes the embedding store by processing menu items.
     *
     * This method retrieves menu items, converts them into text representations,
     * generates embeddings using the provided embedding model, and stores them
     * in the embedding store.
     */
    public void ingest() {
        // Create ingestor with given embedding model and embedding storage
        var ingestor = EmbeddingStoreIngestor.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();

        // Read menu items from JSON
        var menuItems = menuItemsService.getMenuItems();

        // Create text representations of menu items
        var documents = menuItems.stream()
                .map(this::generateDocument)
                .toList();

        // Feed it to the ingestor to create embeddings and store them in embedding storage
        ingestor.ingest(documents);
    }

    /**
     * Converts a {@link MenuItem} into a text-based document for embedding generation.
     *
     * @param item the menu item to convert
     * @return a {@link Document} containing a formatted text representation of the menu item
     */
    private Document generateDocument(MenuItem item) {
        var str = String.format(
                "%s: %s. Category: %s. Price: $%.2f. Tags: %s. Add-ons: %s.",
                item.getName(),
                item.getDescription(),
                item.getCategory(),
                item.getPrice(),
                String.join(", ", item.getTags()),
                String.join(", ", item.getAddOns())
        );

        return Document.from(str);
    }
}
