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

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.AppenderAttachableImpl;
import io.dropwizard.jetty.RequestLogFactory;
import io.dropwizard.jetty.Slf4jRequestLog;
import org.eclipse.jetty.server.RequestLog;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class WotifFormatRequestLogFactory extends RequestLogFactory {

    public WotifFormatRequestLogFactory(RequestLogFactory requestLogFactory) {
        setAppenders(requestLogFactory.getAppenders());
    }

    @Override
    @SuppressWarnings("unchecked")
    public RequestLog build(String name) {
        Slf4jRequestLog superRequestLog = (Slf4jRequestLog) super.build(name);
        List<String> cookies = null;

        try {
            Field field = Slf4jRequestLog.class.getDeclaredField("cookies");
            if (field != null) {
                field.setAccessible(true);
                try {
                    cookies = (List<String>) field.get(superRequestLog);
                } catch (IllegalAccessException e) { cookies = new ArrayList<>(); } // cookies is an optional field
            }
        } catch (NoSuchFieldException e) { cookies = new ArrayList<>(); } // cookies is an optional field

        return new WotifFormatRequestLog(getAppenders(superRequestLog), cookies);
    }

    @SuppressWarnings("unchecked")
    private AppenderAttachableImpl<ILoggingEvent> getAppenders(Slf4jRequestLog slf4jRequestLog) {
        try {
            Field field = Slf4jRequestLog.class.getDeclaredField("appenders");
            field.setAccessible(true);
            return (AppenderAttachableImpl<ILoggingEvent>) field.get(slf4jRequestLog);
        } catch (Exception e) {
            throw new IllegalStateException("Error getting appenders from Slf4jRequestLog", e);
        }
    }

}
