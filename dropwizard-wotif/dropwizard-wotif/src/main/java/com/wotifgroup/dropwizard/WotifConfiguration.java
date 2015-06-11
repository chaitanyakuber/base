/*
 * #%L
 * dropwizard-wotif
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
package com.wotifgroup.dropwizard;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.wotifgroup.dropwizard.jmx.JmxConnectorConfiguration;
import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Configuration extension for Wotif projects.
 */
public class WotifConfiguration extends Configuration {

    @Valid
    @NotNull
    @JsonProperty("jmx")
    private JmxConnectorConfiguration jmx = new JmxConnectorConfiguration();

    @JsonProperty("keyStorePassFile")
    private Optional<String> keyStorePassFile = Optional.absent();

    @JsonProperty("zookeeperConnectString")
    private Optional<String> zookeeperConnectString = Optional.absent();

    /**
     * Returns the JMX-specific section of the configuration file.
     * 
     * @return JMX-specific configuration parameters
     */
    public JmxConnectorConfiguration getJmxConnectorConfiguration() {
        return jmx;
    }

    /**
     * Returns the key store password file for the SSL cert. (optional)
     *
     * @return key store password file
     */
    public Optional<String> getKeyStorePassFile() {
        return keyStorePassFile;
    }

    /**
     * Connect string for zookeeper feature switch
     *
     * @return Zookeeper connect string
     */
    public Optional<String> getZookeeperConnectString() {
        return zookeeperConnectString;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("jmx", jmx)
                .add("keyStorePassFile", keyStorePassFile)
                .toString();
    }

}
