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

package io.helidon.labs.todo.backend;

import com.tangosol.util.Filter;
import com.tangosol.util.Filters;
import com.tangosol.util.Processors;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.ws.rs.NotFoundException;
import io.helidon.labs.todo.coherence.Task;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

import static com.tangosol.util.Filters.always;
import static com.tangosol.util.Filters.equal;

@ApplicationScoped
public class TaskService {

  private static final String MESSAGE = "Unable to find task with id ";

  private static final System.Logger LOG = System.getLogger((TaskResource.class.getName()));

  @Inject
  protected TaskRepository tasks;

  public Task createTask(String title) {
    Objects.requireNonNull(title);
    return Optional.ofNullable(tasks.save(new Task(title))).orElseThrow(()-> new IllegalStateException("unable to save task"));
  }

  public Task updateTask(String id, JsonObject task) {
    if(task.containsKey("title")) {
      updateTitle(id, task.getString("title"));
    }
    if(task.containsKey("completed")) {
      updateCompletionStatus(id, task.getBoolean("completed"));
    }
    return tasks.get(id);
  }

  private Task updateTitle(String id, String title) {
    return Optional.ofNullable(tasks.update(id,Task::setTitle, title)).orElseThrow(()-> new NotFoundException((MESSAGE + id)));
  }

  private Task updateCompletionStatus(String id, Boolean completed) {
    return Optional.ofNullable(tasks.update(id,Task::setCompleted, completed)).orElseThrow(()-> new NotFoundException((MESSAGE + id)));
  }

  public Task deleteTask(String id) {
    Task task = new Task(id);
    tasks.removeById(id, true);
    return task;
  }

  public Collection<Task> getAllTasks() {
    return tasks.getAll(Filters.always());
  }

  public Collection<Task> getTasks(Boolean completed) {
    Filter<Task> filter = !completed ? always() : equal(Task::getCompleted, completed);
    return tasks.getAllOrderedBy(filter,Task::getTitle);
  }

  public Task getTask(String id) {
    return Optional.ofNullable(tasks.get(id)).orElseThrow(() -> new NotFoundException(MESSAGE + id));
  }
}
