/*
 * #%L
 * pool-status-servlet
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
package com.wotifgroup.poolstatusservlet;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.rules.ExternalResource;

import javax.servlet.Servlet;
import java.util.HashMap;
import java.util.Map;

public final class RunWithWebServer extends ExternalResource {

    public static final class Builder {

        private final RunWithWebServer target;

        public Builder() {
            target = new RunWithWebServer();
        }

        public Builder with(final Servlet servlet, final String path) {
            target.addServlet(servlet, path);
            return this;
        }

        public RunWithWebServer build() {
            return target;
        }

    }

    private final Map<Servlet, String> servlets;

    private Server server;

    private RunWithWebServer() {
        servlets = new HashMap<>();
    }

    public void addServlet(final Servlet servlet, final String path) {
        servlets.put(servlet, path);
    }

    @Override
    protected void before() throws Throwable {
        server = new Server(0);

        final ServletHandler handler = new ServletHandler();
        server.setHandler(handler);

        for (Map.Entry<Servlet, String> entry : servlets.entrySet()) {
            handler.addServletWithMapping(new ServletHolder(entry.getKey()), entry.getValue());
        }

        server.start();
    }

    @Override
    protected void after() {
        try {
            if (server != null) {
                server.stop();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to stop server.", e);
        }
    }

    public int port() {
        return ((ServerConnector) server.getConnectors()[0]).getLocalPort();
    }

}
