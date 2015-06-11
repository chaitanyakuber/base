/*
 * #%L
 * pool-status-servlet
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
package com.wotifgroup.poolstatusservlet;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;

import static org.fest.assertions.api.Assertions.assertThat;

public class IntegrationTest {

    private static final PoolStatus POOL_STATUS = new PoolStatus();
    private static RunWithWebServer server;
    private static HttpClient client;

    @ClassRule
    public static RunWithWebServer buildServer() {
        server = new RunWithWebServer.Builder()
                .with(new EnterPoolServlet(POOL_STATUS), "/enter")
                .with(new ExitPoolServlet(POOL_STATUS), "/exit")
                .with(new PoolStatusServlet(POOL_STATUS), "/pool_status.txt")
                .build();
        return server;
    }

    @BeforeClass
    public static void startClient() throws Exception {
        client = new HttpClient();
        client.start();
    }

    @AfterClass
    public static void stopClient() throws Exception {
        client.stop();
    }


    @Test
    public void shouldEnterPoolWhenDead() throws Exception {
        POOL_STATUS.exit();

        final ContentResponse response = newRequest("/enter", HttpMethod.POST);

        assertThat(response.getStatus())
                .isEqualTo(HttpServletResponse.SC_OK);
        assertThat(response.getContentAsString())
                .isEqualTo("Entering pool. Status was dead\n");
    }

    @Test
    public void shouldEnterPoolWhenAlive() throws Exception {
        POOL_STATUS.enter();

        final ContentResponse response = newRequest("/enter", HttpMethod.POST);

        assertThat(response.getStatus())
                .isEqualTo(HttpServletResponse.SC_OK);
        assertThat(response.getContentAsString())
                .isEqualTo("Entering pool. Status was alive\n");
    }

    @Test
    public void shouldExitPoolWhenDead() throws Exception {
        POOL_STATUS.exit();

        final ContentResponse response = newRequest("/exit", HttpMethod.POST);

        assertThat(response.getStatus())
                .isEqualTo(HttpServletResponse.SC_OK);
        assertThat(response.getContentAsString())
                .isEqualTo("Exiting pool. Status was dead\n");
    }

    @Test
    public void shouldExitPoolWhenAlive() throws Exception {
        POOL_STATUS.enter();

        final ContentResponse response = newRequest("/exit", HttpMethod.POST);

        assertThat(response.getStatus())
                .isEqualTo(HttpServletResponse.SC_OK);
        assertThat(response.getContentAsString())
                .isEqualTo("Exiting pool. Status was alive\n");
    }

    @Test
    public void shouldReturnAliveWhenAlive() throws Exception {
        POOL_STATUS.enter();

        final ContentResponse response = newRequest("/pool_status.txt", HttpMethod.GET);

        assertThat(response.getStatus())
                .isEqualTo(HttpServletResponse.SC_OK);
        assertThat(response.getContentAsString())
                .isEqualTo("alive\n");
    }

    @Test
    public void shouldReturnDeadWhenDead() throws Exception {
        POOL_STATUS.exit();

        final ContentResponse response = newRequest("/pool_status.txt", HttpMethod.GET);

        assertThat(response.getStatus())
                .isEqualTo(HttpServletResponse.SC_OK);
        assertThat(response.getContentAsString())
                .isEqualTo("dead\n");
    }

    private ContentResponse newRequest(final String path, final HttpMethod method) throws Exception {
        return client.newRequest("localhost", server.port())
                .path(path)
                .method(method)
                .send();
    }

}
