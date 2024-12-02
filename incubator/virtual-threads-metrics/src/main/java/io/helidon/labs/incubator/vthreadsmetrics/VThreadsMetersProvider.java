/*
 * Copyright (c) 2024 Oracle and/or its affiliates.
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
package io.helidon.labs.incubator.vthreadsmetrics;

import java.lang.management.ManagementFactory;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import io.helidon.metrics.api.Gauge;
import io.helidon.metrics.api.Meter;
import io.helidon.metrics.api.MetricsFactory;
import io.helidon.metrics.spi.MetersProvider;

import jdk.management.VirtualThreadSchedulerMXBean;

public class VThreadsMetersProvider implements MetersProvider {

    private static final String PREFIX = "vthreads.scheduler.";
    private static final String SCOPE = Meter.Scope.BASE;

    /**
     * For service loading.
     */
    @Deprecated
    public VThreadsMetersProvider() {
    }

    @Override
    public Collection<Meter.Builder<?, ?>> meterBuilders(MetricsFactory metricsFactory) {

        Set<Meter.Builder<?, ?>> result = new HashSet<>();

        VirtualThreadSchedulerMXBean virtualThreadSchedulerMXBean = ManagementFactory.getPlatformMXBean(
                VirtualThreadSchedulerMXBean.class);
        result.add(Gauge.builder(PREFIX + "mounted-virtual-thread-count",
                                 virtualThreadSchedulerMXBean::getMountedVirtualThreadCount)
                           .scope(SCOPE)
                           .description(
                                   "Estimate of the number of virtual threads that are currently mounted by the scheduler; -1 "
                                           + "if not known."));
        result.add(Gauge.builder(PREFIX + "parallelism", virtualThreadSchedulerMXBean::getParallelism)
                           .scope(SCOPE)
                           .description("Scheduler's target parallelism."));
        result.add(Gauge.builder(PREFIX + "pool-size", virtualThreadSchedulerMXBean::getPoolSize)
                           .scope(SCOPE)
                           .description(
                                   "Current number of platform threads that the scheduler has started but have not terminated; "
                                           + "-1 if not known."));
        result.add(Gauge.builder(PREFIX + "queued-virtual-thread-count",
                                 virtualThreadSchedulerMXBean::getQueuedVirtualThreadCount)
                           .scope(SCOPE)
                           .description(
                                   "Estimate of the number of virtual threads that are queued to the scheduler to start or "
                                           + "continue execution; -1 if not known."));

        return result;
    }
}
