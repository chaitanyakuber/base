/*
 * #%L
 * dropwizard-wotif-spring
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
package com.wotifgroup.dropwizard.spring;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.wotifgroup.dropwizard.WotifService;
import io.dropwizard.jetty.ConnectorFactory;
import io.dropwizard.jetty.HttpsConnectorFactory;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.server.DefaultServerFactory;
import io.dropwizard.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.MutablePropertySources;

import javax.ws.rs.Path;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;

public abstract class WotifSpringService<T extends AppPropertiesConfiguration> extends WotifService<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WotifSpringService.class);

    public WotifSpringService(String applicationName) {
        super(applicationName);
    }

    /**
     * Deprecated: This functionality is now provided in the WotifBundle. To leverage it, move the ketStorePassFile property in your config.yaml
     * from under the AppProperties parent to be a top level property and Ensure your subclass calls super.initialize(Bootstrap b)
     */
    @Deprecated
    protected void configureKeystore(T config) {
        for (ConnectorFactory connector : ((DefaultServerFactory) config.getServerFactory()).getApplicationConnectors()) {
            if (connector instanceof HttpsConnectorFactory) {
                // As of wx 0.7.0 an inline keystore password is mandatory so now our preference it to get the value from a file
                // and fallback to the provided value
                Optional<String> password = getKeyStorePass(config);
                if (password.isPresent()) {
                    ((HttpsConnectorFactory) connector).setKeyStorePassword(password.get());
                }
            }
        }
    }

    private Optional<String> getKeyStorePass(T config) {
        String keyStorePassFilePath = (String) config.getAppProperties().get("keyStorePassFile");
        if (Strings.isNullOrEmpty(keyStorePassFilePath)) {
            LOGGER.warn("keystorepassfile not specified");
            return Optional.absent();
        }
        try {
            return Optional.of(Files.toString(new File(keyStorePassFilePath), StandardCharsets.US_ASCII));
        } catch (IOException e) {
            LOGGER.info("Cannot read keystorepassfile at " + keyStorePassFilePath);
            return Optional.absent();
        }
    }

    /**
     * Load xml spring context.
     */
    protected ApplicationContext loadXMLSpringContext(T config, Environment environment, String... locations) {
        Map<String, Object> beans = createDropwizardBeans(config, environment);
        return loadXMLSpringContext(config, beans, environment, locations);
    }

    /**
     * Load spring xml and inject beans to the context.
     */
    protected ApplicationContext loadXMLSpringContext(
            T config, Map<String, Object> beans, Environment environment, String... locations) {
        LOGGER.info("Loading Spring XML context files: " + Arrays.asList(locations));
        try {
            GenericApplicationContext applicationContext = new GenericApplicationContext();

            // Inject properties
            setSpringProperties(applicationContext, config);

            // Inject beans
            setBeans(beans, applicationContext);

            // load the xml files
            new XmlBeanDefinitionReader(applicationContext).loadBeanDefinitions(locations);
            applicationContext.refresh();

            lifecycleManage(applicationContext, environment);

            return applicationContext;
        } catch (RuntimeException e) {
            LOGGER.error("Failed to load XML context: ", e);
            throw e;
        }
    }

    private void lifecycleManage(final GenericApplicationContext applicationContext, Environment environment) {
        environment.lifecycle().manage(new Managed() {
            public void start() throws Exception {
            }

            public void stop() throws Exception {
                applicationContext.close();
            }
        });
    }

    private void setBeans(Map<String, Object> beans, GenericApplicationContext applicationContext) {
        if (beans != null) {
            ConfigurableBeanFactory beanFactory = applicationContext.getBeanFactory();
            for (Map.Entry<String, Object> entry : beans.entrySet()) {
                beanFactory.registerSingleton(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * @deprecated Use {@link #loadXMLSpringContext(AppPropertiesConfiguration, java.util.Map, io.dropwizard.setup.Environment, String...)} instead.
     */
    @Deprecated
    protected ApplicationContext loadXMLSpringContextWithEnvironment(T config, Environment environment,
                                                      String... locations) {
        Map<String, Object> beans = new LinkedHashMap<>();
        beans.put("dropwizard.environment", environment);
        return loadXMLSpringContext(config, beans, null, locations);
    }

    private ApplicationContext loadAnnotationSpringContext(
            T config, Map<String, Object> beans, Environment environment, Class configClass) {
        LOGGER.info("Loading Spring Annotated context class: " + configClass.getName());
        try {
            AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();

            applicationContext.register(configClass);
            setSpringProperties(applicationContext, config);
            setBeans(beans, applicationContext);

            applicationContext.refresh();
            lifecycleManage(applicationContext, environment);
            return applicationContext;
        } catch (RuntimeException e) {
            LOGGER.error("Failed to load Spring Annotated Java context: ", e);
            throw e;
        }
    }

    protected ApplicationContext loadSpringContext(T config, Environment environment, Class configClass) {
        final Map<String, Object> beans = createDropwizardBeans(config, environment);
        return loadAnnotationSpringContext(config, beans, environment, configClass);
    }

    private Map<String, Object> createDropwizardBeans(T config, Environment environment) {
        final Map<String, Object> beans = new LinkedHashMap<>();
        beans.put("dropwizard.environment", environment);
        beans.put("dropwizard.config", config);
        beans.put("dropwizard.projectId", getProjectId());
        beans.put("dropwizard.poolStatus", getWotifBundle().getPoolStatus());
        return beans;
    }

    protected ApplicationContext loadAnnotationSpringContext(T config, Environment environment, Class configClass) {
        return loadAnnotationSpringContext(config, null, environment, configClass);
    }

    protected void setSpringProperties(AbstractApplicationContext applicationContext, T config) {
        try {
            MutablePropertySources propertySources = new MutablePropertySources();
            propertySources.addLast(new NestedMapPropertySource("YAML properties", config.getAppProperties()));
            PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
            configurer.setPropertySources(propertySources);
            configurer.setIgnoreUnresolvablePlaceholders(true);
            applicationContext.addBeanFactoryPostProcessor(configurer);
        } catch (RuntimeException e) {
            LOGGER.error("Failed to add App Properties to Spring: ", e);
            throw e;
        }
    }

    /**
     * @param env                To which resources will be added.
     * @param applicationContext From which resources will be scanned.
     */
    protected void addResources(Environment env, ApplicationContext applicationContext) {
        try {
            //find all the Resources and add them into the dropwizard environment
            Map<String, Object> resources = applicationContext.getBeansWithAnnotation(Path.class);
            for (Object nextResource : resources.values()) {
                env.jersey().register(nextResource);
            }
        } catch (RuntimeException e) {
            LOGGER.error("Failed to add Resources from Spring context to DropWizard environment: ", e);
            throw e;
        }
    }

    /**
     * @param env                To which health checks will be added.
     * @param applicationContext From which resources will be scanned.
     */
    protected void addHealthChecks(Environment env, ApplicationContext applicationContext) {
        try {
            for (HealthCheck healthCheck : applicationContext.getBeansOfType(HealthCheck.class).values()) {
                env.healthChecks().register(healthCheck.getClass().getSimpleName(), healthCheck);
            }
        } catch (RuntimeException e) {
            LOGGER.error("Failed to add Health Check beans from Spring context to DropWizard environment: ", e);
            throw e;
        }
    }

    /**
     * @param env                To which the exception mappers will be added.
     * @param applicationContext From which the resources will be scanned.
     */
    protected void addExceptionMappers(Environment env, ApplicationContext applicationContext) {
        for (ExceptionMapper exceptionMapper : applicationContext.getBeansOfType(ExceptionMapper.class).values()) {
            env.jersey().register(exceptionMapper);
        }
    }

    /**
     * @param env                To which the request filters will be added.
     * @param applicationContext From which the resources will be scanned.
     */
    protected void addRequestFilters(Environment env, ApplicationContext applicationContext) {
        env.jersey().property(
                ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS,
                newArrayList(applicationContext.getBeansOfType(ContainerRequestFilter.class).values()));
    }

    /**
     * @param env                To which the response filters will be added.
     * @param applicationContext From which the resources will be scanned.
     */
    protected void addResponseFilters(Environment env, ApplicationContext applicationContext) {
        env.jersey().property(
                ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS,
                newArrayList(applicationContext.getBeansOfType(ContainerResponseFilter.class).values()));
    }

    protected void addProviders(Environment env, ApplicationContext applicationContext) {
        for (Object provider : applicationContext.getBeansWithAnnotation(Provider.class).values()) {
            env.jersey().register(provider);
        }
    }

}
