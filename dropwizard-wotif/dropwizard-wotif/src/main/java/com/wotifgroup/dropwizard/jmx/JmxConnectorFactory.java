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

import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;

import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

/**
 * Factory class for constructing a Jetty JMX ConnectorServer.
 * 
 * Ensures it binds to localhost and that the RMI server also communicates only via localhost. This allows for "security" by not
 * binding to a public interface and allows SSH tunnelling to work for access.
 */
public class JmxConnectorFactory {

    private static final String JMX_SERVICE_URI_PATTERN = "service:jmx:rmi://localhost:%d/jndi/rmi://localhost:%d/jmxrmi";

    private final JmxConnectorConfiguration configuration;

    public JmxConnectorFactory(final JmxConnectorConfiguration configuration) {
        this.configuration = configuration;
    }

    public ManagedJmxConnectorServer build() {
        // Build the connection string with fixed ports
        final String uri = String.format(JMX_SERVICE_URI_PATTERN,
                configuration.getServerPort(),
                configuration.getRegistryPort());

        try {
            JMXServiceURL url = new JMXServiceURL(uri);
            JMXConnectorServer server = JMXConnectorServerFactory.newJMXConnectorServer(url, null,
                    ManagementFactory.getPlatformMBeanServer());

            return new ManagedJmxConnectorServer(configuration.getRegistryPort(), server);
        } catch (MalformedURLException e) {
            throw new JmxConnectorException("Invalid JMX service URL", e);
        } catch (Exception e) {
            throw new JmxConnectorException("Unable to build JMX server.", e);
        }
    }

}
