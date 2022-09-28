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

class TestListsIntegrationTest extends AbstractIntegrationSpec {

    def "can list tests"() {
        given:
        buildFile << """
            apply plugin: 'java-library'
            ${mavenCentralRepository()}

test {

//            jvmArgs '-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005'
            useJUnit()
            onOutput { descriptor, event ->
          logger.lifecycle("Test: " + descriptor + " produced standard out/err: " + event.message )
  }
}
            dependencies { testImplementation "junit:junit:4.13" }
        """

        and:
        file("src/test/java/SomeTest.java") << """
            import org.junit.*;

            public class SomeTest {
                @Test public void foo() {
                    throw new RuntimeException();
                }
            }
        """

        expect:
        succeeds("test", "--dry-run-tests", "--info")
        println(testDirectory.absolutePath)
        System.exit(0)
    }
}
