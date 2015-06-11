/*
 * #%L
 * dropwizard-wotif-hystrix
 * %%
 * Copyright (C) 2015 Wotif Group
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.wotifgroup.dropwizard.hystrix;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.netflix.hystrix.HystrixCircuitBreaker;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandMetrics;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Healthcheck will get list of commands with given name and report unhealthy if
 * circuit breaker of a command is open. If commands is not provided, all
 * available commands from metrics will be used.
 *
 * TODO: Add unit tests
 */
public class HystrixCircuitBreakersHealthCheck extends HealthCheck {
    private final Optional<Collection<String>> criticalCommands;

    public HystrixCircuitBreakersHealthCheck() {
        this.criticalCommands = Optional.absent();
    }

    /**
     * @param criticalCommands Name of commands that will fail health check if its circuit breaker is open
     */
    public HystrixCircuitBreakersHealthCheck(Collection<String> criticalCommands) {
        this.criticalCommands = Optional.fromNullable(criticalCommands);
    }

    @Override
    protected Result check() throws Exception {
        boolean isHealthy = true;
        Map<String, String> status = new LinkedHashMap<>();

        Collection<HystrixCommandKey> commands = getCommands();
        for (HystrixCommandKey command : commands) {
            HystrixCircuitBreaker breaker = HystrixCircuitBreaker.Factory.getInstance(command);
            if (breaker != null) {
                if (breaker.isOpen()) {
                    // Assume unhealthy if this circuit breaker is open and command is in the critical list
                    if (criticalCommands.isPresent() && criticalCommands.get().contains(command.name())) {
                        isHealthy = false;
                    }
                    status.put(command.name(), "Open");
                } else {
                    status.put(command.name(), "Closed");
                }
            }
        }
        String message = Joiner.on(", ").withKeyValueSeparator("=").join(status);
        // Should have WARNING when a circuit breaker is open
        if (status.containsValue("Open")) {
            message = "WARNING " + message;
        }
        return isHealthy ? Result.healthy(message) : Result.unhealthy(message);
    }

    protected Collection<HystrixCommandKey> getCommands() {
        Collection<HystrixCommandKey> commands = new LinkedList<>();

        Collection<HystrixCommandMetrics> metrics = HystrixCommandMetrics.getInstances();
        if (criticalCommands.isPresent()) {
            for (String name : criticalCommands.get()) {
                commands.add(HystrixCommandKey.Factory.asKey(name));
            }
            // Now append other commands
            for (HystrixCommandMetrics metric : metrics) {
                if (!criticalCommands.get().contains(metric.getCommandKey().name())) {
                    commands.add(metric.getCommandKey());
                }
            }
        } else {
            // Just insert all commands from metrics
            for (HystrixCommandMetrics metric : metrics) {
                commands.add(metric.getCommandKey());
            }
        }
        return commands;
    }
}
