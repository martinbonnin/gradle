/*
 * Copyright 2013 the original author or authors.
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

package org.gradle.initialization.buildsrc;

import org.gradle.api.internal.GradleInternal;
import org.gradle.api.internal.provider.DefaultConfigurationTimeBarrier;
import org.gradle.cache.FileLock;
import org.gradle.cache.FileLockManager;
import org.gradle.cache.LockOptions;
import org.gradle.internal.build.BuildState;
import org.gradle.internal.build.BuildStateRegistry;
import org.gradle.internal.build.PublicBuildPath;
import org.gradle.internal.build.StandAloneNestedBuild;
import org.gradle.internal.classpath.ClassPath;
import org.gradle.internal.operations.BuildOperationContext;
import org.gradle.internal.operations.BuildOperationDescriptor;
import org.gradle.internal.operations.BuildOperationExecutor;
import org.gradle.internal.operations.CallableBuildOperation;
import org.gradle.internal.service.scopes.Scopes;
import org.gradle.internal.service.scopes.ServiceScope;

import java.io.File;

import static org.gradle.cache.internal.filelock.LockOptionsBuilder.mode;

@ServiceScope(Scopes.Build.class)
public class BuildSourceBuilder {
    private static final BuildBuildSrcBuildOperationType.Result BUILD_BUILDSRC_RESULT = new BuildBuildSrcBuildOperationType.Result() {
    };

    private final BuildState currentBuild;
    private final FileLockManager fileLockManager;
    private final BuildOperationExecutor buildOperationExecutor;
    private final BuildSrcBuildListenerFactory buildSrcBuildListenerFactory;
    private final BuildStateRegistry buildRegistry;
    private final PublicBuildPath publicBuildPath;
    private final DefaultConfigurationTimeBarrier configurationTimeBarrier;

    public BuildSourceBuilder(
        BuildState currentBuild,
        FileLockManager fileLockManager,
        BuildOperationExecutor buildOperationExecutor,
        BuildSrcBuildListenerFactory buildSrcBuildListenerFactory,
        BuildStateRegistry buildRegistry,
        PublicBuildPath publicBuildPath,
        DefaultConfigurationTimeBarrier configurationTimeBarrier
    ) {
        this.currentBuild = currentBuild;
        this.fileLockManager = fileLockManager;
        this.buildOperationExecutor = buildOperationExecutor;
        this.buildSrcBuildListenerFactory = buildSrcBuildListenerFactory;
        this.buildRegistry = buildRegistry;
        this.publicBuildPath = publicBuildPath;
        this.configurationTimeBarrier = configurationTimeBarrier;
    }

    public ClassPath buildAndGetClassPath(GradleInternal gradle) {
        StandAloneNestedBuild buildSrcBuild = buildRegistry.getBuildSrcNestedBuild(currentBuild);
        if (buildSrcBuild == null) {
            return ClassPath.EMPTY;
        }

        return buildOperationExecutor.call(new CallableBuildOperation<ClassPath>() {
            @Override
            public ClassPath call(BuildOperationContext context) {
                boolean configurationTimeBarrierWasReadyToCross = configurationTimeBarrier.isReadyToCross();
                ClassPath result;
                try {
                    /*
                     * Right now, we don't want to track problems in buildSrc execution time,
                     * so don't cross the configuration time barrier there;
                     * Instead, we might want to handle the barrier in the same way as in normal builds
                     * and after the buildSrc is built, set it back to configuration time with DefaultConfigurationTimeBarrier.prepare
                     */
                    configurationTimeBarrier.setReadyToCross(false);
                    result = buildBuildSrc(buildSrcBuild);
                } finally {
                    configurationTimeBarrier.setReadyToCross(configurationTimeBarrierWasReadyToCross);
                }
                context.setResult(BUILD_BUILDSRC_RESULT);
                return result;
            }

            @Override
            public BuildOperationDescriptor.Builder description() {
                //noinspection Convert2Lambda
                return BuildOperationDescriptor.displayName("Build buildSrc").
                    progressDisplayName("Building buildSrc").
                    details(
                        new BuildBuildSrcBuildOperationType.Details() {
                            @Override
                            public String getBuildPath() {
                                return publicBuildPath.getBuildPath().toString();
                            }
                        }
                    );
            }
        });
    }

    @SuppressWarnings("try")
    private ClassPath buildBuildSrc(StandAloneNestedBuild buildSrcBuild) {
        return buildSrcBuild.run(buildController -> {
            try (FileLock ignored = buildSrcBuildLockFor(buildSrcBuild)) {
                return new BuildSrcUpdateFactory(buildSrcBuildListenerFactory).create(buildController);
            }
        });
    }

    private FileLock buildSrcBuildLockFor(StandAloneNestedBuild build) {
        return fileLockManager.lock(
            new File(build.getBuildRootDir(), ".gradle/noVersion/buildSrc"),
            LOCK_OPTIONS,
            "buildSrc build lock"
        );
    }

    private static final LockOptions LOCK_OPTIONS = mode(FileLockManager.LockMode.Exclusive).useCrossVersionImplementation();
}
