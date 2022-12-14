package projects

import common.Os
import common.VersionedSettingsBranch
import common.applyDefaultSettings
import common.gradleWrapper
import jetbrains.buildServer.configs.kotlin.v2018_1.ui.findBuildFeature
import jetbrains.buildServer.configs.kotlin.v2019_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2019_2.Project
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.parallelTests

class GradleBuildToolRootProject(branch: VersionedSettingsBranch) : Project({
    buildType(BuildType({
        this.name = "Cli Test"
        this.description = "Cli Test"
        this.id("cli_test")

        applyDefaultSettings(Os.LINUX)
        steps {
            gradleWrapper(this@BuildType) {
                name = "GRADLE_RUNNER"
                tasks = "clean cli:test"
                gradleParams = "--no-configuration-cache"
            }
        }
        features {
            parallelTests {
                numberOfBatches = 2
            }
        }
    }))
})
