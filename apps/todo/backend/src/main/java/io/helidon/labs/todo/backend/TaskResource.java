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

import com.oracle.coherence.cdi.Name;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.DistributedCacheService;
import com.tangosol.net.InvocationService;
import com.tangosol.net.Member;
import com.tangosol.net.NamedMap;
import io.helidon.labs.todo.coherence.PreloadDataInvocable;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import io.helidon.labs.todo.coherence.Task;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static java.lang.System.Logger.Level.INFO;

@Path("/api/backend")
@ApplicationScoped
public class TaskResource {

  private static final System.Logger LOG = System.getLogger((TaskResource.class.getName()));

  @Inject
  private TaskService svc;

  @Inject
  @Name("tasks")
  private NamedMap<String, Task> tasks;

  @POST
  @Consumes(APPLICATION_JSON)
  @WithSpan ("create_task")
  public Response createTask(JsonObject task) {
    String title = task.getString("title");
    LOG.log(INFO, "Creating new task {0}", task.getString("title"));
    return Response.ok(svc.createTask(title)).build();
  }

  @GET
  @Produces(APPLICATION_JSON)
  @WithSpan("get_tasks")
  public Response getTasks() {
    LOG.log(INFO, "Retrieving all tasks");
    return Response.ok(svc.getAllTasks()).build();
  }

  @PUT
  @Path("{id}")
  @Consumes(APPLICATION_JSON)
  @WithSpan ("update_task")
  public Response updateTask(@PathParam("id") String id, JsonObject task) {
    LOG.log(INFO, "Updating task {0}", id);
    return Response.ok(svc.updateTask(id, task)).build();
  }

  @GET
  @Path("{id}")
  @Produces(APPLICATION_JSON)
  @WithSpan ("get_task")
  public Response getTask(@PathParam("id") String id) {
    LOG.log(INFO, "Getting details for task {0}", id);
    Optional<Task> task = Optional.ofNullable(svc.getTask(id));

    if (task.isPresent()) {
      return Response.ok(task.get()).build();
    }
    else {
      return Response.ok().status(Response.Status.NOT_FOUND).build();
    }
  }

  @DELETE
  @Path("{id}")
  @Produces(APPLICATION_JSON)
  @WithSpan ("delete_task")
  public Response deleteTask(@PathParam("id") String id) {
    try {
      LOG.log(INFO, "Deleting task {0}", id);
      return Response.ok(svc.deleteTask(id)).build();
    }
    catch (NumberFormatException e) {
      return Response.status(Response.Status.NO_CONTENT).build();
    }
  }

  @GET
  @Path("/preload")
  @Produces(APPLICATION_JSON)
  @WithSpan ("preload")
  @SuppressWarnings("unchecked")
  public Response preload() {
    InvocationService invocationService = (InvocationService) CacheFactory.getService("InvocationService");

    // determine the storage enabled members and choose the first one
    Set<Member> storageEnabledMembers = ((DistributedCacheService) tasks.getService()).getOwnershipEnabledMembers();

    if (storageEnabledMembers.isEmpty()) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }

    Set<Member> setMembers = storageEnabledMembers.stream().findFirst().map(Collections::singleton).get();
    LOG.log(INFO, "Preloading member {0}", setMembers);

    // invoke the PreloadDataInvocable on one of the storage-members
    Map<Member, String> results = (Map<Member, String>) invocationService.query(new PreloadDataInvocable(), setMembers);

    return Response.ok(results.values()).build();
  }
}
