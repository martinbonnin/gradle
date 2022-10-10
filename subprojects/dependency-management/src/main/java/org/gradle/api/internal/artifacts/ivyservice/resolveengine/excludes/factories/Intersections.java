/*
 * Copyright 2019 the original author or authors.
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
package org.gradle.api.internal.artifacts.ivyservice.resolveengine.excludes.factories;

import org.gradle.api.artifacts.ModuleIdentifier;
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.excludes.specs.ExcludeSpec;

import java.util.Set;

public class Intersections {
    /**
     * Tries to compute an intersection of 2 specs.
     * The result MUST be a simplification, otherwise this method returns null.
     */
    public static ExcludeSpec tryIntersect(ExcludeSpec left, ExcludeSpec right, ExcludeFactory factory) {
        if (left.equals(right)) {
            return left;
        }

        return left.beginIntersect(right, factory);
    }

    public static ExcludeSpec asUnion(Set<ExcludeSpec> remainder, ExcludeFactory factory) {
        if (remainder.isEmpty()) {
            // It's an intersection, and this method is always called on the remainder
            // of a reduction operation. If the remainder is empty then it means that
            // the intersection is empty
            return factory.nothing();
        }
        return remainder.size() == 1 ? remainder.iterator().next() : factory.anyOf(remainder);
    }

    public static ExcludeSpec moduleIds(Set<ModuleIdentifier> common, ExcludeFactory factory) {
        if (common.isEmpty()) {
            return factory.nothing();
        }
        if (common.size() == 1) {
            return factory.moduleId(common.iterator().next());
        }
        return factory.moduleIdSet(common);
    }

    public static ExcludeSpec moduleIdSet(Set<ModuleIdentifier> moduleIds, ExcludeFactory factory) {
        if (moduleIds.isEmpty()) {
            return factory.nothing();
        }
        if (moduleIds.size() == 1) {
            return factory.moduleId(moduleIds.iterator().next());
        }
        return factory.moduleIdSet(moduleIds);
    }

    public static ExcludeSpec groupSet(Set<String> common, ExcludeFactory factory) {
        if (common.isEmpty()) {
            return factory.nothing();
        }
        if (common.size() == 1) {
            return factory.group(common.iterator().next());
        }
        return factory.groupSet(common);
    }
}
