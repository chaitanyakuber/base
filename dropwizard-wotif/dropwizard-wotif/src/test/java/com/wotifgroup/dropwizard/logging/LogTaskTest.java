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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import io.dropwizard.servlets.tasks.Task;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LogTaskTest {
    private final LoggerContext loggerContext = mock(LoggerContext.class);
    private final Logger logger = (Logger) LoggerFactory.getLogger(LogTaskTest.class);
    private final Logger logger2 = (Logger) LoggerFactory.getLogger(LogTask.class);
    private final String loggerName = LogTaskTest.class.getName();
    private final String logger2Name = LogTask.class.getName();

    private final PrintWriter output = mock(PrintWriter.class);
    private Task task = new LogTask(this.loggerContext);

    @Test
    public void listLogs() throws Exception {
        logger.setLevel(Level.ERROR);
        logger2.setLevel(Level.DEBUG);
        when(loggerContext.getLoggerList()).thenReturn(Lists.newArrayList(logger, logger2));

        task.execute(ImmutableMultimap.<String, String> of(), output);

        verify(output, times(1)).printf(loggerName + ",ERROR\n");
        verify(output, times(1)).printf(logger2Name + ",DEBUG\n");
        verify(loggerContext, times(1)).getLoggerList();
    }

    @Test
    public void getLog() throws Exception {
        logger.setLevel(Level.ERROR);
        when(loggerContext.exists(loggerName)).thenReturn(logger);

        task.execute(ImmutableMultimap.<String, String> of("log", loggerName), output);

        verify(loggerContext, times(1)).exists(loggerName);
        verify(output, times(1)).printf(loggerName + ",ERROR");
    }

    @Test
    public void setLog() throws Exception {
        
        // We need to use the real loggerContext because getLogger is final mockito-no-like-final-methods.
        LoggerContext lc = new LoggerContext(); 
        Task task = new LogTask(lc);

        task.execute(ImmutableMultimap.<String, String> of("log", loggerName, "level", "INFO"), output);
        assertEquals(lc.getLogger(LogTaskTest.class).getLevel(), Level.INFO);
    }

}
