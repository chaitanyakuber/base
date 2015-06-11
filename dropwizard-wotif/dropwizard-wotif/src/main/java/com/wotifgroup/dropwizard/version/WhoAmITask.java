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
import io.dropwizard.servlets.tasks.Task;

import java.io.PrintWriter;

/**
 * Returns the application version string.
 */
public class WhoAmITask extends Task {

    private final ProjectId projectId;

    public WhoAmITask(ProjectId projectId) {
        super("whoami");
        this.projectId = projectId;
    }

    @Override
    public void execute(ImmutableMultimap<String, String> parameters, PrintWriter output) throws Exception {
        if (parameters.containsKey("version")) {
            output.println(projectId.getVersion());
        } else {
            output.printf("Group-ID: %s\nArtifact-ID: %s\nVersion: %s\n",
                    projectId.getGroupId(), projectId.getArtifactId(), projectId.getVersion());
        }
    }
}
