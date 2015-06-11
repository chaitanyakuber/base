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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

class AbstractPoolServlet extends HttpServlet {

    static final Logger LOGGER = LoggerFactory.getLogger(PoolStatus.class);

    private final PoolStatus pool;

    protected AbstractPoolServlet(final PoolStatus pool) {
        this.pool = pool;
    }

    protected void enter(final HttpServletResponse resp) throws ServletException, IOException {
        String message = String.format("Entering pool. Status was %s", pool.enter());
        LOGGER.info(message);
        respond(resp, message);
    }

    protected final void exit(final HttpServletResponse resp) throws ServletException, IOException {
        String message = String.format("Exiting pool. Status was %s", pool.exit());
        LOGGER.info(message);
        respond(resp, message);
    }

    protected final void state(final HttpServletResponse resp) throws ServletException, IOException {
        final PoolStatus.State state = pool.state();
        resp.setStatus(HttpServletResponse.SC_OK);
        respond(resp, state.toString());
    }

    private void respond(final HttpServletResponse response, final String entity) throws ServletException, IOException {
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        response.setContentLength(entity.length() + 1);

        final ServletOutputStream out = response.getOutputStream();
        out.print(entity);
        out.print('\n');
        out.close();
    }

}
