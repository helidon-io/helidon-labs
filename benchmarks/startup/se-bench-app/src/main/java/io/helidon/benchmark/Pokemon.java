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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * Pokemon entity used by the Helidon Data repository.
 */
@Json.Entity
@Entity
@Table(name = "POKEMON")
public class Pokemon {

    @Id
    @Column(name = "ID")
    @SequenceGenerator(name = "pokemon_seq", sequenceName = "POKEMON_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pokemon_seq")
    private Integer id;

    @Column(name = "NAME", unique = true, nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "TYPE_ID", nullable = false)
    private Type type;

    /**
     * Constructor for a new pokemon instance.
     *
     * @param name pokemon name
     * @param type pokemon type
     */
    public Pokemon(String name, Type type) {
        this.id = null;
        this.name = name;
        this.type = type;
    }

    /**
     * Default constructor for JPA.
     */
    public Pokemon() {
        this(null, null);
    }

    /**
     * Pokemon id.
     *
     * @return pokemon id
     */
    public Integer getId() {
        return id;
    }

    /**
     * Set pokemon id.
     *
     * @param id pokemon id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Pokemon name.
     *
     * @return pokemon name
     */
    public String getName() {
        return name;
    }

    /**
     * Set pokemon name.
     *
     * @param name pokemon name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Pokemon type.
     *
     * @return pokemon type
     */
    public Type getType() {
        return type;
    }

    /**
     * Set pokemon type.
     *
     * @param type pokemon type
     */
    public void setType(Type type) {
        this.type = type;
    }
}
