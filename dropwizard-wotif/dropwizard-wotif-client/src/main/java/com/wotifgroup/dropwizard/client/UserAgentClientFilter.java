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

import com.google.common.base.Strings;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

class UserAgentClientFilter extends ClientFilter {

    private final String userAgent;

    public UserAgentClientFilter(final String userAgent) {
        this.userAgent = userAgent;
    }

    @Override
    public ClientResponse handle(final ClientRequest cr) {
        if (!Strings.isNullOrEmpty(userAgent)) {
            cr.getHeaders().add("User-Agent", userAgent);
        }
        return getNext().handle(cr);
    }

}
