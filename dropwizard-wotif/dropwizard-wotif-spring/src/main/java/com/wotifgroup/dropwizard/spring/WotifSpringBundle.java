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
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.servlets.tasks.Task;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.MutablePropertySources;

import javax.ws.rs.Path;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;

public abstract class WotifSpringBundle<T extends AppPropertiesConfiguration> implements ConfiguredBundle<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(WotifSpringBundle.class);

    protected ApplicationContext applicationContext;

    /**
     * Can use either {@link #loadXMLSpringContext} or {@link #loadAnnotationSpringContext} to create a spring context
     */
    abstract protected ApplicationContext loadSpringContext(T configuration, Environment environment)
            throws RuntimeException;

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
        // Do nothing
    }

    @Override
    public void run(T configuration, Environment environment) throws Exception {
        applicationContext = loadSpringContext(configuration, environment);
        if (applicationContext == null) {
            LOGGER.error("Could not get Spring application context, Spring resources will not be added.");
            return;
        }
        if (applicationContext instanceof ConfigurableApplicationContext) {
            manageSpringContext((ConfigurableApplicationContext)applicationContext, environment);
        }
        addAllSpringBeans(configuration, environment);
    }

    /**
     * Add all Spring resources into current environment.
     */
    protected void addAllSpringBeans(T configuration, Environment environment) {
        addResources(environment);
        addHealthChecks(environment);
        addTasks(environment);
        addManageds(environment);
        addProviders(environment);
        addExceptionMappers(environment);
        addRequestFilters(environment);
        addResponseFilters(environment);
    }

    /**
     * @param env                To which resources will be added.
     */
    protected void addResources(Environment env) {
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

    protected void addHealthChecks(Environment env) {
        try {
            for (Map.Entry<String, HealthCheck> entry : applicationContext.getBeansOfType(HealthCheck.class).entrySet()) {
                env.healthChecks().register(entry.getKey(), entry.getValue());
            }
        } catch (RuntimeException e) {
            LOGGER.error("Failed to add Health Check beans from Spring context to DropWizard environment: ", e);
            throw e;
        }
    }

    protected void addTasks(Environment env) {
        for (Task task : applicationContext.getBeansOfType(Task.class).values()) {
            env.admin().addTask(task);
        }
    }

    protected void addManageds(Environment env) {
        for (Managed managed : applicationContext.getBeansOfType(Managed.class).values()) {
            env.lifecycle().manage(managed);
        }
    }

    protected void addExceptionMappers(Environment env) {
        for (ExceptionMapper exceptionMapper : applicationContext.getBeansOfType(ExceptionMapper.class).values()) {
            env.jersey().register(exceptionMapper);
        }
    }

    protected void addRequestFilters(Environment env) {
        Collection<ContainerRequestFilter> filters = applicationContext.getBeansOfType(ContainerRequestFilter.class).values();
        if (!filters.isEmpty()) {
            env.jersey().property(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, newArrayList(filters));
        }
    }

    protected void addResponseFilters(Environment env) {
        Collection<ContainerResponseFilter> filters = applicationContext.getBeansOfType(ContainerResponseFilter.class).values();
        if (!filters.isEmpty()) {
            env.jersey().property(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS, newArrayList(filters));
        }
    }

    protected void addProviders(Environment env) {
        for (Object provider : applicationContext.getBeansWithAnnotation(Provider.class).values()) {
            env.jersey().register(provider);
        }
    }

    protected Map<String, Object> createDefaultBeans(T config, Environment environment) {
        final Map<String, Object> beans = new LinkedHashMap<>();

        beans.put("dropwizard.environment", environment);
        beans.put("dropwizard.config", config);
        return beans;
    }

    // Helpers

    /**
     * Load Spring context from xml files.
     * @return Spring application context
     */
    protected ApplicationContext loadXMLSpringContext(
            T config, Map<String, Object> beans, String... locations) throws RuntimeException {
        LOGGER.info("Loading Spring XML context files: " + Arrays.asList(locations));
        try {
            GenericApplicationContext applicationContext = new GenericApplicationContext();

            // Inject properties
            setSpringProperties(applicationContext, config);

            // Inject beans
            setBeans(applicationContext, beans);

            // load the xml files
            new XmlBeanDefinitionReader(applicationContext).loadBeanDefinitions(locations);
            applicationContext.refresh();

            return applicationContext;
        } catch (RuntimeException e) {
            LOGGER.error("Failed to load XML context: ", e);
            throw e;
        }
    }

    /**
     * Load Spring context from annotation class.
     * @return Spring application context
     */
    protected ApplicationContext loadAnnotationSpringContext(
            T config, Map<String, Object> beans, Class<?>... annotatedClasses)
            throws RuntimeException {
        LOGGER.info("Loading Spring Annotated context classes: " + Arrays.asList(annotatedClasses));

        try {
            AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();

            applicationContext.register(annotatedClasses);
            setSpringProperties(applicationContext, config);
            setBeans(applicationContext, beans);

            applicationContext.refresh();
            return applicationContext;
        } catch (RuntimeException e) {
            LOGGER.error("Failed to load Spring Annotated Java context: ", e);
            throw e;
        }
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

    protected void setBeans(GenericApplicationContext applicationContext, Map<String, Object> beans) {
        if (beans != null) {
            ConfigurableBeanFactory beanFactory = applicationContext.getBeanFactory();
            for (Map.Entry<String, Object> entry : beans.entrySet()) {
                beanFactory.registerSingleton(entry.getKey(), entry.getValue());
            }
        }
    }

    public static class Builder<T extends AppPropertiesConfiguration> {
        /**
         * Load Spring from XML files
         */
        public WotifSpringBundle<T> build(final String... locations) {
            return new WotifSpringBundle<T>() {
                @Override
                protected ApplicationContext loadSpringContext(T configuration, Environment environment) {
                    Map<String, Object> beans = createDefaultBeans(configuration, environment);
                    return loadXMLSpringContext(configuration, beans, locations);
                }
            };
        }

        public WotifSpringBundle<T> build(final Class<?>... annotatedClasses) {
            return new WotifSpringBundle<T>() {
                @Override
                public ApplicationContext loadSpringContext(T configuration, Environment environment) {
                    Map<String, Object> beans = createDefaultBeans(configuration, environment);
                    return loadAnnotationSpringContext(configuration, beans, annotatedClasses);
                }
            };
        }
    }

    private static void manageSpringContext(final ConfigurableApplicationContext applicationContext, Environment environment) {
        environment.lifecycle().manage(new Managed() {
            public void start() throws Exception {
            }
            public void stop() throws Exception {
                applicationContext.close();
            }
        });
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

}
