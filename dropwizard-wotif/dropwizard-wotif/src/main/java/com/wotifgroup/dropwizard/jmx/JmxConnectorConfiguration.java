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
package com.wotifgroup.dropwizard.jmx;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import io.dropwizard.validation.ValidationMethod;

/**
 * Configuration for JMX connector.
 */
public class JmxConnectorConfiguration {

    @Min(1025)
    @Max(65535)
    @JsonProperty
    protected int registryPort = 8086;

    @Min(1025)
    @Max(65535)
    @JsonProperty
    protected int serverPort = 8087;

    @JsonProperty
    protected String readOnlyUsername = null;

    @JsonProperty
    protected String readOnlyPassword = null;

    @JsonProperty
    protected String readWriteUsername = null;

    @JsonProperty
    protected String readWritePassword = null;

    @ValidationMethod(message = "must have readOnlyUsername if readOnlyPassword is defined")
    public boolean isReadOnlyUsernameDefined() {
        return (readOnlyPassword == null) || (readOnlyUsername != null);
    }

    @ValidationMethod(message = "must have readWriteUsername if readWritePassword is defined")
    public boolean isReadWriteUsernameDefined() {
        return (readWritePassword == null) || (readWriteUsername != null);
    }

    @ValidationMethod(message = "registryPort must not equal serverPort")
    public boolean isRegistryPortDifferentFromServerPort() {
        return registryPort != serverPort;
    }

    public int getRegistryPort() {
        return registryPort;
    }

    public int getServerPort() {
        return serverPort;
    }

    public Optional<String> getReadOnlyUsername() {
        return Optional.fromNullable(readOnlyUsername);
    }

    public Optional<String> getReadOnlyPassword() {
        return Optional.fromNullable(readOnlyPassword);
    }

    public Optional<String> getReadWriteUsername() {
        return Optional.fromNullable(readWriteUsername);
    }

    public Optional<String> getReadWritePassword() {
        return Optional.fromNullable(readWritePassword);
    }

}
