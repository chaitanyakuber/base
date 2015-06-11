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
package com.wotifgroup.dropwizard.poolstatus;

import com.google.common.collect.ImmutableMultimap;
import com.wotifgroup.poolstatusservlet.PoolStatus;
import io.dropwizard.servlets.tasks.Task;
import org.junit.Test;

import java.io.PrintWriter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class EnterPoolTaskTest {

    private final PoolStatus pool = mock(PoolStatus.class);

    private final PrintWriter output = mock(PrintWriter.class);

    private final Task task = new EnterPoolTask(pool);

    @Test
    public void entersPool() throws Exception {
        task.execute(ImmutableMultimap.<String, String> of(), output);

        verify(pool, times(1)).enter();
    }

}
