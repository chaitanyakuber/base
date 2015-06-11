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

import com.wotifgroup.featureservice.ZkfssStateRepository;
import io.dropwizard.lifecycle.Managed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZkfssStateRepositoryManaged implements Managed {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZkfssStateRepositoryManaged.class);

    private final ZkfssStateRepository stateRepository;

    public ZkfssStateRepositoryManaged(ZkfssStateRepository stateRepository) {
        this.stateRepository = stateRepository;
    }

    @Override
    public void start() throws Exception {
        // The zookeeper service has been already started
        LOGGER.info("Started Zookeeper feature switch service");
    }

    @Override
    public void stop() throws Exception {
        stateRepository.getZKFeatureSwitchService().stop();
        LOGGER.info("Stopped Zookeeper feature switch service");
    }
}
