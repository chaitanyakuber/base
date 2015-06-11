/*
 * #%L
 * dropwizard-wotif-featureswitch
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
package com.wotifgroup.dropwizard.featureswitch;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMultimap;
import io.dropwizard.servlets.tasks.Task;
import org.togglz.core.Feature;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.manager.FeatureManager;

import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Dropwizard task to get status of feature switches.
 *
 * Usage:
 * <pre>
 * Get status of all feature switches:
 *   curl -XPOST 'http://host:port/tasks/featureswitch'
 * Get status of specific feature switches:
 *   curl -XPOST 'http://host:port/tasks/featureswitch?feature=FEATURE_1&feature=FEATURE_2'
 * </pre>
*/
public class FeatureSwitchTask extends Task {
    private static final String NAME = "featureswitch";

    public FeatureSwitchTask() {
        super(NAME);
    }

    @Override
    public void execute(ImmutableMultimap<String, String> parameters, PrintWriter writer) throws Exception {
        FeatureManager featureManager;

        try {
            featureManager = FeatureContext.getFeatureManager();
        } catch (RuntimeException e) {
            writer.append("Could not find FeatureManager.");
            return;
        }
        Map<String, Boolean> featureValues = new LinkedHashMap<>();

        ImmutableCollection<String> names = parameters.containsKey("feature") ? parameters.get("feature") : null;
        for (Feature feature : featureManager.getFeatures()) {
            if (names == null || names.contains(feature.name())) {
                featureValues.put(feature.name(), featureManager.isActive(feature));
            }
        }

        writer.write(Joiner.on("\n").withKeyValueSeparator("=").join(featureValues));
    }
}
