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
import io.dropwizard.servlets.tasks.Task;
import org.slf4j.ILoggerFactory;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * RPC for logback config
 */
public class LogTask extends Task {
    private final LoggerContext loggerContext;

    public LogTask(final LoggerContext loggerContext) {
        super("log");
        this.loggerContext = loggerContext;
    }

    public LogTask(ILoggerFactory loggerFactory) {
        this((LoggerContext) loggerFactory);
    }

    @Override
    public void execute(ImmutableMultimap<String, String> parameters,
            PrintWriter output) throws Exception {
        if (parameters.containsKey("log") && parameters.containsKey("level")) {
            String log = parameters.get("log").iterator().next();
            String level = parameters.get("level").iterator().next();
            setLoggerLevel(log, level);
        } else if (parameters.containsKey("log")) {
            String log = parameters.get("log").iterator().next();
            output.printf(log + "," + getLoggerLevel(log));
        } else {
            for (Map.Entry<String, String> entry : getLoggers().entrySet()) {
                output.printf(entry.getKey() + "," + entry.getValue() + "\n");
            }
        }
    }

    public void setLoggerLevel(String loggerName, String levelStr) {
        if (loggerName == null) {
            return;
        }
        if (levelStr == null) {
            return;
        }
        loggerName = loggerName.trim();
        levelStr = levelStr.trim();

        Logger logger = loggerContext.getLogger(loggerName);
        if ("null".equalsIgnoreCase(levelStr)) {
            logger.setLevel(null);
        } else {
            Level level = Level.toLevel(levelStr, null);
            if (level != null) {
                logger.setLevel(level);
            }
        }
    }

    public String getLoggerLevel(String loggerName) {
        loggerName = loggerName.trim();
        Logger logger = loggerContext.exists(loggerName);
        if (logger != null && logger.getLevel() != null) {
            return logger.getLevel().toString();
        } else {
            return null;
        }
    }

    public Map<String, String> getLoggers() {
        Map<String, String> map = new HashMap<>();
        for (Logger logger : loggerContext.getLoggerList()) {
            map.put(logger.getName(), logger.getLevel() == null ? "null" : logger.getLevel().toString());
        }
        return map;
    }
}
