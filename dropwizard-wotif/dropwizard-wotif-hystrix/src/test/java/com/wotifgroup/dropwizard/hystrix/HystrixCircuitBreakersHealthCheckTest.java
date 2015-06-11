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
import com.netflix.hystrix.Hystrix;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HystrixCircuitBreakersHealthCheckTest {
    private HystrixCircuitBreakersHealthCheck healthCheck;

    @Before
    public void setUp() {
        healthCheck = new HystrixCircuitBreakersHealthCheck(Arrays.asList("FailingCommand", "SuccessCommand"));
    }

    @After
    public void tearDown() {
        Hystrix.reset();
    }

    @Test
    public void shouldBeHealthyIfAllClosed() throws Exception {
        new SuccessCommand().execute();
        HealthCheck.Result result = healthCheck.check();
        assertTrue(result.isHealthy());
        assertEquals("SuccessCommand=Closed", result.getMessage());
    }

    @Test
    public void shouldBeUnhealthyIfOneIsOpen() throws Exception {
        new SuccessCommand().execute();
        // Threshold for failing command is 2
        for (int i = 0; i < 2; ++i) {
            try {
                new FailingCommand().execute();
            } catch (Exception e) {
                // ignore
            }
        }
        // Wait for metrics data be collected
        Thread.sleep(20);

        HealthCheck.Result result = healthCheck.check();
        assertFalse(result.isHealthy());
        assertEquals("WARNING FailingCommand=Open, SuccessCommand=Closed", result.getMessage());
    }
}
