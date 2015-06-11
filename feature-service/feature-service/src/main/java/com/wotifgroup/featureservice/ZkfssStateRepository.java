/*
 * #%L
 * feature-service
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
package com.wotifgroup.featureservice;

import com.wotifgroup.zkfss.FeatureSwitchService;
import com.wotifgroup.zkfss.ZKFeatureSwitchService;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;

import javax.annotation.PostConstruct;

public class ZkfssStateRepository implements StateRepository {

    private FeatureSwitchService featureSwitchService;
    private String appName;
    private String zookeeperConnectionString;
    private ZKFeatureSwitchService zkfss;

    public ZkfssStateRepository(String appName, String zookeeperConnectionString) {
        this.appName = appName;
        this.zookeeperConnectionString = zookeeperConnectionString;
    }

    @PostConstruct
    public void init() {
        if (this.featureSwitchService == null) {
            zkfss = new ZKFeatureSwitchService().setApplicationName(appName);
            if (zookeeperConnectionString != null) {
                zkfss = zkfss.setConnectString(zookeeperConnectionString);
            }
            zkfss.start();
            this.featureSwitchService = zkfss;
        }
    }

    @Override
    public FeatureState getFeatureState(Feature feature) {
        return new FeatureState(feature, featureSwitchService.isEnabled(feature.name()));
    }

    @Override
    public void setFeatureState(FeatureState featureState) {
        throw new UnsupportedOperationException("zkfss does not support setting feature state.");
    }

    public FeatureSwitchService getFeatureSwitchService() {
        return featureSwitchService;
    }

    public void setFeatureSwitchService(FeatureSwitchService featureSwitchService) {
        this.featureSwitchService = featureSwitchService;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getZookeeperConnectionString() {
        return zookeeperConnectionString;
    }

    public void setZookeeperConnectionString(String zookeeperConnectionString) {
        this.zookeeperConnectionString = zookeeperConnectionString;
    }

    public ZKFeatureSwitchService getZKFeatureSwitchService() {
        return zkfss;
    }

}
