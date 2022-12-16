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

package org.gradle.api.internal.provider;

import org.gradle.api.provider.Provider;

/**
 * A {@link org.gradle.api.provider.ProviderFactory} which only provides
 * environment variables, system properties, and Gradle properties.
 */
public class BasicProviderFactory extends DefaultProviderFactory {
    @Override
    public Provider<String> environmentVariable(Provider<String> variableName) {
        return variableName.map(name -> System.getenv(name));
    }

    @Override
    public Provider<String> systemProperty(Provider<String> propertyName) {
        return propertyName.map(name -> System.getProperty(name));
    }

    @Override
    public Provider<String> gradleProperty(Provider<String> propertyName) {
        return systemProperty(propertyName);
    }
}
