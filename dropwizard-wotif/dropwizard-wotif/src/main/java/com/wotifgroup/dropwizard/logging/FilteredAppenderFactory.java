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

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.ImmutableSet;
import io.dropwizard.logging.AppenderFactory;
import org.apache.commons.lang.StringUtils;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * A FileAppenderFactory with filters by logger name.
 * If 'includes' is specified, only logs on loggers whose name is in that comma-separated list will be allowed.
 * If 'excludes' is specified, any logs on loggers whose name is in that comma-separated list will be excluded.
 */
@JsonTypeName("filtered")
public class FilteredAppenderFactory implements AppenderFactory {

    @Valid
    @NotNull
    private AppenderFactory appenderFactory;

    private List<String> includes;
    private List<String> excludes;

    @JsonProperty
    public AppenderFactory getAppender() {
        return appenderFactory;
    }

    @JsonProperty
    public void setAppender(AppenderFactory appender) {
        this.appenderFactory = appender;
    }

    @JsonProperty
    public String getIncludes() {
        return StringUtils.join(includes, ",");
    }

    @JsonProperty
    public void setIncludes(String includes) {
        this.includes = Arrays.asList(StringUtils.split(includes, ","));
    }

    @JsonProperty
    public String getExcludes() {
        return StringUtils.join(excludes, ",");
    }

    @JsonProperty
    public void setExcludes(String excludes) {
        this.excludes = Arrays.asList(StringUtils.split(excludes, ","));
    }

    @Override
    public Appender<ILoggingEvent> build(LoggerContext context, String applicationName, Layout<ILoggingEvent> layout) {
        final Set<String> includesSet = includes != null ? ImmutableSet.copyOf(includes) : Collections.<String>emptySet();
        final Set<String> excludeSet = excludes != null ? ImmutableSet.copyOf(excludes) : Collections.<String>emptySet();
        Appender<ILoggingEvent> appender = appenderFactory.build(context, applicationName, layout);
        appender.addFilter(new Filter<ILoggingEvent>() {
            @Override
            public FilterReply decide(ILoggingEvent iLoggingEvent) {
                if (!includesSet.isEmpty() && !includesSet.contains(iLoggingEvent.getLoggerName())) {
                    return FilterReply.DENY;
                } else if (excludeSet.contains(iLoggingEvent.getLoggerName())) {
                    return FilterReply.DENY;
                } else {
                    return FilterReply.NEUTRAL;
                }
            }
        });
        return appender;
    }
}
