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

package org.gradle.configurationcache

import org.gradle.configurationcache.fixtures.MissingScriptFixture
import org.gradle.test.fixtures.file.TestFile
import spock.lang.Issue

import static org.gradle.util.internal.GFileUtils.relativePathOf

@Issue("https://github.com/gradle/gradle/issues/18897")
class ConfigurationCacheMissingScriptIntegrationTest extends AbstractConfigurationCacheIntegrationTest {

    def "picking up formerly-missing build #missingScriptsSpec scripts"() {
        given:
        def fixture = missingScriptsSpec.createFixture()
        fixture.createSettingsScriptIn(testDirectory)
        fixture.createBuildScriptIn(createDir('a'))

        when:
        configurationCacheRun 'ok'

        then:
        def buildScriptFile = fixture.createBuildScriptIn(createDir('b'))
        def buildScriptFileRelativePath = relativePathOf(buildScriptFile)

        when:
        configurationCacheRun 'ok'

        then:
        outputContains("Calculating task graph as configuration cache cannot be reused because file '$buildScriptFileRelativePath' has changed.")
        result.assertTasksExecuted(":a:ok", ":b:ok")

        where:
        missingScriptsSpec << MissingScriptFixture.specs('a', 'b')
    }

    def "picking up formerly-missing settings #missingScriptsSpec script"() {
        given:
        useTestDirectoryThatIsNotEmbeddedInAnotherBuild()

        def fixture = missingScriptsSpec.createFixture()
        fixture.createBuildScriptIn(testDirectory)
        fixture.createBuildScriptIn(createDir('a'))
        fixture.createBuildScriptIn(createDir('b'))

        and:
        configurationCacheRun 'ok'

        and:
        def settingsScriptFile = fixture.createSettingsScriptIn(testDirectory)
        def settingsScriptFileRelativePath = relativePathOf(settingsScriptFile)

        when:
        configurationCacheRun 'ok'

        then:
        outputContains("Calculating task graph as configuration cache cannot be reused because file '$settingsScriptFileRelativePath' has changed.")
        result.assertTasksExecuted(":ok", ":a:ok", ":b:ok")

        where:
        missingScriptsSpec << MissingScriptFixture.specs('a', 'b')
    }

    def "picking up formerly-missing buildSrc/settings #missingScriptsSpec script"() {
        given:
        def fixture = missingScriptsSpec.createFixture()
        fixture.createBuildScriptIn(testDirectory)
        fixture.createBuildScriptIn(createDir("buildSrc"))
        fixture.createBuildScriptIn(createDir("buildSrc/a"))


        and:
        configurationCacheRun 'ok'

        and:
        def settingsScriptFile = fixture.createSettingsScriptIn(createDir("buildSrc"))
        def settingsScriptFileRelativePath = relativePathOf(settingsScriptFile)

        when:
        configurationCacheRun 'ok'

        then:
        outputContains("Calculating task graph as configuration cache cannot be reused because file '$settingsScriptFileRelativePath' has changed.")

        where:
        missingScriptsSpec << MissingScriptFixture.specs('a')
    }

    private String relativePathOf(TestFile file) {
        relativePathOf(file, testDirectory)
    }
}
