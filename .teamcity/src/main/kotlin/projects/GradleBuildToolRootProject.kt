package projects

import common.Os
import common.VersionedSettingsBranch
import common.applyDefaultSettings
import common.gradleWrapper
import jetbrains.buildServer.configs.kotlin.v2019_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2019_2.Project

class GradleBuildToolRootProject(branch: VersionedSettingsBranch) : Project({
    buildType(BuildType({
        this.name = "Cli Test"
        this.description = "Cli Test"
        this.id("cli-test")

        applyDefaultSettings(Os.LINUX)
        steps {
            gradleWrapper(this@BuildType) {
                name = "GRADLE_RUNNER"
                tasks = "clean cli:test"
            }
        }
    }))
})
