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

package io.helidon.labs.todo.coherence;

import com.tangosol.io.pof.schema.annotation.PortableType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;
import java.util.UUID;

@PortableType(id = 1001)
@Entity
@Table(name="TASKS")
public class Task  {

  @Id
  @Column(name = "ID")
  String id;

  @Column(name = "TITLE")
  String title;

  @Column(name = "COMPLETED")
  Boolean completed;

  public Task() {

  }

  public Task(String title) {
    this.id = UUID.randomUUID().toString().substring(0, 6);
    this.title = title;
    this.completed = false;
  }

  public Task(String id, String title, Boolean completed) {
    this.id = id;
    this.title = title;
    this.completed = completed;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public Task setTitle(String title) {
    this.title = title;
    return this;
  }

  public Boolean getCompleted() {
    return completed;
  }

  public Task setCompleted(Boolean completed) {
    this.completed = completed;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Task task = (Task) o;
    return completed == task.completed && Objects.equals(id, task.id) && Objects.equals(title, task.title);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, title, completed);
  }

  @Override
  public String toString() {
    return "Task{" +
            "id='" + id + '\'' +
            ", title='" + title + '\'' +
            ", completed=" + completed +
            '}';
  }
}
