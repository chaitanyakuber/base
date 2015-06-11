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

import com.google.common.collect.ImmutableMultimap;
import com.netflix.hystrix.HystrixCircuitBreaker;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandMetrics;
import io.dropwizard.servlets.tasks.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Manage hystrix circuit breaker
 *
 * <pre>
 *   To view state of all circuit breakers in metrics:
 *     curl -XPOST http://.../tasks/hystrix
 *
 *   To view circuit breaker state of specific commands:
 *     curl -XPOST http://.../tasks/hystrix?command=Command1&command=Command2
 *
 *   To close a circuit breaker:
 *     curl -XPOST http://.../tasks/hystrix?action=close&command=Command1
 * </pre>
 */
// TODO: Add unit tests
public class HystrixTask extends Task {
    private static final Logger LOGGER = LoggerFactory.getLogger(HystrixTask.class);

    private static final String NAME = "hystrix";
    private static final String COMMAND_PARAMETER_NAME = "command";
    private static final String ACTION_PARAMETER_NAME = "action";
    private static final String ACTION_CLOSE = "close";

    protected HystrixTask() {
        super(NAME);
    }

    @Override
    public void execute(ImmutableMultimap<String, String> parameters, PrintWriter writer) throws Exception {
        Collection<HystrixCommandKey> commands = new LinkedList<>();

        if (parameters.containsKey(COMMAND_PARAMETER_NAME)) {
            getCommands(parameters.get(COMMAND_PARAMETER_NAME), commands);
            String action = getFirstParameter(parameters, ACTION_PARAMETER_NAME);
            if (ACTION_CLOSE.equals(action)) {
                closeCircuitBreaker(commands);
            }
        } else {
            getAllCommands(commands);
        }
        printCircuitBreakerStatus(commands, writer);
    }

    private static void getAllCommands(Collection<HystrixCommandKey> output) {
        Collection<HystrixCommandMetrics> metrics = HystrixCommandMetrics.getInstances();
        for (HystrixCommandMetrics metric : metrics) {
            output.add(metric.getCommandKey());
        }
    }

    private static void getCommands(Collection<String> names, Collection<HystrixCommandKey> output) {
        for (String name : names) {
            output.add(HystrixCommandKey.Factory.asKey(name));
        }
    }

    private static void printCircuitBreakerStatus(Collection<HystrixCommandKey> commands, PrintWriter writer) {
        for (HystrixCommandKey command : commands) {
            HystrixCircuitBreaker breaker = HystrixCircuitBreaker.Factory.getInstance(command);
            if (breaker != null) {
                writer.write(command.name());
                writer.write("=");
                writer.write(breaker.isOpen() ? "Open" : "Closed");
                writer.write("\n");
            }
        }
    }

    // Force close circuit breakers
    private static void closeCircuitBreaker(Collection<HystrixCommandKey> commands) {
        for (HystrixCommandKey command : commands) {
            HystrixCircuitBreaker breaker = HystrixCircuitBreaker.Factory.getInstance(command);
            if (breaker != null) {
                LOGGER.info("Closing circuit breaker: {}", command.name());
                breaker.markSuccess();
            }
        }
    }

    private static String getFirstParameter(ImmutableMultimap<String, String> parameters, String name) {
        if (parameters.containsKey(name)) {
            return parameters.get(name).iterator().next();
        }
        return null;
    }

}
