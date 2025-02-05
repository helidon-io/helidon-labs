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

import com.oracle.coherence.common.base.Logger;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.DistributedCacheService;
import com.tangosol.net.InvocationService;
import com.tangosol.net.Member;
import com.tangosol.net.NamedMap;

import com.tangosol.net.events.EventInterceptor;
import com.tangosol.net.events.application.LifecycleEvent;
import com.tangosol.util.Base;

import java.util.Map;
import java.util.Random;
import java.util.Set;

public class BootstrapInterceptor
        implements EventInterceptor<LifecycleEvent> {

    @Override
    @SuppressWarnings("unchecked")
    public void onEvent(LifecycleEvent event) {
        if (event.getType() == LifecycleEvent.Type.ACTIVATED) {
            int memberId = CacheFactory.getCluster().getLocalMember().getId();

            if (memberId == 1 || isRunningInKubernetes()) {
                // on startup of first member only or if in kubernetes
                NamedMap<String, Task> tasks    = CacheFactory.getCache("tasks");
                boolean                loadData = false;

                // check to see if the data is loaded already if we are in Kubernetes
                if (isRunningInKubernetes()) {
                    if (tasks.isEmpty()) {
                        // wait for a short while in case the two storage members start at exact same time
                        // and if the tasks cache is still zero then load
                        Base.sleep(new Random().nextInt(1000) + 1000L);
                        if (tasks.isEmpty()) {
                            loadData = true;
                        }
                    }
                }
                else if (memberId == 1) {
                    loadData = true;  // we are not in Kubernetes so load the data for 1st member
                }

                if (loadData) {
                    Set<Member> setMembers = ((DistributedCacheService) tasks.getService()).getOwnershipEnabledMembers();

                    if (!setMembers.isEmpty()) {
                        InvocationService invocationService = (InvocationService) CacheFactory.getService("InvocationService");
                        Logger.info("Preloading data from member Id=" + setMembers.iterator().next().getId());
                        Map<Member, String> results = (Map<Member, String>) invocationService.query(new PreloadDataInvocable(),
                                setMembers);
                        results.values().forEach(Logger::info);
                    }
                    else {
                        Logger.warn("Please start Coherence storage member first");
                    }
                }
            }
        }
    }

    public static boolean isRunningInKubernetes() {
        return System.getenv("KUBERNETES_SERVICE_HOST") != null &&
               System.getenv("KUBERNETES_SERVICE_PORT") != null;
    }
}
