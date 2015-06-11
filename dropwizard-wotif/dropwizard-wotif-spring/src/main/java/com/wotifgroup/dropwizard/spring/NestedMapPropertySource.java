/*
 * #%L
 * dropwizard-wotif-spring
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
package com.wotifgroup.dropwizard.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.PropertySource;

import java.util.Map;

public class NestedMapPropertySource extends PropertySource<Map> {
    private static final Logger LOGGER = LoggerFactory.getLogger(NestedMapPropertySource.class);

    private final Map propertyMap;

    public NestedMapPropertySource(String name, Map propertyMap) {
        super(name);
        this.propertyMap = propertyMap;
    }

    @Override
    public Object getProperty(String name) {
        if (name == null) {
            return null;
        }
        String[] propertyPath = name.split("\\.");
        Map map = propertyMap;
        for (int i = 0; i < propertyPath.length; i++) {
            Object value = map.get(propertyPath[i]);
            if (value instanceof Map && i < propertyPath.length) {
                map = (Map) value;
            } else if (!(value instanceof Map) && i == propertyPath.length - 1) {
                return value;
            } else {
                LOGGER.warn("No value exists for property \"{}\"", name);
                return null;
            }
        }
        return null;
    }
}
