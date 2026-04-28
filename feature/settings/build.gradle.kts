plugins {
    id("nltimer.android.library")
    id("nltimer.android.hilt")
}

android {
    namespace = "com.nltimer.feature.settings"
}

dependencies {
    implementation(projects.core.designsystem)
    implementation(projects.core.data)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)

    implementation(libs.materialkolor)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.androidx.activity.compose)
    debugImplementation(libs.compose.ui.tooling)
}
