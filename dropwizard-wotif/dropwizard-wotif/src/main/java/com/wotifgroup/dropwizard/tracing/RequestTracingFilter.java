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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterators;
import com.google.common.net.InetAddresses;
import org.slf4j.MDC;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.regex.Pattern;

/**
 * Filter that looks for an incoming request ID and if none is found, adds its own.
 */
public class RequestTracingFilter implements Filter {

    @VisibleForTesting
    static final String X_OPAQUE_ID = "X-Opaque-ID";
    @VisibleForTesting
    static final String REQUEST_ID = "requestId";

    private static final Pattern NEW_LINES = Pattern.compile("[\r\n]");

    private final IdGenerator idGenerator;

    public RequestTracingFilter() {
        this.idGenerator = new IdGenerator();
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        // Nothing to do
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            final boolean siteLocal = isSiteLocalRequest(request);

            final String requestId = buildRequestId(siteLocal, (HttpServletRequest) request);

            MDC.put(REQUEST_ID, requestId);
            request.setAttribute(REQUEST_ID, requestId);

            // Response headers must be set prior to handling request as Transfer-Encoding is "chunked"
            setResponseHeaders(siteLocal, (HttpServletResponse) response, requestId);
        }
        chain.doFilter(request, response);

    }

    private boolean isSiteLocalRequest(final ServletRequest request) {
        // If the address is invalid it's probably from the Internet
        try {
            InetAddress inetAddress = InetAddresses.forString(request.getRemoteAddr());
            return inetAddress.isLoopbackAddress() || inetAddress.isSiteLocalAddress();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    // TODO: Move this to common request handler library
    private String buildRequestId(final boolean siteLocal, final HttpServletRequest httpRequest) {
        final Iterator<String> headers = Iterators.forEnumeration(httpRequest.getHeaderNames());
        if (siteLocal && Iterators.contains(headers, X_OPAQUE_ID)) {
            final String requestId = httpRequest.getHeader(X_OPAQUE_ID);
            return NEW_LINES.matcher(requestId).replaceAll("");
        }
        return idGenerator.generate();
    }

    // TODO: Move this to common request handler library
    private void setResponseHeaders(final boolean siteLocal, final HttpServletResponse httpResponse, final String requestId) {
        if (siteLocal) {
            httpResponse.setHeader(X_OPAQUE_ID, requestId);
        }
    }

    @Override
    public void destroy() {
        // Nothing to do
    }

}
