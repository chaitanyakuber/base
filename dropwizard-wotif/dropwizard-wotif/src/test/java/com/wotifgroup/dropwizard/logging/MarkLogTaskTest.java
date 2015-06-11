/*
 * #%L
 * dropwizard-wotif
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
package com.wotifgroup.dropwizard.logging;

import com.google.common.collect.ImmutableMultimap;
import io.dropwizard.servlets.tasks.Task;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import static org.junit.Assert.assertEquals;

public class MarkLogTaskTest {

    public static final String TEST_MESSAGE = "Test mark";

    private final Task task = new MarkLogTask();

    @Test
    public void marksLogAtDefaultInfoLevel() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter output = new PrintWriter(baos);
        task.execute(ImmutableMultimap.of("message", TEST_MESSAGE), output);
        assertEquals("Log marked with: '<<MARK>> " + TEST_MESSAGE + "' at INFO level\n", baos.toString());
    }

    @Test
    public void marksLogAtErrorLevel() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter output = new PrintWriter(baos);
        task.execute(ImmutableMultimap.of("message", TEST_MESSAGE, "level", "ERROR"), output);
        assertEquals("Log marked with: '<<MARK>> " + TEST_MESSAGE + "' at ERROR level\n", baos.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsIllegalArgWhenNoMessageParamProvided() throws Exception {
        task.execute(ImmutableMultimap.<String, String>of(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsIllegalArgWhenInvalidLevelProvided() throws Exception {
        task.execute(ImmutableMultimap.of("message", TEST_MESSAGE, "level", "DODGY"), null);
    }

}
