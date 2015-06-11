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

import com.netflix.hystrix.contrib.codahalemetricspublisher.HystrixCodaHaleMetricsPublisher;
import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet;
import com.netflix.hystrix.strategy.HystrixPlugins;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.util.Collection;

public class WotifHystrixBundle<T> implements ConfiguredBundle<T> {
    private static final String STREAM_NAME = "Hystrix Stream";
    private static final String STREAM_URL = "/metrics/hystrix.stream";
    private static final String CIRCUIT_BREAKERS_HEALTHCHECK_NAME = "HystrixCircuitBreakers";

    private final Collection<String> heathCheckCommands;

    /**
     * By default, the heath check included in the bundle does not report unhealthy if a circuit breaker is open.
     * If a status of a circuit breaker would affect health check, specify its name in
     * {@link WotifHystrixBundle#WotifHystrixBundle(java.util.Collection)}
     */
    public WotifHystrixBundle() {
        heathCheckCommands = null;
    }

    /**
     * Also add health check for circuit breaker of given commands.
     */
    public WotifHystrixBundle(Collection<String> heathCheckCommands) {
        this.heathCheckCommands = heathCheckCommands;
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
        // Do nothing
    }

    @Override
    public void run(T configuration, Environment environment) {
        HystrixPlugins.getInstance().registerMetricsPublisher(new HystrixCodaHaleMetricsPublisher(environment.metrics()));

        HystrixMetricsStreamServlet servlet = new HystrixMetricsStreamServlet();
        environment.admin().addServlet(STREAM_NAME, servlet).addMapping(STREAM_URL);
        environment.lifecycle().addLifeCycleListener(new HystrixServletManager(servlet));

        // HystrixTask need to be reviewed before adding into the bundle
        environment.admin().addTask(new HystrixTask());

        environment.healthChecks().register(CIRCUIT_BREAKERS_HEALTHCHECK_NAME,
                new HystrixCircuitBreakersHealthCheck(heathCheckCommands));
    }
}
