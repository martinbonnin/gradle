/*
 * Copyright 2021 the original author or authors.
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

package org.gradle.smoketests

import org.gradle.testkit.runner.TaskOutcome

class GradleBuildSoakTestConfigurationCacheSmokeTest extends AbstractGradleBuildConfigurationCacheSmokeTest {
    def "can run Gradle soak tests with configuration cache enabled"() {

        given:
        def tasks = [
            // TODO: the version of KGP we use still accesses Task.project from a cacheIf predicate
            "-Dorg.gradle.configuration-cache.internal.task-execution-access-pre-stable=true",
            ':soak:forkingIntegTest',
            '--tests=org.gradle.connectivity.MavenCentralDependencyResolveIntegrationTest'
        ]

        when:
        configurationCacheRun(tasks, 0)

        then:
        assertConfigurationCacheStateStored()

        when:
        run([":soak:clean"])

        then:
        configurationCacheRun(tasks, 1)

        then:
        assertConfigurationCacheStateLoaded()
        result.task(":soak:forkingIntegTest").outcome == TaskOutcome.FROM_CACHE
    }
}
