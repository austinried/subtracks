import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.roborazzi)
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.aboutLibraries)
}

ktlint {
    version.set(libs.versions.ktlint.get())
}

android {
    namespace = "com.subtracks"
    compileSdk = 37
    buildToolsVersion = "37.0.0"

    defaultConfig {
        applicationId = "com.subtracks.next"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    lint {
        checkReleaseBuilds = false
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    sourceSets
        .getByName("test")
        .kotlin.directories
        .add("src/integrationTest/kotlin")

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            // Debug key so `installRelease` works locally; store and F-Droid builds re-sign.
            signingConfig = signingConfigs.getByName("debug")
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

roborazzi {
    outputDir.set(file("src/test/screenshots"))
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

val integrationTestClasses =
    listOf(
        "com.subtracks.data.source.subsonic.SubsonicSourceIntegrationTest",
        "com.subtracks.data.sync.SyncServiceIntegrationTest",
    )

tasks.withType<Test>().configureEach {
    if (name.endsWith("UnitTest")) {
        filter {
            integrationTestClasses.forEach { excludeTestsMatching(it) }
        }
    }
}

tasks.register<Test>("integrationTest") {
    description = "Runs the integration tests against locally started Subsonic servers"
    group = "verification"
    val unitTest = tasks.named<Test>("testDebugUnitTest")
    dependsOn("compileDebugUnitTestKotlin")
    testClassesDirs = files(provider { unitTest.get().testClassesDirs })
    classpath = files(provider { unitTest.get().classpath })
    filter {
        integrationTestClasses.forEach { includeTestsMatching(it) }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.sqlite.bundled)
    implementation(libs.androidx.room3.runtime)
    implementation(libs.androidx.room3.sqlite.wrapper)
    implementation(libs.androidx.room3.paging)
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.palette)
    implementation(libs.reorderable)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.datasource.okhttp)
    implementation(libs.okhttp)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.koin.androidx.compose)
    implementation(libs.aboutlibraries.core)
    implementation(libs.aboutlibraries.compose.m3)
    ksp(libs.androidx.room3.compiler)

    coreLibraryDesugaring(libs.desugar.jdk.libs)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.roborazzi.junit.rule)
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.sqlite.bundled.jvm)
    testImplementation(libs.coil.test)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.kotlinx.coroutines.test)
}
