plugins {
    id("nltimer.android.library")
}

android {
    namespace = "com.nltimer.core.debugui"
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.designsystem)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.ui)
}
