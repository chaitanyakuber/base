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
package com.wotifgroup.dropwizard.jmx;

import java.rmi.registry.LocateRegistry;

import javax.management.remote.JMXConnectorServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dropwizard.lifecycle.Managed;

/**
 * Wraps a JMX ConnectorServer so it can be managed by Dropwizard.
 */
public class ManagedJmxConnectorServer implements Managed {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagedJmxConnectorServer.class);

    private final int registryPort;

    private final JMXConnectorServer server;

    public ManagedJmxConnectorServer(int registryPort, JMXConnectorServer server) {
        this.registryPort = registryPort;
        this.server = server;
    }

    @Override
    public void start() throws Exception {

        // If registry already exists for this JVM, you especially hit this in unit/acceptance testing cases
        // No elegant way to check I could find, so catch and swallow
        // You could also unexport it on stop() UnicastRemoteObject.unexportObject(<ref>, true);
        // Try/catch change probably lowest impact
        try {
            LocateRegistry.createRegistry(registryPort);
        } catch (java.rmi.server.ExportException ex) {
            if (!ex.getMessage().equals("internal error: ObjID already in use")) {
                throw ex;
            }
        }

        server.start();
        LOGGER.info("JMX remote URL: {}", server.getAddress());
    }

    @Override
    public void stop() throws Exception {

        server.stop();
    }

}
