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

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import io.dropwizard.servlets.tasks.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;

/**
 * Adds an arbitrary entry to the application log at INFO level. Useful for operational activities.
 */
public class MarkLogTask extends Task {

    private static final String MESSAGE_QUERY_PARAM = "message";
    private static final String LEVEL_QUERY_PARAM = "level";
    private static final Logger LOGGER = LoggerFactory.getLogger(MarkLogTask.class);

    public MarkLogTask() {
        super("marklog");
    }

    @Override
    public void execute(ImmutableMultimap<String, String> parameters, PrintWriter output) throws Exception {
        String message = getMessageParam(parameters);
        String level = getLevelParam(parameters);
        switch (level.toUpperCase()) {
            case "TRACE":
                LOGGER.trace(message);
                break;
            case "DEBUG":
                LOGGER.debug(message);
                break;
            case "INFO":
                LOGGER.info(message);
                break;
            case "WARN":
                LOGGER.warn(message);
                break;
            case "ERROR":
                LOGGER.error(message);
                break;
            default:
                throw new IllegalArgumentException(String.format("Unsupported log level '%s'.", level));
        }

        output.printf("Log marked with: '%s' at %s level\n", message, level);
        output.flush();
    }

    private String getMessageParam(ImmutableMultimap<String, String> parameters) {
        Optional<String> messageWrapper = getLastParamValue(parameters, MESSAGE_QUERY_PARAM);
        if (!messageWrapper.isPresent()) {
            throw new IllegalArgumentException("No 'message' query parameter provided for marklog.");
        }
        return String.format("<<MARK>> %s", messageWrapper.get());
    }

    private String getLevelParam(ImmutableMultimap<String, String> parameters) {
        Optional<String> levelWrapper = getLastParamValue(parameters, LEVEL_QUERY_PARAM);
        String level = "";
        if (levelWrapper.isPresent()) {
            level = levelWrapper.get().toUpperCase();
        } else {
            level = "INFO";
        }
        return level;
    }

    private Optional<String> getLastParamValue(ImmutableMultimap<String, String> parameters, String queryParam) {
        ImmutableCollection<String> values = parameters.get(queryParam);
        if (values.isEmpty()) {
            return Optional.absent();
        }
        ImmutableList<String> valuesList = values.asList();
        return Optional.of(valuesList.get(valuesList.size() - 1));
    }

}
