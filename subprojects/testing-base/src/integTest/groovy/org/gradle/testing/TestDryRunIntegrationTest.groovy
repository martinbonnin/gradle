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

package org.gradle.testing

import org.gradle.integtests.fixtures.AbstractIntegrationSpec
import org.gradle.integtests.fixtures.DefaultTestExecutionResult

class TestDryRunIntegrationTest extends AbstractIntegrationSpec {

    def "dry run JUnit test is skipping execution"() {
        given:
        buildFile << """
            $jUnitSetup
        """

        and:
        file("src/test/java/SomeTest.java") << """
            import org.junit.*;

            public class SomeTest {
                @Test public void failingTest() {
                    throw new RuntimeException();
                }
            }
        """

        expect:
        succeeds("test", "--test-dry-run", "--info")
    }

    def "dry run JUnit test is considering as passed in reports"() {
        given:
        buildFile << """
            $jUnitSetup
        """

        and:
        file("src/test/java/SomeTest.java") << """
            import org.junit.*;

            public class SomeTest {
                @Test public void failingTest() {
                    throw new RuntimeException();
                }
            }
        """

        when:
        succeeds("test", "--test-dry-run")

        then:
        def result = new DefaultTestExecutionResult(testDirectory)
        result.testClassByHtml("SomeTest").assertTestPassed("failingTest")
        result.testClassByXml("SomeTest").assertTestPassed("failingTest")
    }

    private String jUnitSetup = """
        apply plugin: 'java-library'
        ${mavenCentralRepository()}

        test {
            useJUnit()
        }
        dependencies { testImplementation 'junit:junit:4.13' }
        """
}
