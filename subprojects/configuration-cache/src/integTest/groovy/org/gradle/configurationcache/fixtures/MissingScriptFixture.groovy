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

package org.gradle.configurationcache.fixtures

import org.gradle.test.fixtures.file.TestFile

class MissingScriptFixture {

    static List<Spec> specs(String... projects) {
        ScriptLanguage.values().collect { ScriptLanguage l ->
            new Spec(l, Arrays.asList(projects))
        }
    }

    static class Spec {
        private final ScriptLanguage scriptLanguage;
        private final List<String> projects;

        Spec(ScriptLanguage scriptLanguage, List<String> projects) {
            this.scriptLanguage = scriptLanguage
            this.projects = projects
        }

        @Override
        String toString() {
            "$scriptLanguage".toLowerCase()
        }

        MissingScriptFixture createFixture() {
            def includedProjects = projects.collect { project -> "\"$project\""}.join(", ")

            switch (scriptLanguage) {
                case ScriptLanguage.GROOVY:
                    def buildScript = 'task ok'
                    def buildScriptFileName = 'build.gradle'
                    def settingsScript = "include $includedProjects"
                    def settingsScriptFileName = "settings.gradle"
                    return new MissingScriptFixture(buildScriptFileName, buildScript, settingsScriptFileName, settingsScript)
                case ScriptLanguage.KOTLIN:
                    def buildScript = 'tasks.register("ok")'
                    def buildScriptFileName = 'build.gradle.kts'
                    def settingsScript = "include ($includedProjects)"
                    def settingsScriptFileName = "settings.gradle.kts"
                    return new MissingScriptFixture(buildScriptFileName, buildScript, settingsScriptFileName, settingsScript)
            }

        }
    }

    enum ScriptLanguage {
        GROOVY,
        KOTLIN
    }

    private final String buildScriptFileName
    private final String buildScript

    private final String settingsScriptFileName
    private final String settingsScript

    MissingScriptFixture(String buildScriptFileName, String buildScript, String settingsScriptFileName, String settingsScript) {
        this.buildScriptFileName = buildScriptFileName
        this.buildScript = buildScript
        this.settingsScriptFileName = settingsScriptFileName
        this.settingsScript = settingsScript
    }

    TestFile createBuildScriptIn(TestFile dir) {
        return dir.file(buildScriptFileName) << buildScript
    }

    TestFile createSettingsScriptIn(TestFile dir) {
        return dir.file(settingsScriptFileName) << settingsScript
    }

    String getBuildScriptFileName() {
        return buildScriptFileName
    }

    String getSettingsScriptFileName() {
        return settingsScriptFileName
    }
}
