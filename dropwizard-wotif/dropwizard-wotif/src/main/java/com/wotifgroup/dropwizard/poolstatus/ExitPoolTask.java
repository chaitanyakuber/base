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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;

/**
 * Marks this application as ready to exit the load balancer pool.
 */
public class ExitPoolTask extends Task {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExitPoolTask.class);

    private final PoolStatus pool;

    public ExitPoolTask(final PoolStatus pool) {
        super("exit-pool");
        this.pool = pool;
    }

    @Override
    public void execute(ImmutableMultimap<String, String> parameters, PrintWriter output) throws Exception {
        PoolStatus.State oldState = pool.exit();
        String message = String.format("Exiting pool. Status was %s", oldState);
        LOGGER.info(message);
        output.printf("%s\n", message);
    }

}
