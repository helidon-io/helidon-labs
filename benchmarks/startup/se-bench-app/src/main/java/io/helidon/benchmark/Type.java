/*
 * Copyright (c) 2026 Oracle and/or its affiliates.
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
package io.helidon.benchmark;

import io.helidon.json.binding.Json;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * Pokemon type entity.
 */
@Json.Entity
@Entity
@Table(name = "TYPE")
public class Type {

    @Id
    @Column(name = "ID")
    @SequenceGenerator(name = "type_seq", sequenceName = "TYPE_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "type_seq")
    private Integer id;

    @Column(name = "NAME", unique = true, nullable = false)
    private String name;

    /**
     * Constructor for a pokemon type.
     *
     * @param id type id
     * @param name type name
     */
    public Type(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Constructor for a new pokemon type with generated id.
     *
     * @param name type name
     */
    public Type(String name) {
        this(null, name);
    }

    /**
     * Default constructor for JPA.
     */
    public Type() {
        this(null, null);
    }

    /**
     * Type id.
     *
     * @return type id
     */
    public Integer getId() {
        return id;
    }

    /**
     * Set type id.
     *
     * @param id type id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Type name.
     *
     * @return type name
     */
    public String getName() {
        return name;
    }

    /**
     * Set type name.
     *
     * @param name type name
     */
    public void setName(String name) {
        this.name = name;
    }
}
