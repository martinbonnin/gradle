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

import com.google.common.collect.Sets;
import org.gradle.api.artifacts.ModuleIdentifier;
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.excludes.specs.ExcludeAnyOf;
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.excludes.specs.ExcludeNothing;
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.excludes.specs.ExcludeSpec;
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.excludes.specs.GroupExclude;
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.excludes.specs.ModuleIdSetExclude;

import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class Intersections {
    private final ExcludeFactory factory;

    public Intersections(ExcludeFactory factory) {
        this.factory = factory;
    }

    /**
     * Tries to compute an intersection of 2 specs.
     * The result MUST be a simplification, otherwise this method returns null.
     */
    public ExcludeSpec tryIntersect(ExcludeSpec left, ExcludeSpec right) {
        if (left.equals(right)) {
            return left;
        } else {
            return left.beginIntersect(right, factory);
        }
    }

    public static ExcludeSpec doIntersect(ExcludeAnyOf left, ExcludeSpec right, ExcludeFactory factory) {
        Set<ExcludeSpec> leftComponents = left.getComponents();
        // Here, we will distribute A ∩ (B ∪ C) if, and only if, at
        // least one of the distribution operations (A ∩ B) can be simplified
        ExcludeSpec[] excludeSpecs = leftComponents.toArray(new ExcludeSpec[0]);
        ExcludeSpec[] intersections = null;
        for (int i = 0; i < excludeSpecs.length; i++) {
            ExcludeSpec excludeSpec = excludeSpecs[i].beginIntersect(right, factory);
            if (excludeSpec != null) {
                if (intersections == null) {
                    intersections = new ExcludeSpec[excludeSpecs.length];
                }
                intersections[i] = excludeSpec;
            }
        }
        if (intersections != null) {
            Set<ExcludeSpec> simplified = Sets.newHashSetWithExpectedSize(excludeSpecs.length);
            for (int i = 0; i < intersections.length; i++) {
                ExcludeSpec intersection = intersections[i];
                if (intersection instanceof ExcludeNothing) {
                    continue;
                }
                if (intersection != null) {
                    simplified.add(intersection);
                } else {
                    simplified.add(factory.allOf(excludeSpecs[i], right));
                }
            }
            return factory.fromUnion(simplified);
        } else {
            return null;
        }
    }

    public static ExcludeSpec doIntersect(ExcludeAnyOf left, ExcludeAnyOf right, ExcludeFactory factory) {
        Set<ExcludeSpec> leftComponents = left.getComponents();
        Set<ExcludeSpec> rightComponents = right.getComponents();
        Set<ExcludeSpec> common = Sets.newHashSet(leftComponents);
        common.retainAll(rightComponents);
        if (common.size() >= 1) {
            ExcludeSpec alpha = factory.fromUnion(common);
            if (leftComponents.equals(common) || rightComponents.equals(common)) {
                return alpha;
            }
            Set<ExcludeSpec> remainderLeft = Sets.newHashSet(leftComponents);
            remainderLeft.removeAll(common);
            Set<ExcludeSpec> remainderRight = Sets.newHashSet(rightComponents);
            remainderRight.removeAll(common);

            ExcludeSpec unionLeft = factory.fromUnion(remainderLeft);
            ExcludeSpec unionRight = factory.fromUnion(remainderRight);
            ExcludeSpec beta = factory.allOf(unionLeft, unionRight);
            return factory.anyOf(alpha, beta);
        } else {
            // slowest path, full distribution
            // (A ∪ B) ∩ (C ∪ D) = (A ∩ C) ∪ (A ∩ D) ∪ (B ∩ C) ∪ (B ∩ D)
            Set<ExcludeSpec> intersections = Sets.newHashSetWithExpectedSize(leftComponents.size() * rightComponents.size());
            for (ExcludeSpec leftSpec : leftComponents) {
                for (ExcludeSpec rightSpec : rightComponents) {
                    ExcludeSpec merged = leftSpec.beginIntersect(rightSpec, factory);
                    //ExcludeSpec merged = leftSpec.beginIntersect(rightSpec, factory);
                    if (merged == null) {
                        merged = factory.allOf(leftSpec, rightSpec);
                    }
                    if (!(merged instanceof ExcludeNothing)) {
                        intersections.add(merged);
                    }
                }
            }
            return factory.fromUnion(intersections);
        }
    }





    public static ExcludeSpec doIntersect(GroupExclude left, ModuleIdSetExclude right, ExcludeFactory factory) {
        String group = left.getGroup();
        Set<ModuleIdentifier> moduleIds = right.getModuleIds().stream().filter(id -> id.getGroup().equals(group)).collect(toSet());
        return factory.fromModuleIds(moduleIds);
    }
}
