/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose.compiler) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.dependency.analysis.gradle.plugin)
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.kotlinx.kover)
    alias(libs.plugins.pillarbox.detekt)
}

// Configure the `wrapper` task, so it can easily be updated by simply running `./gradlew wrapper`.
tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
    gradleVersion = "latest"
}

val clean by tasks.registering(Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

/*
 * https://detekt.dev/docs/gettingstarted/git-pre-commit-hook
 * https://medium.com/@alistair.cerio/android-ktlint-and-pre-commit-git-hook-5dd606e230a9
 */
val installGitHook by tasks.registering(Copy::class) {
    description = "Install the Git pre-commit hook locally"
    from(file("${rootProject.rootDir}/git_hooks/pre-commit"))
    into { file("${rootProject.rootDir}/.git/hooks") }
    filePermissions {
        unix("rwxr-xr-x")
    }
}

tasks.getByPath(":pillarbox-demo:preBuild").dependsOn(installGitHook)

dependencyAnalysis {
    issues {
        all {
            onUnusedDependencies {
                severity("fail")
            }
        }

        project(":pillarbox-cast") {
            onUnusedDependencies {
                // This dependency is not used directly, but needed if we want to use the default Media3 cast receiver
                exclude(libs.androidx.media3.cast)
            }
        }

        project(":pillarbox-core-business") {
            onUnusedDependencies {
                // This dependency is not used directly, but required to be able to compile `CommandersActStreaming`
                exclude(libs.tagcommander.core)
            }
        }

        project(":pillarbox-player") {
            onUnusedDependencies {
                // These dependencies are not used directly, but automatically used by libs.androidx.media3.exoplayer
                exclude(libs.androidx.media3.dash, libs.androidx.media3.hls)
            }
        }
    }
}
