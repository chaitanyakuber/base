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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.container.filter.LoggingFilter;
import io.dropwizard.setup.Environment;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.StaticApplicationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

public class WotifSpringBundleTest {

    private Environment environment;
    private WotifSpringBundle<AppPropertiesConfiguration> bundle;
    private StaticApplicationContext context;

    @Before
    public void setup() {
        environment = new Environment("Test Env", new ObjectMapper(), null, null, null);

        bundle = new WotifSpringBundle<AppPropertiesConfiguration>() {
            @Override
            public ApplicationContext loadSpringContext(AppPropertiesConfiguration configuration, Environment environment) {
                return WotifSpringBundleTest.this.context;
            }
        };
    }

    @Test
    public void testAddResources() throws Exception {
        context = new StaticApplicationContext();
        context.registerSingleton("DummyResource", DummyResource.class);

        bundle.applicationContext = this.context;
        bundle.addResources(environment);

        Set<Object> resources = environment.jersey().getResourceConfig().getSingletons();
        assertTrue(setContainsObjectOfType(DummyResource.class, resources));
    }

    @Test
    public void testAddHealthChecks() throws Exception {
        context = new StaticApplicationContext();
        context.registerSingleton("DummyHealthCheck", DummyHealthCheck.class);

        bundle.applicationContext = this.context;
        bundle.addHealthChecks(environment);

        Set<String> resources = environment.healthChecks().getNames();
        assertTrue(resources.contains("DummyHealthCheck"));
    }

    @Test
    public void testAddExceptionMappers() throws Exception {
        context = new StaticApplicationContext();
        context.registerSingleton("DummyExceptionMapper", DummyExceptionMapper.class);

        bundle.applicationContext = this.context;
        bundle.addExceptionMappers(environment);

        Set<Object> resources = environment.jersey().getResourceConfig().getSingletons();
        assertTrue(setContainsObjectOfType(DummyExceptionMapper.class, resources));
    }

    @Test
    public void testAddRequestFilters() throws Exception {
        context = new StaticApplicationContext();
        context.registerSingleton("LoggingFilter", LoggingFilter.class);

        bundle.applicationContext = this.context;
        bundle.addRequestFilters(environment);

        List resources = environment.jersey().getResourceConfig().getContainerRequestFilters();
        assertTrue(containsLoggingFilter(resources));
    }

    @Test
    public void testAddResponseFilters() throws Exception {
        context = new StaticApplicationContext();
        context.registerSingleton("LoggingFilter", LoggingFilter.class);

        bundle.applicationContext = this.context;
        bundle.addResponseFilters(environment);

        List resources = environment.jersey().getResourceConfig().getContainerResponseFilters();
        assertTrue(containsLoggingFilter(resources));
    }

    @Test
    public void testAddEverything() throws Exception {
        context = new StaticApplicationContext();

        context.registerSingleton("DummyResource", DummyResource.class);
        context.registerSingleton("DummyHealthCheck", DummyHealthCheck.class);
        context.registerSingleton("DummyExceptionMapper", DummyExceptionMapper.class);
        context.registerSingleton("LoggingFilter", LoggingFilter.class);

        bundle.run(setupConfig("some value"), environment);

        Set<Object> resources = environment.jersey().getResourceConfig().getSingletons();

        assertTrue(setContainsObjectOfType(DummyResource.class, resources));
        assertTrue(environment.healthChecks().getNames().contains("DummyHealthCheck"));
        assertTrue(setContainsObjectOfType(DummyExceptionMapper.class, resources));

        assertTrue(containsLoggingFilter(environment.jersey().getResourceConfig().getContainerRequestFilters()));
        assertTrue(containsLoggingFilter(environment.jersey().getResourceConfig().getContainerResponseFilters()));
    }

    @Test
    public void testLoadXMLSpringContext() throws Exception {
        AppPropertiesConfiguration config = setupConfig("some value");

        ApplicationContext context = bundle.loadXMLSpringContext(config, null, "classpath:spring/dummy-context.xml");

        assertThat(context.getBean("dummyBean").equals("some value"));
    }

    @Test
    public void testShouldLoadXMLSpringContextWithExternalProperties() throws Exception {
        AppPropertiesConfiguration config = setupConfig("some value");

        ApplicationContext context = bundle.loadXMLSpringContext(config, null, "classpath:spring/dummy-context-external-properties.xml");

        assertThat(context.getBean("externalPropertiesBean").equals("I am a property in a properties file"));
        assertThat(context.getBean("dummyBean").equals("some value"));
    }

    @Test
    public void testLoadAnnotationSpringContext() throws Exception {
        AppPropertiesConfiguration config = setupConfig("some other value");

        ApplicationContext context = bundle.loadAnnotationSpringContext(config, null, DummyAppConfig.class);

        DummyBean dummyBean = (DummyBean) context.getBean("dummyBean");
        assertThat(dummyBean.getMyInjectedProperty()).isEqualTo("some other value");
    }

    private boolean containsLoggingFilter(List resources) {
        for (Object resource : resources) {
            if (resource instanceof LoggingFilter) {
                return true;
            }
        }
        return false;
    }

    private boolean setContainsObjectOfType(Class resourceClass, Set<Object> resources) {
        for (Object resource : resources) {
            if (resource.getClass() == resourceClass) {
                return true;
            }
        }
        return false;
    }

    private AppPropertiesConfiguration setupConfig(String value) {
        // creates a config with a property "injected.property"
        Map<String, String> prop = new HashMap<>();
        prop.put("property", value);

        Map<String, Object> appProperties = new HashMap<>();
        appProperties.put("injected", prop);

        AppPropertiesConfiguration config = new AppPropertiesConfiguration();
        config.setAppProperties(appProperties);
        return config;
    }
}
