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

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;

import com.wotifgroup.zkfss.FeatureSwitchService;

public class ZkfssStateRepositoryTest {
    
    private static final String APP_NAME = "test";
    private static final String ACTIVE_FEATURE = "active_feature";
    private ZkfssStateRepository repository;
    private Feature activeFeature = new Feature() {
        public boolean isActive() { return true; };
        public String name() { return ACTIVE_FEATURE; };
    };
    private FeatureState enabledFeatureState = new FeatureState(activeFeature, true); 
    private FeatureState disabledFeatureState = new FeatureState(activeFeature, false); 
    private FeatureSwitchService fssWithEnabledFeature = new FeatureSwitchService() {

        @Override
        public boolean isEnabled(String key) {
            return enabledFeatureState.isEnabled();
        }
        
    };
    private FeatureSwitchService fssWithDisabledFeature = new FeatureSwitchService() {

        @Override
        public boolean isEnabled(String key) {
            return disabledFeatureState.isEnabled();
        }
        
    };
    
    @Before
    public void setUp() {
        repository = new ZkfssStateRepository(APP_NAME, null);
    }
    
    @After
    public void tearDown() {
        repository = null;
    }

    @Test
    public void testGetFeatureState() {
        repository.setFeatureSwitchService(fssWithEnabledFeature);
        assertTrue(repository.getFeatureState(activeFeature).isEnabled());
        repository.setFeatureSwitchService(fssWithDisabledFeature);
        assertFalse(repository.getFeatureState(activeFeature).isEnabled());
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testSetFeatureState() {
        repository.setFeatureState(enabledFeatureState);
        fail("This is unsupported and should fail");
    }
}
