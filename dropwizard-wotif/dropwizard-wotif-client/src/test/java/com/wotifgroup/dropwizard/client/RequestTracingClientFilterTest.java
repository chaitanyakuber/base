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

import com.sun.jersey.api.client.ClientHandler;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.junit.Test;
import org.slf4j.MDC;

import javax.ws.rs.core.MultivaluedMap;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.reflect.core.Reflection.method;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RequestTracingClientFilterTest {

    @Test
    public void shouldNotAddRequestIdIfNotSet() throws Throwable {
        final MultivaluedMap headers = new MultivaluedMapImpl();
        final RequestTracingClientFilter filter = new RequestTracingClientFilter();
        final ClientFilter testFilter = new ClientFilter() {
            @Override
            public ClientResponse handle(final ClientRequest cr) throws ClientHandlerException {
                assertThat(MDC.get("requestId")).isNull();
                assertThat(cr.getHeaders().getFirst("X-Opaque-ID")).isNull();
                return null;
            }
        };

        method("setNext").withParameterTypes(ClientHandler.class)
                .in(filter)
                .invoke(testFilter);

        final ClientRequest request = mock(ClientRequest.class);

        when(request.getHeaders()).thenReturn(headers);

        filter.handle(request);

        assertThat(headers).isEmpty();
    }


    @Test
    public void shouldAddValueIfset() throws Throwable {
        final MultivaluedMap headers = new MultivaluedMapImpl();
        final String expected = "trace";

        final RequestTracingClientFilter filter = new RequestTracingClientFilter();
        final ClientFilter testFilter = new ClientFilter() {
            @Override
            public ClientResponse handle(final ClientRequest cr) throws ClientHandlerException {
                assertThat(MDC.get("requestId")).isEqualTo(expected);
                assertThat(cr.getHeaders().getFirst("X-Opaque-ID")).isEqualTo(expected);
                return null;
            }
        };

        method("setNext").withParameterTypes(ClientHandler.class)
                .in(filter)
                .invoke(testFilter);

        final ClientRequest request = mock(ClientRequest.class);

        when(request.getHeaders()).thenReturn(headers);

        MDC.put("requestId", expected);

        try {
            filter.handle(request);
        } finally {
            MDC.remove("requestId");
        }

        assertThat(headers).hasSize(1);
        assertThat(headers.getFirst("X-Opaque-ID")).isEqualTo(expected);
    }

}
