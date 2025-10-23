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

package io.helidon.labs.apps.mcp.coffee.shop.declarative;

import java.util.List;

import io.helidon.data.Data;

/**
 * {@link io.helidon.extensions.mcp.examples.coffee.shop.declarative.Order} entity data repository interface.
 * <p>
 * {@code OrderRepository} interface acts as a data access layer, encapsulating the logic for interacting
 * with the {@link io.helidon.extensions.mcp.examples.coffee.shop.declarative.Order} entity data, and also provides
 * basic CRUD (Create, Read, Update, Delete) operation for the
 * {@link io.helidon.extensions.mcp.examples.coffee.shop.declarative.Order} entity.
 *
 * @see io.helidon.data.Data.CrudRepository
 * @see io.helidon.extensions.mcp.examples.coffee.shop.declarative.Order
 */
@Data.Repository
interface OrderRepository extends Data.CrudRepository<Order, Integer> {
    /**
     * Retrieves a list of {@link io.helidon.extensions.mcp.examples.coffee.shop.declarative.Order} entities
     * ordered by its {@code ID}.
     * <p>
     * Query defined by method name: return list of {@link io.helidon.extensions.mcp.examples.coffee.shop.declarative.Order}
     * entities ordered by {@code ID} property.
     *
     * @return a list of {@link io.helidon.extensions.mcp.examples.coffee.shop.declarative.Order} entities
     */
    List<Order> listOrderById();
}
