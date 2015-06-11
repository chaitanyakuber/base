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
package com.wotifgroup.server.filter;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

import javax.ws.rs.core.HttpHeaders;

/**
 *
 * @author jjordan
 *
 * This filter works around a bug in jetty's GzipFilter.
 *
 * When sending a gzipped response, jetty automatically appends "-gzip" to the ETag header,
 * but it doesn't strip this on incoming requests, so later checks (e.g. by jersey's ContainerRequest#evaluatePreconditions)
 * will fail.  We strip this suffix, so that it works again.
 *
 * This filter may not behave correctly with respect to the Vary/Accept-Encoding headers - only use it if you're sure you don't
 * care (e.g. you're certain all clients use gzip, and/or there are no caching proxies between client and server).
 *
 * Also, jetty may not append the "-gzip" suffix to ETags generated for a 304 Not Modified response due to the empty response
 * body, which means the response ETag in a 304 response may not actually match the requested If-None-Match header...
 */
public class FixEtagFilter implements ContainerRequestFilter {

    @Override
    public ContainerRequest filter(ContainerRequest request) {
        final String ifNoneMatch = request.getHeaderValue(HttpHeaders.IF_NONE_MATCH);
        if (ifNoneMatch != null && (ifNoneMatch.contains("-deflate") || ifNoneMatch.contains("-gzip"))) {
            final String newIfNoneMatch = ifNoneMatch.replace("-deflate", "").replace("-gzip", "");
            request.getRequestHeaders().putSingle(HttpHeaders.IF_NONE_MATCH, newIfNoneMatch);
        }
        return request;
    }

}
