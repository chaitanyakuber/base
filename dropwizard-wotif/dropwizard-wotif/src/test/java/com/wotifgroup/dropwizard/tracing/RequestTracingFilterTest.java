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
package com.wotifgroup.dropwizard.tracing;

import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.MDC;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;

import static com.wotifgroup.dropwizard.tracing.RequestTracingFilter.REQUEST_ID;
import static com.wotifgroup.dropwizard.tracing.RequestTracingFilter.X_OPAQUE_ID;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RequestTracingFilterTest {

    private static final String INTERNAL_IP = "10.1.1.1";
    private static final String LOOPBACK_IP = "127.0.0.1";
    private static final String EXTERNAL_IP = "203.8.182.99";
    private HttpServletRequest request = mock(HttpServletRequest.class);
    private HttpServletResponse response = mock(HttpServletResponse.class);

    @Test
    public void echoesRequestId() throws Exception {
        checkEchoesRequestId(INTERNAL_IP);
        checkEchoesRequestId(LOOPBACK_IP);
    }

    @Test
    public void generatesRequestIdIfNoneGiven() throws Exception {
        final StringBuilder got = new StringBuilder();
        final Filter f = new RequestTracingFilter();
        final FilterChain c = new FilterChain() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response)
                    throws IOException, ServletException {
                final String requestId = MDC.get(REQUEST_ID);
                got.append(requestId);
                assertThat(requestId).isNotEmpty();
            }
        };
        when(request.getHeaderNames()).thenReturn(Collections.<String>emptyEnumeration());
        when(request.getRemoteAddr()).thenReturn(INTERNAL_IP);

        f.doFilter(request, response, c);

        verify(response, times(1)).setHeader(X_OPAQUE_ID, got.toString());
        assertThat(MDC.get(REQUEST_ID)).isNotNull();
    }

    @Test
    public void dropsAndDoesntReturnRequestIdIfExternalAddress() throws Exception {
        final String dummyRequestId = "foo";
        final Filter f = new RequestTracingFilter();
        final FilterChain c = new FilterChain() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response)
                    throws IOException, ServletException {
                assertThat(MDC.get(REQUEST_ID)).isNotEqualTo(dummyRequestId);
            }
        };

        when(request.getHeaderNames()).thenReturn(enumeration(X_OPAQUE_ID));
        when(request.getHeader(X_OPAQUE_ID)).thenReturn(dummyRequestId);
        when(request.getRemoteAddr()).thenReturn(EXTERNAL_IP);

        f.doFilter(request, response, c);

        assertThat(MDC.get(REQUEST_ID)).isNotEqualTo(dummyRequestId);
    }

    @Test
    public void dropsAndDoesntReturnRequestIdIfCrapAddress() throws Exception {
        final String dummyRequestId = "foo";
        final Filter f = new RequestTracingFilter();
        final FilterChain c = new FilterChain() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response)
                    throws IOException, ServletException {
                assertThat(MDC.get(REQUEST_ID)).isNotEqualTo(dummyRequestId);
            }
        };

        when(request.getHeaderNames()).thenReturn(enumeration(X_OPAQUE_ID));
        when(request.getHeader(X_OPAQUE_ID)).thenReturn(dummyRequestId);
        when(request.getRemoteAddr()).thenReturn("unknown");

        f.doFilter(request, response, c);

        assertThat(MDC.get(REQUEST_ID)).isNotEqualTo(dummyRequestId);
    }

    private void checkEchoesRequestId(String ipAddress) throws IOException, ServletException {
        final String expected = "foo";

        final Filter f = new RequestTracingFilter();
        final FilterChain c = new FilterChain() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response)
                    throws IOException, ServletException {
                assertThat(MDC.get(REQUEST_ID)).isEqualTo(expected);
            }
        };

        when(request.getHeaderNames()).thenReturn(enumeration(X_OPAQUE_ID));
        when(request.getHeader(X_OPAQUE_ID)).thenReturn(expected);
        when(request.getRemoteAddr()).thenReturn(ipAddress);

        f.doFilter(request, response, c);

        verify(response, times(1)).setHeader(X_OPAQUE_ID, expected);
        assertThat(MDC.get(REQUEST_ID)).isEqualTo(expected);
        Mockito.reset(request, response);
    }

    private Enumeration<String> enumeration(String... values) {
        return Collections.enumeration(Arrays.asList(values));
    }

}
