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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wotifgroup.dropwizard.WotifConfiguration;

import java.util.Map;

public class AppPropertiesConfiguration extends WotifConfiguration {
    @JsonProperty
    private Map appProperties;

    public Map getAppProperties() {
        return appProperties;
    }

    public void setAppProperties(Map appProperties) {
        this.appProperties = appProperties;
    }
}
