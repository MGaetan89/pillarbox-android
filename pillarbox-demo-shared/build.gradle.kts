/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

plugins {
    alias(libs.plugins.pillarbox.android.library)
}

dependencies {
    compileOnly(project(":pillarbox-core-business"))
    api(project(":pillarbox-player"))

    implementation(libs.androidx.annotation)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.ui.graphics)
    api(libs.androidx.lifecycle.viewmodel)
    api(libs.androidx.media3.common)
    implementation(libs.androidx.media3.exoplayer)
    api(libs.androidx.navigation.common)
    api(libs.androidx.navigation.runtime)
    implementation(libs.androidx.paging.common)
    api(libs.kotlinx.coroutines.core)
    implementation(libs.okhttp)
    api(libs.srg.data)
    api(libs.srg.dataprovider.paging)
    api(libs.srg.dataprovider.retrofit)

    testImplementation(libs.junit)
}
