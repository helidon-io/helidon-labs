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

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Represents an order in the coffee shop.
 * <p>
 * An order includes the customer name, the content with names
 * of beverage and dishes, and the total price.
 */
@Entity
@Table(name = "ORDERS")
public class Order {
    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "CONTENT")
    private String content;

    @Column(name = "PRICE")
    private BigDecimal price;

    /**
     * Create a new {@code Order} instance.
     */
    public Order() {
        this.id = UUID.randomUUID().toString();
    }

    /**
     * Create a new {@code Order} instance with the specified values.
     *
     * @param name    the order name
     * @param content the order content
     * @param price   the order price
     */
    public Order(String name, String content, BigDecimal price) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.price = price;
        this.content = content;
    }

    /**
     * Returns the unique identifier of the order.
     *
     * @return the order id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the order.
     *
     * @param id the order id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the name of the order.
     *
     * @return the order name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the order.
     *
     * @param name the order name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the content of the order.
     *
     * @return the order content
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the content of the order.
     *
     * @param content the order content
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Returns the price of the order.
     *
     * @return the order price
     */
    public BigDecimal getPrice() {
        return price;
    }

    /**
     * Sets the price of the order.
     *
     * @param price the order price
     */
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
