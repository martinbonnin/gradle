/*
 * Copyright 2024 the original author or authors.
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

package org.gradle.api.internal.artifacts.dsl.dependencies;

import org.gradle.api.JavaVersion;
import org.gradle.api.attributes.java.TargetJvmVersion;
import org.gradle.api.attributes.plugin.GradlePluginApiVersion;
import org.gradle.api.internal.attributes.AttributeValue;
import org.gradle.api.internal.attributes.AttributesSchemaInternal;
import org.gradle.internal.component.resolution.failure.describer.ResolutionFailureDescriber;
import org.gradle.internal.component.resolution.failure.exception.AbstractResolutionFailureException;
import org.gradle.internal.component.resolution.failure.exception.VariantSelectionException;
import org.gradle.internal.component.resolution.failure.type.IncompatibleGraphVariantFailure;
import org.gradle.internal.component.resolution.failure.interfaces.ResolutionFailure;

import java.util.List;
import java.util.Optional;

/**
 * A {@link ResolutionFailureDescriber} that describes a {@link ResolutionFailure} caused by a requested library
 * (<strong>non-plugin</strong>) requiring a higher JVM version.
 *
 * This is determined by assessing the incompatibility of the {@link TargetJvmVersion#TARGET_JVM_VERSION_ATTRIBUTE}
 * attribute on the requested library and comparing the provided JVM version with the requested JVM version.  Note that the version
 * of the running JVM is <strong>NOT</strong> relevant to this failure.
 *
 * Whether a request is a plugin request or not is determined by the presence of the {@link GradlePluginApiVersion#GRADLE_PLUGIN_API_VERSION_ATTRIBUTE}
 * attribute.
 */
public abstract class TargetJVMVersionOnLibraryTooNewFailureDescriber extends AbstractJVMVersionTooNewFailureDescriber {
    private static final String JVM_VERSION_TOO_HIGH_TEMPLATE = "Dependency resolution is looking for a library compatible with JVM runtime version %s, but '%s' is only compatible with JVM runtime version %s or newer.";

    @Override
    protected JavaVersion getJVMVersion(IncompatibleGraphVariantFailure failure) {
        AttributeValue<Integer> jvmVersionAttribute = failure.getRequestedAttributes().findEntry(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE);
        return JavaVersion.toVersion(jvmVersionAttribute.get());
    }

    @Override
    public boolean canDescribeFailure(IncompatibleGraphVariantFailure failure) {
        boolean isPluginRequest = failure.getRequestedAttributes().contains(GradlePluginApiVersion.GRADLE_PLUGIN_API_VERSION_ATTRIBUTE);
        return !isPluginRequest && isDueToJVMVersionTooNew(failure);
    }

    @Override
    public AbstractResolutionFailureException describeFailure(IncompatibleGraphVariantFailure failure, Optional<AttributesSchemaInternal> schema) {
        JavaVersion minJVMVersionSupported = findMinJVMSupported(failure.getCandidates()).orElseThrow(IllegalStateException::new);
        String message = buildNeedsNewerJDKFailureMsg(failure.describeRequest(), minJVMVersionSupported, failure);
        List<String> resolutions = buildResolutions(suggestChangeLibraryVersion(failure.describeRequest(), minJVMVersionSupported));
        return new VariantSelectionException(message, failure, resolutions);
    }

    private String buildNeedsNewerJDKFailureMsg(String requestedName, JavaVersion minRequiredJVMVersion, IncompatibleGraphVariantFailure failure) {
        return String.format(JVM_VERSION_TOO_HIGH_TEMPLATE, getJVMVersion(failure).getMajorVersion(), requestedName, minRequiredJVMVersion.getMajorVersion());
    }

    private String suggestChangeLibraryVersion(String requestedName, JavaVersion minRequiredJVMVersion) {
        return "Change the dependency on '" + requestedName + "' to an earlier version that supports JVM runtime version " + minRequiredJVMVersion.getMajorVersion() + ".";
    }
}
