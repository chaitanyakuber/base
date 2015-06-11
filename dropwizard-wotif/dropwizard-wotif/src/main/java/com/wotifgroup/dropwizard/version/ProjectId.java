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
package com.wotifgroup.dropwizard.version;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class ProjectId {
    public static final ProjectId UNKNOWN = new ProjectId("?", "?", "?");

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectId.class);

    private final String groupId;
    private final String artifactId;
    private final String version;

    public ProjectId(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    /**
     * Gets the project Group/Artifact/Version from the manifest file for the given class. This attribute is automatically set for Maven
     * projects.
     *
     * @param clazz
     *            A class from the .jar file from which the version will be derived.
     * @return ProjectId
     */
    public static ProjectId getForClass(final Class<?> clazz) {
        if (clazz == null) {
            return UNKNOWN;
        }

        // The following code makes some assumptions about how the classpath information is stored and may or may not be specific
        // to the Oracle JVM.

        final URL appJar = clazz.getProtectionDomain().getCodeSource().getLocation();
        final Enumeration<URL> manifestUrls;
        try {
            manifestUrls = clazz.getClassLoader().getResources("META-INF/MANIFEST.MF");
        } catch (IOException e) {
            LOGGER.warn("Error reading project ID for " + clazz.getName(), e);
            return UNKNOWN;
        }

        while (manifestUrls.hasMoreElements()) {
            URL url = manifestUrls.nextElement();

            if (url.toString().contains(appJar.toString())) {
                return readProjectDetailsFromManifest(url);
            }
        }

        LOGGER.warn("Manifest for app jar " + appJar + " not found");
        return UNKNOWN;
    }

    private static ProjectId readProjectDetailsFromManifest(final URL url) {
        try (final InputStream input = url.openStream()) {
            final Manifest manifest = new Manifest(input);
            final Attributes attributes = manifest.getMainAttributes();

            return new ProjectId(
                attributes.getValue("Project-Group-Id"),
                attributes.getValue("Project-Artifact-Id"),
                attributes.getValue("Project-Version"));
        } catch (Exception e) {
            LOGGER.warn("Error reading project ID from " + url, e);
            return UNKNOWN;
        }
    }
}
