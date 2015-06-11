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
import com.netflix.hystrix.Hystrix;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HystrixTaskTest {

    private HystrixTask task;
    private ImmutableMultimap.Builder<String, String> parametersBuilder;
    private StringWriter stringWriter = new StringWriter();

    @Before
    public void setUp() {
        reset();
        task = new HystrixTask();
    }

    @After
    public void tearDown() {
        Hystrix.reset();
    }

    @Test
    public void shouldPrintStatusOfAllCircuitBreakers() throws Exception {
        new SuccessCommand().execute();
        try {
            new FailingCommand().execute();
        } catch (Exception e) {
            // Ignore
        }
        task.execute(parametersBuilder.build(), new PrintWriter(stringWriter));
        String result = stringWriter.toString();
        assertTrue(result.contains("SuccessCommand=Closed\n"));
        assertTrue(result.contains("FailingCommand=Closed\n"));
    }

    @Test
    public void shouldPrintStatusOfProvidedCircuitBreakers() throws Exception {
        new SuccessCommand().execute();
        for (int i = 0; i < 2; ++i) {
            try {
                new FailingCommand().execute();
            } catch (Exception e) {
                // Ignore
            }
        }
        Thread.sleep(20);

        task.execute(parametersBuilder.put("command", "FailingCommand").build(), new PrintWriter(stringWriter));
        String result = stringWriter.toString();
        assertEquals("FailingCommand=Open\n", result);
    }

    @Test
    public void shouldCloseCircuitBreaker() throws Exception {
        for (int i = 0; i < 2; ++i) {
            try {
                new FailingCommand().execute();
            } catch (Exception e) {
                // Ignore
            }
        }
        Thread.sleep(20);

        task.execute(parametersBuilder.put("command", "FailingCommand").build(), new PrintWriter(stringWriter));
        String result = stringWriter.toString();
        assertEquals("FailingCommand=Open\n", result);

        reset();
        task.execute(parametersBuilder.put("command", "FailingCommand").put("action", "close").build(),
                new PrintWriter(stringWriter));
        result = stringWriter.toString();
        assertEquals("FailingCommand=Closed\n", result);
    }

    private void reset() {
        parametersBuilder = new ImmutableMultimap.Builder<>();
        stringWriter = new StringWriter();
    }
}
