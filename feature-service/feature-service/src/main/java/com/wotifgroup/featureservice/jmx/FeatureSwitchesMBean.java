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
package com.wotifgroup.featureservice.jmx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.togglz.core.Feature;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.repository.FeatureState;

import javax.management.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static com.google.common.collect.Iterables.toArray;

/**
 * Add this bean to your Spring config to expose your features over JMX.
 * Usually it is not necessary to explicitly register the bean with JMX.
 * <p/>
 * Write operations write through to the underlying store (e.g. the features.properties file).
 * <p/>
 * <strong>NB:</strong> must be initialised after the FeatureConfiguration.
 * In a Spring app, this can be achieved with the depends-on attribute on the bean declaration.
 * <p/>
 * Features can implement {@link SelfDescribing} to provide a description of themselves.
 */
public class FeatureSwitchesMBean implements DynamicMBean {

    private static final Logger LOG = LoggerFactory.getLogger(FeatureSwitchesMBean.class);

    private FeatureManager featureManager = null;

    @Override
    public Boolean getAttribute(String attributeName)
            throws AttributeNotFoundException, MBeanException, ReflectionException {
        boolean active = false;
        try {
            Feature feature = getFeature(attributeName);
            active = getFeatureManger().isActive(feature);
        } catch (Exception e) {
            throw new MBeanException(e);
        }
        return active;
    }

    @Override
    public void setAttribute(Attribute attribute)
            throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        try {
            FeatureContext.getFeatureManager().setFeatureState(
                    new FeatureState(getFeature(attribute.getName()), (Boolean) attribute.getValue()));
        } catch (Exception e) {
            throw new MBeanException(e);
        }
    }

    @Override
    public AttributeList getAttributes(String[] attributeNames) {
        List<Attribute> attributes = new ArrayList<Attribute>();
        for (String attributeName : attributeNames) {
            try {
                attributes.add(new Attribute(attributeName, getAttribute(attributeName)));
            } catch (Exception e) {
                LOG.debug("Error with " + attributeName + " in getAttributes", e);
            }
        }
        return new AttributeList(attributes);
    }

    @Override
    public AttributeList setAttributes(AttributeList attributes) {
        List<Attribute> successful = new ArrayList<Attribute>();
        for (Attribute attribute : attributes.asList()) {
            try {
                setAttribute(attribute);
                successful.add(attribute);
            } catch (Exception e) {
                LOG.debug("Error with " + attribute.getName() + " in setAttributes", e);
            }
        }
        return new AttributeList(successful);
    }

    @Override
    public Object invoke(String actionName, Object[] params, String[] signature)
            throws MBeanException, ReflectionException {
        throw new MBeanException(new UnsupportedOperationException("no operations"));
    }

    @Override
    public MBeanInfo getMBeanInfo() {
        return new MBeanInfo(
                getClass().getName(),
                "Togglz Feature Switches",
                toArray(buildAttributeInfos(), MBeanAttributeInfo.class),
                new MBeanConstructorInfo[]{},
                new MBeanOperationInfo[]{},
                new MBeanNotificationInfo[]{});
    }

    private List<MBeanAttributeInfo> buildAttributeInfos() {
        FeatureManager localFeatureManager = getFeatureManger();
        if(localFeatureManager==null){
            return Collections.emptyList();
        }

        List<MBeanAttributeInfo> attributeInfos = new ArrayList<MBeanAttributeInfo>();
        for (Feature feature : localFeatureManager.getFeatures()) {
            attributeInfos.add(buildAttributeInfo(feature));
        }
        return attributeInfos;
    }

    private MBeanAttributeInfo buildAttributeInfo(Feature feature) {
        String description = feature instanceof SelfDescribing ?
                ((SelfDescribing) feature).description() : feature.name();
        return new MBeanAttributeInfo(
                feature.name(), Boolean.TYPE.getName(), description, true, true, false);
    }

    private Feature getFeature(String attributeName) throws AttributeNotFoundException {

        FeatureManager featureManager = FeatureContext.getFeatureManager();
        Feature feature = null;

        Iterator<Feature> featureIterator = featureManager.getFeatures ().iterator();
        Feature tmp;
        while (featureIterator.hasNext()){
            tmp = featureIterator.next();
            if (tmp.name().equals(attributeName)){
                feature = tmp;
                break;
            }
        }
        if (feature==null){
            throw new AttributeNotFoundException(attributeName);
        }
        return feature;
    }


    private FeatureManager getFeatureManger(){
        if (featureManager==null){
            try{
                featureManager = FeatureContext.getFeatureManager();
            } catch (IllegalStateException e) {
                LOG.error("Error getting FeatureManager - is FeatureConfiguration initialised yet?", e);
            }
        }

        return featureManager;
    }
}
