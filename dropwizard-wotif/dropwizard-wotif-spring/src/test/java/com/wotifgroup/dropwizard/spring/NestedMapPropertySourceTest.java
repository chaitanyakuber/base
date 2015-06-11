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

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class NestedMapPropertySourceTest {

    private NestedMapPropertySource propertySource;

    @Before
    @SuppressWarnings(value = "unchecked")
    public void setup() {
        // a:
        //    b: bValue
        //    c:
        //      d: dValue
        // e: eValue
        Map testMap = new HashMap();
        Map a = new HashMap();
        testMap.put("a", a);
        a.put("b", "bValue");
        Map c = new HashMap();
        a.put("c", c);
        c.put("d", "dValue");
        testMap.put("e", "eValue");

        propertySource = new NestedMapPropertySource("testMap", testMap);
    }

    @Test
    public void getNullPropertyReturnsNull() throws Exception {
        assertNull(propertySource.getProperty(null));
    }

    @Test
    public void getEmptyPropertyReturnsNull() throws Exception {
        assertNull(propertySource.getProperty(""));
    }

    @Test
    public void getExistingPropertyReturnsValue() throws Exception {
        assertEquals("bValue", propertySource.getProperty("a.b"));
        assertEquals("dValue", propertySource.getProperty("a.c.d"));
        assertEquals("eValue", propertySource.getProperty("e"));
    }

    @Test
    public void getNonExistentPropertyReturnsNull() throws Exception {
        assertNull(propertySource.getProperty("f"));
    }

    @Test
    public void getNonLeafReturnsNull() throws Exception {
        assertNull(propertySource.getProperty("a"));
        assertNull(propertySource.getProperty("c"));
    }

}
