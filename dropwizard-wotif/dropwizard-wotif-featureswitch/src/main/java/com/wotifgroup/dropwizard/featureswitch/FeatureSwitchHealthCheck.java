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

import com.codahale.metrics.health.HealthCheck;
import com.google.common.base.Joiner;
import com.wotifgroup.featureservice.ZkfssStateRepository;
import org.apache.zookeeper.ZooKeeper;
import org.togglz.core.Feature;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.manager.FeatureManager;

import java.util.LinkedHashMap;
import java.util.Map;

public class FeatureSwitchHealthCheck extends HealthCheck {

    private ZkfssStateRepository stateRepository;

    protected FeatureSwitchHealthCheck(ZkfssStateRepository stateRepository) {
        this.stateRepository = stateRepository;
    }

    @Override
    protected Result check() throws Exception {
        final ZooKeeper.States state = stateRepository.getZKFeatureSwitchService()
                .getCuratorFrameworkClient()
                .getZookeeperClient()
                .getZooKeeper()
                .getState();

        if (!state.isAlive()) {
            return Result.unhealthy("Zookeeper client is dead, in state: %s", state);
        }
        if (!state.isConnected()) {
            return Result.unhealthy("Zookeeper client not connected, in state: %s", state);
        }

        FeatureManager featureManager = FeatureContext.getFeatureManager();
        Map<String, Boolean> featureValues = new LinkedHashMap<>();
        for (Feature feature : featureManager.getFeatures()) {
            featureValues.put(feature.name(), featureManager.isActive(feature));
        }

        return Result.healthy(Joiner.on(", ").withKeyValueSeparator("=").join(featureValues));
    }

}
