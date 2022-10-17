/*
 * Copyright 2022 the original author or authors.
 *
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
 */

package org.gradle.api.internal.tasks.testing.testng;

import com.google.inject.Injector;
import com.google.inject.Module;
import org.testng.IAttributes;
import org.testng.IResultMap;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.collections.ListMultiMap;
import org.testng.collections.Maps;
import org.testng.internal.Attributes;
import org.testng.internal.ResultMap;
import org.testng.xml.XmlTest;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TestDryRunner implements ITestContext {

    private final ISuite suite;

    private final IAttributes attributes = new Attributes();

    private final ListMultiMap<Class<? extends Module>, Module> guiceModules = Maps.newListMultiMap();

    private final XmlTest xmlTest;

    public TestDryRunner(ISuite suite, XmlTest xmlTest) {
        this.suite = suite;
        this.xmlTest = xmlTest;
    }

    @Override
    public String getName() {
        return suite.getName();
    }

    @Override
    public Date getStartDate() {
        return new Date();
    }

    @Override
    public Date getEndDate() {
        return new Date();
    }

    @Override
    public IResultMap getPassedTests() {
        return new ResultMap();
    }

    @Override
    public IResultMap getSkippedTests() {
        return new ResultMap();
    }

    @Override
    public IResultMap getFailedButWithinSuccessPercentageTests() {
        return new ResultMap();
    }

    @Override
    public IResultMap getFailedTests() {
        return null;
    }

    @Override
    public String[] getIncludedGroups() {
        return new String[0];
    }

    @Override
    public String[] getExcludedGroups() {
        return new String[0];
    }

    @Override
    public String getOutputDirectory() {
        return suite.getOutputDirectory();
    }

    @Override
    public ISuite getSuite() {
        return suite;
    }

    @Override
    public ITestNGMethod[] getAllTestMethods() {
        return suite.getAllMethods().toArray(new ITestNGMethod[0]);
    }

    @Override
    public String getHost() {
        return suite.getHost();
    }

    @Override
    public Collection<ITestNGMethod> getExcludedMethods() {
        return null;
    }

    @Override
    public IResultMap getPassedConfigurations() {
        return new ResultMap();
    }

    @Override
    public IResultMap getSkippedConfigurations() {
        return new ResultMap();
    }

    @Override
    public IResultMap getFailedConfigurations() {
        return new ResultMap();
    }

    @Override
    public XmlTest getCurrentXmlTest() {
        return xmlTest;
    }

    private final Map<List<Module>, Injector> injectors = Maps.newHashMap();

    @Override
    public void addInjector(List<com.google.inject.Module> moduleInstances, com.google.inject.Injector injector) {
        injectors.put(moduleInstances, injector);
    }

    @Override
    public com.google.inject.Injector getInjector(List<com.google.inject.Module> moduleInstances) {
        return injectors.get(moduleInstances);
    }

    @Override
    public void addGuiceModule(Class<? extends com.google.inject.Module> cls, com.google.inject.Module module) {
        guiceModules.put(cls, module);
    }

    @Override
    public List<com.google.inject.Module> getGuiceModules(Class<? extends com.google.inject.Module> cls) {
        return guiceModules.get(cls);
    }

    @Override
    public Object getAttribute(String name) {
        return attributes.getAttribute(name);
    }

    @Override
    public void setAttribute(String name, Object value) {
        attributes.setAttribute(name, value);
    }

    @Override
    public Set<String> getAttributeNames() {
        return attributes.getAttributeNames();
    }

    @Override
    public Object removeAttribute(String name) {
        return attributes.removeAttribute(name);
    }
}
