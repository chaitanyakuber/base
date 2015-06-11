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

import com.google.common.annotations.VisibleForTesting;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.ClientFilter;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Environment;

public class WotifJerseyClientBuilder extends JerseyClientBuilder {

    @VisibleForTesting
    static final ClientFilter TRACING_FILTER = new RequestTracingClientFilter();
    private final ClientFilter userAgentFilter;

    public WotifJerseyClientBuilder(final Environment environment, final String version) {
        super(environment);
        userAgentFilter = new UserAgentClientFilter(environment.getName() + "/" + version);
    }

    @Override
    public Client build(final String name) {
        final Client client = super.build(name);
        client.addFilter(TRACING_FILTER);
        client.addFilter(userAgentFilter);
        return client;
    }

}
