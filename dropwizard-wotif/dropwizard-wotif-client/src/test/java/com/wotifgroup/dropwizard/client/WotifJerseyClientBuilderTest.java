/*
 * #%L
 * dropwizard-wotif-client
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
package com.wotifgroup.dropwizard.client;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.setup.Environment;
import org.junit.Test;

import javax.validation.Validation;

import static org.fest.assertions.api.Assertions.assertThat;

public class WotifJerseyClientBuilderTest {

    @Test
    public void shouldAddTracingFilter() {
        final Environment env = new Environment("test", new ObjectMapper(), Validation.buildDefaultValidatorFactory().getValidator(), new MetricRegistry(), getClass().getClassLoader());
        assertThat(new WotifJerseyClientBuilder(env, "1.2.3").build("test").isFilterPresent(WotifJerseyClientBuilder.TRACING_FILTER)).isTrue();
    }

    @Test
    public void shouldAddUserAgentFilter() {
        final Environment env = new Environment("test", new ObjectMapper(), Validation.buildDefaultValidatorFactory().getValidator(), new MetricRegistry(), getClass().getClassLoader());
        assertThat(new WotifJerseyClientBuilder(env, "1.2.3").build("test").getHeadHandler() instanceof UserAgentClientFilter).isTrue();
    }

}
