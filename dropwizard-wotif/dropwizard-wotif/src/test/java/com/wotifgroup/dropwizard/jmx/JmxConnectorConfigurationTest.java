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

import static org.fest.assertions.api.Assertions.*;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.*;

import java.io.File;
import java.util.Collections;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.ConfigurationFactory;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.io.Resources;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

public class JmxConnectorConfigurationTest {

    private JmxConnectorConfiguration jmx;

    @Before
    public void setUp() throws Exception {
        Validator validator = mock(Validator.class);
        when(validator.validate(anyObject())).thenReturn(Collections.<ConstraintViolation<Object>>emptySet());
        this.jmx = new ConfigurationFactory<JmxConnectorConfiguration>(JmxConnectorConfiguration.class, validator, new ObjectMapper(), "")
                .build(new File(Resources.getResource("yaml/jmx.yml").toURI()));
    }

    @Test
    public void hasARegistryPort() throws Exception {
        assertThat(jmx.getRegistryPort())
                .isEqualTo(9086);
    }

    @Test
    public void hasAServerPort() throws Exception {
        assertThat(jmx.getServerPort())
                .isEqualTo(9087);
    }

    @Test
    public void hasAReadOnlyUsername() throws Exception {
        assertThat(jmx.getReadOnlyUsername())
                .isEqualTo(Optional.of("readonly"));
    }

    @Test
    public void hasAReadOnlyPassword() throws Exception {
        assertThat(jmx.getReadOnlyPassword())
                .isEqualTo(Optional.of("readonlypassword"));
    }

    @Test
    public void hasAReadWriteUsername() throws Exception {
        assertThat(jmx.getReadWriteUsername())
                .isEqualTo(Optional.of("readwrite"));
    }

    @Test
    public void hasAReadWritePassword() throws Exception {
        assertThat(jmx.getReadWritePassword())
                .isEqualTo(Optional.of("readwritepassword"));
    }

    @Test
    public void defaultConfigurationIsValid() {
        JmxConnectorConfiguration config = new JmxConnectorConfiguration();

        assertThat(config.isReadOnlyUsernameDefined()).isTrue();
        assertThat(config.isReadWriteUsernameDefined()).isTrue();
    }

}
