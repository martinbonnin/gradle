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

import org.gradle.test.fixtures.file.TestFile

class ConfigurationCacheFlowScopeIntegrationTest extends AbstractConfigurationCacheIntegrationTest {

    def '#target plugin can react to task execution result'() {
        given:
        def configCache = newConfigurationCacheFixture()

        and:
        withLavaLampPluginFor target

        when: 'task runs successfully'
        configurationCacheRun 'help'

        then: 'flow action receives build result'
        configCache.assertStateStored(true)
        outputContains '(green)'

        when: 'task from cache runs successfully'
        configurationCacheRun 'help'

        then: 'flow action receives build result'
        configCache.assertStateLoaded()
        outputContains '(green)'

        when: 'task fails'
        buildFile '''
            tasks.register('fail') {
                doLast { assert false }
            }
        '''
        configurationCacheFails 'fail'

        then: 'flow action receives build failure'
        outputContains '(red)'
        configCache.assertStateStored()

        when: 'task from cache fails'
        configurationCacheFails 'fail'

        then: 'flow action receives build failure'
        outputContains '(red)'
        configCache.assertStateLoaded()

        where:
        target << ScriptTarget.values()
    }

    def '#target plugin can react to configuration failure'() {
        given:
        withLavaLampPluginFor target

        and:
        buildFile '''
            assert false
        '''

        when:
        configurationCacheFails 'help'

        then:
        outputContains '(red)'

        where:
        target << ScriptTarget.values()
    }

    enum ScriptTarget {
        PROJECT,
        SETTINGS;

        String getTargetType() {
            toString().capitalize()
        }

        @Override
        String toString() {
            name().toLowerCase()
        }
    }

    private withLavaLampPluginFor(ScriptTarget target) {
        def targetType = target.targetType
        scriptFileFor(target) << """
            import org.gradle.api.flow.*

            class LavaLampPlugin implements Plugin<$targetType> {

                final FlowScope flowScope
                final FlowProviders flowProviders

                @Inject
                LavaLampPlugin(FlowScope flowScope, FlowProviders flowProviders) {
                    this.flowScope = flowScope
                    this.flowProviders = flowProviders
                }

                void apply($targetType target) {
                    flowScope.always(SetLavaLampColor) {
                        parameters.color = flowProviders.requestedTasksResult.map {
                            it.failure.present ? 'red' : 'green'
                        }
                    }
                }
            }

            class SetLavaLampColor implements FlowAction<Parameters> {

                interface Parameters extends FlowParameters {
                    @Input Property<String> getColor()
                }

                void execute(Parameters parameters) {
                    println "(${'$'}{parameters.color.get()})"
                }
            }

            apply type: LavaLampPlugin
        """
    }

    private TestFile scriptFileFor(ScriptTarget target) {
        file(target == ScriptTarget.PROJECT ? 'build.gradle' : 'settings.gradle')
    }
}
