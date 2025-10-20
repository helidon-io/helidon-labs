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
 * Represents a menu item in the coffee shop.
 * <p>
 * A menu item includes details such as its name, description, category, price,
 * tags, and optional add-ons.
 */
@Entity
@Table(name = "MENU")
public class MenuItem {
    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "CATEGORY")
    private String category;

    @Column(name = "PRICE")
    private BigDecimal price;

    @Column(name = "TAGS")
    private String tags;

    @Column(name = "ADDONS")
    private String addOns;

    /**
     * Create a new {@code MenuItem} instance.
     */
    public MenuItem() {
        this.id = UUID.randomUUID().toString();
    }

    /**
     * Create a new {@code MenuItem} instance.
     *
     * @param id menu item id
     */
    public MenuItem(String id) {
        this.id = id;
    }

    /**
     * Returns the unique identifier of the menu item.
     *
     * @return the menu item id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the menu item.
     *
     * @param id the menu item id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the name of the menu item.
     *
     * @return the menu item name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the menu item.
     *
     * @param name the menu item name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the description of the menu item.
     *
     * @return the menu item description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the menu item.
     *
     * @param description the menu item description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the category of the menu item.
     *
     * @return the menu item category
     */
    public String getCategory() {
        return category;
    }

    /**
     * Sets the category of the menu item.
     *
     * @param category the menu item category
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Returns the price of the menu item.
     *
     * @return the menu item price
     */
    public BigDecimal getPrice() {
        return price;
    }

    /**
     * Sets the price of the menu item.
     *
     * @param price the menu item price
     */
    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    /**
     * Returns the tags of the menu item.
     *
     * @return the menu item tags
     */
    public String getTags() {
        return tags;
    }

    /**
     * Sets the tags of the menu item.
     *
     * @param tags the menu item tags
     */
    public void setTags(String tags) {
        this.tags = tags;
    }

    /**
     * Returns the add-ons of the menu item.
     *
     * @return the menu item add-ons
     */
    public String getAddOns() {
        return addOns;
    }

    /**
     * Sets the add-ons of the menu item.
     *
     * @param addOns the menu item add-ons
     */
    public void setAddOns(String addOns) {
        this.addOns = addOns;
    }
}
