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
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class WotifSpringServiceTest {

    private WotifSpringService service;
    private Environment environment;

    @Before
    public void setup() {
        setupService();
    }

    @SuppressWarnings("unchecked")
    private void setupService() {
        environment = new Environment("Test Env", new ObjectMapper(), null, null, null);
        service = new WotifSpringService<AppPropertiesConfiguration>("test-application") {
            @Override
            public void run(AppPropertiesConfiguration appPropertiesConfiguration, Environment environment) {
                // Do Nothing
            }
        };
        service.initialize(mock(Bootstrap.class));
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

    @Test
    public void testLoadXMLSpringContext() throws Exception {
        AppPropertiesConfiguration config = setupConfig("some value");

        ApplicationContext context = service.loadXMLSpringContext(config, environment, "classpath:spring/dummy-context.xml");

        assertThat(context.getBean("dummyBean").equals("some value"));
    }

    @Test
    public void testShouldLoadXMLSpringContextWithExternalProperties() throws Exception {
        AppPropertiesConfiguration config = setupConfig("some value");

        ApplicationContext context = service.loadXMLSpringContext(config, environment, "classpath:spring/dummy-context-external-properties.xml");

        assertThat(context.getBean("externalPropertiesBean").equals("I am a property in a properties file"));
        assertThat(context.getBean("dummyBean").equals("some value"));
    }

    @Test
    public void testLoadAnnotationSpringContext() throws Exception {
        AppPropertiesConfiguration config = setupConfig("some other value");

        ApplicationContext context = service.loadAnnotationSpringContext(config, environment, DummyAppConfig.class);

        DummyBean dummyBean = (DummyBean) context.getBean("dummyBean");
        assertThat(dummyBean.getMyInjectedProperty()).isEqualTo("some other value");

    }

    @Test
     public void testAddResources() throws Exception {
        AppPropertiesConfiguration config = setupConfig("some other value");
        Environment env = new Environment("Test Env", new ObjectMapper(), null, null, null);
        ApplicationContext context = service.loadAnnotationSpringContext(config, environment, DummyAppConfig.class);

        service.addResources(env, context);

        Set<Object> resources = env.jersey().getResourceConfig().getSingletons();
        assertThat(setContainsObjectOfType(DummyResource.class, resources)).isTrue();
    }

    @Test
    public void testAddHealthChecks() throws Exception {
        AppPropertiesConfiguration config = setupConfig("some other value");
        Environment env = new Environment("Test Env", new ObjectMapper(), null, null, null);
        ApplicationContext context = service.loadAnnotationSpringContext(config, environment, DummyAppConfig.class);

        service.addHealthChecks(env, context);

        Set<String> resources = env.healthChecks().getNames();
        assertThat(resources.contains("DummyHealthCheck")).isTrue();
    }

    @Test
    public void testAddExceptionMappers() throws Exception {
        AppPropertiesConfiguration config = setupConfig("some other value");
        Environment env = new Environment("Test Env", new ObjectMapper(), null, null, null);
        ApplicationContext context = service.loadAnnotationSpringContext(config, environment, DummyAppConfig.class);

        service.addExceptionMappers(env, context);

        Set<Object> resources = env.jersey().getResourceConfig().getSingletons();
        assertThat(setContainsObjectOfType(DummyExceptionMapper.class, resources)).isTrue();
    }

    @Test
    public void testAddRequestFilters() throws Exception {
        AppPropertiesConfiguration config = setupConfig("some other value");
        Environment env = new Environment("Test Env", new ObjectMapper(), null, null, null);
        ApplicationContext context = service.loadAnnotationSpringContext(config, environment, DummyAppConfig.class);

        service.addRequestFilters(env, context);

        List resources = env.jersey().getResourceConfig().getContainerRequestFilters();
        assertThat(containsLoggingFilter(resources)).isTrue();
    }

    @Test
    public void testAddResponseFilters() throws Exception {
        AppPropertiesConfiguration config = setupConfig("some other value");
        Environment env = new Environment("Test Env", new ObjectMapper(), null, null, null);
        ApplicationContext context = service.loadAnnotationSpringContext(config, environment, DummyAppConfig.class);

        service.addResponseFilters(env, context);

        List resources = env.jersey().getResourceConfig().getContainerResponseFilters();
        assertThat(containsLoggingFilter(resources)).isTrue();
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

}
