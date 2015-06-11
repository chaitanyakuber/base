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
package com.wotifgroup.dropwizard.version;

import com.google.common.collect.ImmutableMultimap;
import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 */
public class WhoAmITaskTest {

    @Test
    public void returnsArtifactDetails() throws Exception {
        WhoAmITask task = new WhoAmITask(ProjectId.getForClass(getClass()));

        assertThat(task.getName())
                .isEqualTo("whoami");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        final ImmutableMultimap<String, String> parameters = ImmutableMultimap.of();

        task.execute(parameters, pw);

        assertThat(sw.toString()).
                isEqualTo("Group-ID: com.wotifgroup\nArtifact-ID: test\nVersion: 1.0\n");
    }

    @Test
    public void returnsVersionOnly() throws Exception {
        WhoAmITask task = new WhoAmITask(ProjectId.getForClass(getClass()));

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        final ImmutableMultimap<String, String> parameters = ImmutableMultimap.of("version", "");

        task.execute(parameters, pw);

        assertThat(sw.toString()).
                isEqualTo("1.0\n");
    }

}
