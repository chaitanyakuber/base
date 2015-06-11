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
package com.wotifgroup.dropwizard;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.wotifgroup.dropwizard.jmx.JmxConnectorFactory;
import com.wotifgroup.dropwizard.jmx.ManagedJmxConnectorServer;
import com.wotifgroup.dropwizard.logging.CardMaskingConfigurer;
import com.wotifgroup.dropwizard.logging.LogTask;
import com.wotifgroup.dropwizard.logging.MarkLogTask;
import com.wotifgroup.dropwizard.logging.StartupShutdownLogger;
import com.wotifgroup.dropwizard.logging.WotifFormatRequestLogFactory;
import com.wotifgroup.dropwizard.poolstatus.EnterPoolTask;
import com.wotifgroup.dropwizard.poolstatus.ExitPoolTask;
import com.wotifgroup.dropwizard.tracing.RequestTracingFilter;
import com.wotifgroup.dropwizard.version.ProjectId;
import com.wotifgroup.dropwizard.version.WhoAmITask;
import com.wotifgroup.poolstatusservlet.PoolStatus;
import com.wotifgroup.poolstatusservlet.PoolStatusServlet;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.jetty.ConnectorFactory;
import io.dropwizard.jetty.HttpsConnectorFactory;
import io.dropwizard.server.AbstractServerFactory;
import io.dropwizard.server.DefaultServerFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;

/**
 * Bundle of standard functionality for Wotif Dropwizard applications.
 */
public class WotifBundle implements ConfiguredBundle<WotifConfiguration> {

    private final ProjectId projectId;
    private PoolStatus poolStatus;
    private CardMaskingConfigurer cardMaskingConfigurer = new CardMaskingConfigurer();

    public WotifBundle(ProjectId projectId) {
        this.projectId = projectId;
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
        // Was disabled by default in DropWizard 0.6.2 and prior (in com.yammer.dropwizard.json.ObjectMapperFactory), but is
        // apparently no longer disabled by default from 0.7.0 (see io.dropwizard.jackson.Jackson#newObjectMapper).
        // Enabling this when clients upgrade is dangerous, and we generally don't want to do this anyway for better forwards
        // compatibility: services adding new fields to responses shouldn't cause client failures.
        bootstrap.getObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    @Override
    public void run(WotifConfiguration configuration, Environment environment) throws Exception {
        tracing(environment);
        jmx(configuration, environment);
        poolStatus(environment);
        logging(environment);
        setWotifFormatRequestLog(configuration);
        version(environment);
        configureKeyStore(configuration);

    }

    private void configureKeyStore(WotifConfiguration configuration) {
        // Ugly hack: if a key store password file is present, use the password therein to configure and https connectors
        // that have been declared in the dropwizard configuration.
        if (configuration.getKeyStorePassFile().isPresent()) {
            final String keyStorePassFilePath = configuration.getKeyStorePassFile().get();
            if (Strings.isNullOrEmpty(keyStorePassFilePath)) {
                throw new IllegalStateException("Neither a keyStorePass nor keyStorePassFile provided!");
            }
            try {
                final String keyStorePassword = Files.toString(new File(keyStorePassFilePath), StandardCharsets.US_ASCII);
                for (ConnectorFactory connector :
                        ((DefaultServerFactory) configuration.getServerFactory()).getApplicationConnectors()) {
                    if (connector instanceof HttpsConnectorFactory) {
                        ((HttpsConnectorFactory) connector).setKeyStorePassword(keyStorePassword);
                    }
                }

            } catch (IOException e) {
                throw new RuntimeException("Error reading keystorepassfile at " + keyStorePassFilePath, e);
            }

        }

    }

    private void tracing(Environment environment) {
        environment.servlets().addFilter("request tracing", RequestTracingFilter.class)
                .addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "/*");
    }

    private void jmx(WotifConfiguration configuration, Environment environment) {
        final JmxConnectorFactory jmxConnectorFactory = new JmxConnectorFactory(configuration.getJmxConnectorConfiguration());
        final ManagedJmxConnectorServer jmxConnector = jmxConnectorFactory.build();

        environment.lifecycle().manage(jmxConnector);
    }

    private void poolStatus(Environment environment) {
        poolStatus = new PoolStatus();
        environment.admin().addTask(new EnterPoolTask(getPoolStatus()));
        environment.admin().addTask(new ExitPoolTask(getPoolStatus()));
        environment.servlets().addServlet("pool_status.txt", new PoolStatusServlet(getPoolStatus()))
                .addMapping("/pool_status.txt");
    }

    private void logging(Environment environment) {
        final Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        environment.admin().addTask(new LogTask(root.getLoggerContext()));
        environment.admin().addTask(new MarkLogTask());
        environment.lifecycle().manage(new StartupShutdownLogger(environment.getName()));
        cardMaskingConfigurer.addCardMasking(root);
    }

    private void setWotifFormatRequestLog(WotifConfiguration configuration) {
        AbstractServerFactory serverFactory = (AbstractServerFactory) configuration.getServerFactory();
        serverFactory.setRequestLogFactory(new WotifFormatRequestLogFactory(serverFactory.getRequestLogFactory()));
    }

    private void version(final Environment environment) {
        environment.admin().addTask(new WhoAmITask(projectId));
    }

    public PoolStatus getPoolStatus() {
        return poolStatus;
    }

    public ProjectId getProjectId() {
        return projectId;
    }

}
