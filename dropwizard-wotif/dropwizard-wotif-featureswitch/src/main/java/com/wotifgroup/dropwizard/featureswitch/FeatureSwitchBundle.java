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

import com.google.common.base.Optional;
import com.wotifgroup.dropwizard.WotifConfiguration;
import com.wotifgroup.featureservice.ClassLoaderHierarchyFeatureManagerProvider;
import com.wotifgroup.featureservice.ZkfssStateRepository;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.togglz.core.Feature;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.FeatureManagerBuilder;
import org.togglz.core.user.NoOpUserProvider;

public class FeatureSwitchBundle<T extends WotifConfiguration> implements ConfiguredBundle<T> {
    private final Class<? extends Feature> featureEnum;

    public FeatureSwitchBundle(Class<? extends Feature> featureEnum) {
        this.featureEnum = featureEnum;
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
    }

    @Override
    public void run(T configuration, Environment environment) throws Exception {
        Optional<String> connectString = configuration.getZookeeperConnectString();
        if (!connectString.isPresent()) {
            throw new RuntimeException("Zookeeper connect string is not set," +
                    " please verify the property zookeeperConnectString in yaml config");
        }
        ZkfssStateRepository stateRepository = new ZkfssStateRepository(environment.getName(), connectString.get());
        // Feature service should be initialised as soon as possible
        stateRepository.init();
        environment.lifecycle().manage(new ZkfssStateRepositoryManaged(stateRepository));

        FeatureManager featureManager = new FeatureManagerBuilder()
                .stateRepository(stateRepository)
                .featureEnum(featureEnum)
                .userProvider(new NoOpUserProvider())
                .build();
        ClassLoaderHierarchyFeatureManagerProvider.bind(featureManager);

        FeatureSwitchTask task = new FeatureSwitchTask();
        environment.admin().addTask(task);

        FeatureSwitchHealthCheck healthCheck = new FeatureSwitchHealthCheck(stateRepository);
        environment.healthChecks().register("FeatureSwitch", healthCheck);
    }
}
