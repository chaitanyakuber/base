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

import org.junit.Before;
import org.junit.Test;
import org.togglz.core.annotation.Label;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.FeatureManagerBuilder;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.file.FileBasedStateRepository;
import org.togglz.core.user.NoOpUserProvider;
import org.togglz.core.user.UserProvider;

import java.io.File;

import static org.junit.Assert.assertNotNull;


public class ClassLoaderHierarchyFeatureManagerProviderTest {


    FeatureManager featureManager;

    @Before
    public void init() {
        String appName = "dummyUnitTestFeatureManager";
        StateRepository stateRepository = new FileBasedStateRepository(new File("/tmp/feature.dummy.test.properties"));
        UserProvider userProvider = new NoOpUserProvider();
        FeatureConfiguration config = new FeatureConfiguration();
        config.setAppName(appName);
        config.setStateRepository(stateRepository);
        config.setFeatureClass(TestFeature.class);
        config.setUserProvider(userProvider);
        featureManager = new FeatureManagerBuilder().togglzConfig(config).build();
    }

    @Test
    public void testGetFeatureManager() throws Exception {
        ClassLoaderHierarchyFeatureManagerProvider featureProvider = new ClassLoaderHierarchyFeatureManagerProvider();
        FeatureManager featureManager = featureProvider.getFeatureManager();
        assertNotNull(featureManager);
    }

    @Test
    public void testBindCalledTwiceIsNotFatal() throws Exception {

        ClassLoaderHierarchyFeatureManagerProvider.bind(featureManager);
        ClassLoaderHierarchyFeatureManagerProvider.bind(featureManager);

    }


    private enum TestFeature implements org.togglz.core.Feature {

        @Label("some feature")
        feature_one,
        @Label("another feature")
        feature_two;

        public boolean isActive() {
            return FeatureContext.getFeatureManager().isActive(this);
        }


    }
}
