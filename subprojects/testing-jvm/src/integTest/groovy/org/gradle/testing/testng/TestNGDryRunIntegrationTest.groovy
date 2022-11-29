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

package org.gradle.testing.testng

import org.gradle.integtests.fixtures.DefaultTestExecutionResult
import org.gradle.integtests.fixtures.MultiVersionIntegrationSpec
import org.gradle.integtests.fixtures.TargetCoverage
import org.gradle.integtests.fixtures.TestExecutionResult
import org.gradle.testing.fixture.TestNGCoverage

@TargetCoverage({ TestNGCoverage.SUPPORTED_BY_JDK })
class TestNGDryRunIntegrationTest extends MultiVersionIntegrationSpec {

    def "dry run test is skipping execution and considering as passed in report"() {
        given:
        buildFile << testNgSetup()

        and:
        file("src/test/java/SomeTest.java") << failingTestNGTest
        TestExecutionResult executionResult = new DefaultTestExecutionResult(testDirectory)

        expect:
        succeeds("test", "--test-dry-run")
        executionResult.testClass("SomeTest").assertTestPassed("failingTest")
    }

    private String testNgSetup() {
        return """
        apply plugin: 'java-library'
        ${mavenCentralRepository()}

        test {
            useTestNG()
        }
        dependencies { testImplementation 'org.testng:testng:${versionNumber}' }
        """
    }

    private String failingTestNGTest = """
        import org.testng.annotations.*;

        public class SomeTest {
            @Test public void failingTest() {
                throw new RuntimeException();
            }
        }
        """
}
