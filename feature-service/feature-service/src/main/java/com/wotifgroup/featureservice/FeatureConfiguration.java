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

import org.togglz.core.Feature;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.FeatureManagerBuilder;
import org.togglz.core.manager.TogglzConfig;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.user.NoOpUserProvider;
import org.togglz.core.user.UserProvider;

import javax.annotation.PostConstruct;

/**
 * This class is intended to simplify using Togglz inside a Spring app, and also provide sensible wotif defaults.
 * 
 * It is intended to be spring-friendly. It relies on its init() and destroy() methods being called, which are spring-annotated
 * (but can be called explicitly instead if used outside spring).
 */
public class FeatureConfiguration implements TogglzConfig {

    private String appName;
    private Class<? extends Feature> featureClass;
    private StateRepository stateRepository;
    private String zookeeperConnectionString;
    private UserProvider userProvider;

    @PostConstruct
    public void init() {
        // default values if they haven't been specified
        if (this.stateRepository == null) {
            this.stateRepository = new ZkfssStateRepository(appName, zookeeperConnectionString);
        }
        if (this.userProvider == null) {
            // If app wants to expose admin console, can integrate its own UserProvider.
            this.userProvider = new NoOpUserProvider();
        }

        FeatureManager featureManager = new FeatureManagerBuilder().togglzConfig(this).build();
        ClassLoaderHierarchyFeatureManagerProvider.bind(featureManager);
    }


    @Override
    public Class<? extends Feature> getFeatureClass() {
        return this.featureClass;
    }

    @Override
    public StateRepository getStateRepository() {
        return this.stateRepository;
    }

    @Override
    public UserProvider getUserProvider() {
        return this.userProvider;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setFeatureClass(Class<? extends Feature> featureClass) {
        this.featureClass = featureClass;
    }

    public void setStateRepository(StateRepository stateRepository) {
        this.stateRepository = stateRepository;
    }

    public void setUserProvider(UserProvider userProvider) {
        this.userProvider = userProvider;
    }

    public void setZookeeperConnectionString(String zookeeperConnectionString) {
        this.zookeeperConnectionString = zookeeperConnectionString;
    }

}
