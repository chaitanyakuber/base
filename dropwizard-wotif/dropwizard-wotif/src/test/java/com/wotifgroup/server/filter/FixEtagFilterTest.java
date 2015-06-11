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
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * FWIW, I hate verifying the internal state of an argument for a test, but ContainerRequest is a PITA to create a test
 * double for. If someone knows of a better way to use ContainerRequest and verify the output then please change below.
 */
public class FixEtagFilterTest {

    private FixEtagFilter filter;

    @Mock
    private ContainerRequest input;

    @Mock
    private MultivaluedMap<String, String> requestHeaders;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        filter = new FixEtagFilter();
        when(input.getRequestHeaders()).thenReturn(requestHeaders);
    }

    @Test
    public void testNoIfNoneMatchHeader() {
        when(input.getHeaderValue(HttpHeaders.IF_NONE_MATCH)).thenReturn(null);
        filter.filter(input);
        verify(requestHeaders, never()).putSingle(anyString(), anyString());
    }

    @Test
    public void testNoDashInIfNoneMatchHeader() {
        when(input.getHeaderValue(HttpHeaders.IF_NONE_MATCH)).thenReturn("");
        filter.filter(input);
        verify(requestHeaders, never()).putSingle(anyString(), anyString());
    }

    @Test
     public void testDashWithoutQuoteInIfNoneMatchHeader() {
        when(input.getHeaderValue(HttpHeaders.IF_NONE_MATCH)).thenReturn("something-gzip");
        filter.filter(input);
        verify(requestHeaders).putSingle(HttpHeaders.IF_NONE_MATCH, "something");
    }

    @Test
    public void testDashWithQuoteInIfNoneMatchHeader() {
        when(input.getHeaderValue(HttpHeaders.IF_NONE_MATCH)).thenReturn("something-gzip\"");
        filter.filter(input);
        verify(requestHeaders).putSingle(HttpHeaders.IF_NONE_MATCH, "something\"");
    }

    @Test
    public void testDeflateInIfNoneMatchHeader() {
        when(input.getHeaderValue(HttpHeaders.IF_NONE_MATCH)).thenReturn("something-deflate");
        filter.filter(input);
        verify(requestHeaders).putSingle(HttpHeaders.IF_NONE_MATCH, "something");
    }

    @Test
    public void testZipInIfNoneMatchHeader() {
        when(input.getHeaderValue(HttpHeaders.IF_NONE_MATCH)).thenReturn("something-zip");
        filter.filter(input);
        verify(requestHeaders, never()).putSingle(anyString(), anyString());
    }

}
