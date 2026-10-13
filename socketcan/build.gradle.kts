plugins {
    alias(libs.plugins.kotlinJvm)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    api(project(":core"))
    api(libs.jna)
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(kotlin("test"))
}
