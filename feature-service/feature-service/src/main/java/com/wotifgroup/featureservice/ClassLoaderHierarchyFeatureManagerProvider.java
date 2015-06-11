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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.spi.FeatureManagerProvider;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This implementation of {@link FeatureManagerProvider} is based on
 * a version 1.0.1 class org.togglz.servlet.spi.WebAppFeatureManagerProvider (since removed)
 * instead of storing one {@link FeatureManager} for
 * each context classloader, it stores one per context classloader hierarchy.
 * <br/>
 * This is to simplify using Togglz inside applications packaged as EARs.
 * <br/>
 * It needs to be registered with the {@link org.togglz.core.context.FeatureContext} as described in
 * <a href="http://www.togglz.org/documentation/advanced-config.html">Togglz Advanced Config</a>.
 */
public class ClassLoaderHierarchyFeatureManagerProvider implements FeatureManagerProvider {
    private static final Logger LOG = LoggerFactory.getLogger(ClassLoaderHierarchyFeatureManagerProvider.class);

    private static final ConcurrentHashMap<ClassLoader, FeatureManager> managerMap = new ConcurrentHashMap<ClassLoader, FeatureManager>();


    protected static Logger getLogger(){
        return LOG;
    }

    @Override
    public int priority() {
        return 100;
    }

    @Override
    public FeatureManager getFeatureManager() {
        return managerMap.get(getContextRootClassLoader());
    }

    /**
     * Binds the {@link FeatureManager} to the current context root classloader.
     *
     * @param featureManager The manager to store
     */
    public static void bind(FeatureManager featureManager) {
        Object old = managerMap.putIfAbsent(getContextRootClassLoader(), featureManager);
        if (old != null) {
            LOG.warn("There is already a FeatureManager associated with the context root ClassLoader of the current thread");
        }
    }

    /**
     * Removes the {@link FeatureManager} associated with the current context root classloader from the
     * internal datastructure.
     */
    public static void release() {
        managerMap.remove(getContextRootClassLoader());
    }

    private static ClassLoader getContextRootClassLoader() {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader == null) {
                throw new IllegalStateException("No context ClassLoader associated with the current thread");
            }
            Set<ClassLoader> visited = new HashSet<ClassLoader>();
            for (; ; ) {
                ClassLoader parent = classLoader.getParent();
                if (parent == null || !visited.add(classLoader)) {
                    return classLoader;
                }
                classLoader = parent;
            }
        } catch (Exception e) {
            throw new IllegalStateException("Unable to get the context root ClassLoader for the current thread", e);
        }
    }

}
