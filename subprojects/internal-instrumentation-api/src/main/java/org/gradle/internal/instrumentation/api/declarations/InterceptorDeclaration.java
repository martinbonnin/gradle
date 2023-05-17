/*
 * Copyright 2023 the original author or authors.
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

package org.gradle.internal.instrumentation.api.declarations;

import org.gradle.internal.instrumentation.api.annotations.CallableDefinition;
import org.gradle.internal.instrumentation.api.annotations.CallableKind;
import org.gradle.internal.instrumentation.api.annotations.InterceptCalls;
import org.gradle.internal.instrumentation.api.annotations.SpecificGroovyCallInterceptors;
import org.gradle.internal.instrumentation.api.annotations.SpecificJvmCallInterceptors;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class InterceptorDeclaration {
    public static final String JVM_BYTECODE_GENERATED_CLASS_NAME_FOR_CONFIG_CACHE = "org.gradle.internal.classpath.generated.InterceptorDeclaration_ConfigCacheJvmBytecode";
    public static final String GROOVY_INTERCEPTORS_GENERATED_CLASS_NAME_FOR_CONFIG_CACHE = "org.gradle.internal.classpath.generated.InterceptorDeclaration_ConfigCacheGroovyInterceptors";
    public static final String JVM_BYTECODE_GENERATED_CLASS_NAME_FOR_PROPERTY_UPGRADES = "org.gradle.internal.classpath.generated.InterceptorDeclaration_PropertyUpgradesJvmBytecode";
    public static final String GROOVY_INTERCEPTORS_GENERATED_CLASS_NAME_FOR_PROPERTY_UPGRADES = "org.gradle.internal.classpath.generated.InterceptorDeclaration_PropertyUpgradesGroovyInterceptors";

    public static final List<String> JVM_BYTECODE_GENERATED_CLASS_NAMES = Collections.unmodifiableList(Arrays.asList(
        JVM_BYTECODE_GENERATED_CLASS_NAME_FOR_CONFIG_CACHE,
        JVM_BYTECODE_GENERATED_CLASS_NAME_FOR_PROPERTY_UPGRADES
    ));

    public static final List<String> GROOVY_INTERCEPTORS_GENERATED_CLASS_NAMES = Collections.unmodifiableList(Arrays.asList(
        GROOVY_INTERCEPTORS_GENERATED_CLASS_NAME_FOR_CONFIG_CACHE,
        GROOVY_INTERCEPTORS_GENERATED_CLASS_NAME_FOR_PROPERTY_UPGRADES
    ));
}
