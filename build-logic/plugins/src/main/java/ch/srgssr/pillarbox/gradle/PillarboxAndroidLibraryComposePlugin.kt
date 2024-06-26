/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.gradle

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Custom Gradle plugin to configure Compose in an Android library module for Pillarbox.
 */
class PillarboxAndroidLibraryComposePlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

        extensions.configure<LibraryExtension> {
            buildFeatures {
                compose = true
            }
        }
    }
}
