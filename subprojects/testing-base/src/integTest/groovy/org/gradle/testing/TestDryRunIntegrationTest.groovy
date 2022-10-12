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
import org.gradle.integtests.fixtures.TestExecutionResult
import org.gradle.integtests.fixtures.TestNGExecutionResult
import org.gradle.integtests.fixtures.executer.DefaultGradleDistribution
import spock.lang.Shared

class TestDryRunIntegrationTest extends AbstractIntegrationSpec {

    def "dry run #type test is skipping execution and considering as passed in report"(String type, String testSetup, String failingTest) {
        given:
        buildFile << testSetup

        and:
        file("src/test/java/SomeTest.java") << failingTest

        and:
        TestExecutionResult executionResult
        switch (type) {
            case "JUnit":
            case "JUnitPlatform":
                executionResult = new DefaultTestExecutionResult(testDirectory)
                break
            case "TestNG":
                executionResult = new TestNGExecutionResult(testDirectory)
                break
            default:
                throw new IllegalArgumentException()
        }

        expect:
        succeeds("test", "--test-dry-run", "--info", "--debug-jvm")
//        System.exit(0)
        executionResult.testClass("SomeTest").assertTestPassed("failingTest")

        where:
        type            | testSetup          | failingTest
//        "JUnit"         | jUnitSetup         | failingJUnitTest
//        "JUnitPlatform" | jUnitPlatformSetup | failingJUnitPlatformTest
        "TestNG"        | testNgSetup        | failingTestNGTest
    }

    @Shared
    private String jUnitSetup = """
        apply plugin: 'java-library'
        ${mavenCentralRepository()}

        test {
            useJUnit()
        }
        dependencies { testImplementation 'junit:junit:4.13' }
        """

    @Shared
    private String testNgSetup = """
        apply plugin: 'java-library'
        ${mavenCentralRepository()}

        test {
            useTestNG()
        }
        dependencies { testImplementation 'org.testng:testng:6.9.10' }
        """

    @Shared
    private String jUnitPlatformSetup = """
        apply plugin: 'java-library'
        ${mavenCentralRepository()}

        test {
            useJUnitPlatform()
        }

        dependencies {
            testImplementation 'org.junit.jupiter:junit-jupiter-api:5.8.2'
            testImplementation 'org.junit.jupiter:junit-jupiter-engine:5.8.2'
        }
        """

    @Shared
    private String failingJUnitPlatformTest = """
        import org.junit.jupiter.api.*;

        public class SomeTest {
            @Test public void failingTest() {
                throw new RuntimeException();
            }
        }
        """

    @Shared
    private String failingTestNGTest = """
        import org.testng.annotations.*;

        public class SomeTest {
            @Test public void failingTest() {
                throw new RuntimeException();
            }
        }
        """

    @Shared
    private String failingJUnitTest = """
        import org.junit.*;

        public class SomeTest {
            @Test public void failingTest() {
                throw new RuntimeException();
            }
        }
        """
}
