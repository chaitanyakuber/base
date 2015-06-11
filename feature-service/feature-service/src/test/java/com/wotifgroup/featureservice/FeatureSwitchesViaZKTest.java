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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.test.TestingServer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.togglz.core.context.FeatureContext;

import com.wotifgroup.zkfss.ZKFeatureSwitchService;

public class FeatureSwitchesViaZKTest {
    private static final String APP_NAME = "test";
    private static FeatureConfiguration fc;
    private static ZkfssStateRepository zkfssStateRepository;
    private static ZKFeatureSwitchService zkfss;
    private static CuratorFramework curatorFrameworkClient;
    private static TestingServer ts;

    @BeforeClass
    public static void doOnce() throws Exception {
        ClassLoaderHierarchyFeatureManagerProvider.release();
        FeatureContext.clearCache();
        ts = new TestingServer(2345);
        zkfssStateRepository = new ZkfssStateRepository(APP_NAME, "localhost:2345");
        zkfssStateRepository.init();
        fc = new FeatureConfiguration();
        fc.setAppName(APP_NAME);
        fc.setFeatureClass(ApplicationFeature.class);
        fc.setStateRepository(zkfssStateRepository);
        fc.init();
        zkfss = (ZKFeatureSwitchService) zkfssStateRepository.getFeatureSwitchService();
        curatorFrameworkClient = zkfss.getCuratorFrameworkClient();

    }

    @Before
    public void setup() throws Exception {
        curatorFrameworkClient.create().forPath("/zkfss");
    }

    @After
    public void teardown() throws Exception {
        deleteAllChildren("/zkfss");
    }

    private void deleteAllChildren(String node) throws Exception {
        List<String> children = curatorFrameworkClient.getChildren().forPath(node);
        for (String child : children) {
            deleteAllChildren(node + "/" + child);
        }
        curatorFrameworkClient.delete().forPath(node);
    }

    @AfterClass
    public static void allDone() throws IOException {
        ts.close();
    }

    @Test
    public void testFeatureIsNotActiveIfNotSet() {
        assertFalse(ApplicationFeature.FEATURE_SWITCH_NAME.isActive());
    }

    @Test
    public void testFeatureIsActive() throws Exception {
        ApplicationFeature.FEATURE_SWITCH_NAME.isActive();
        String nodePath = "/zkfss/" + ApplicationFeature.FEATURE_SWITCH_NAME.name();
        curatorFrameworkClient.create().forPath(nodePath, "true".getBytes());

        /*
         * TODO: Think of a better way than this hack. We need a little bit of time for zookeeper to notify zfss of changes before
         * continuing.
         */
        Thread.sleep(100);
        assertTrue(ApplicationFeature.FEATURE_SWITCH_NAME.isActive());
    }

    public enum ApplicationFeature implements org.togglz.core.Feature {

        FEATURE_SWITCH_NAME;

        public boolean isActive() {
            return FeatureContext.getFeatureManager().isActive(this);
        }

    }

}
