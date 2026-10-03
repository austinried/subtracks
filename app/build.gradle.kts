import org.gradle.api.tasks.testing.logging.TestExceptionFormat
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
        applicationId = "com.subtracks"
        minSdk = 24
        targetSdk = 37
        versionCode = 13
        versionName = "3.0.0"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    androidResources {
        generateLocaleConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    lint {
        checkReleaseBuilds = false
        warningsAsErrors = true
        disable +=
            setOf(
                "MissingTranslation",
                "MissingQuantity",
                // Version nags are handled by the dependency-update process, not the build.
                "AndroidGradlePluginVersion",
                "GradleDependency",
                "NewerVersionAvailable",
            )
    }

    packaging {
        jniLibs {
            // Prebuilt .so files with no symbol table; leaving them unstripped avoids the strip warning.
            keepDebugSymbols +=
                setOf(
                    "**/libandroidx.graphics.path.so",
                    "**/libdatastore_shared_counter.so",
                    "**/libsqliteJni.so",
                )
        }
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

    signingConfigs {
        val keystorePath = providers.environmentVariable("RELEASE_KEYSTORE").orNull
        if (keystorePath != null) {
            create("release") {
                storeFile = file(keystorePath)
                storePassword = providers.environmentVariable("RELEASE_KEYSTORE_PASSWORD").orNull
                keyAlias = providers.environmentVariable("RELEASE_KEY_ALIAS").orNull
                keyPassword = providers.environmentVariable("RELEASE_KEY_PASSWORD").orNull
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            // Release signing when the keystore is supplied (CI); debug elsewhere so local build/install works.
            signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        allWarningsAsErrors.set(true)
    }
}

roborazzi {
    outputDir.set(file("src/test/screenshots"))
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

// Resolving the variant classpaths here makes AGP warn that a configuration was resolved during
// configuration time; upstream, tracked in mikepenz/AboutLibraries#1369.
aboutLibraries {
    collect {
        configPath = file("config")
    }
}

val integrationTestClasses =
    listOf(
        "com.subtracks.data.source.subsonic.SubsonicAuthIntegrationTest",
        "com.subtracks.data.source.subsonic.SubsonicErrorIntegrationTest",
        "com.subtracks.data.source.subsonic.SubsonicMediaIntegrationTest",
        "com.subtracks.data.source.subsonic.SubsonicSourceIntegrationTest",
        "com.subtracks.data.source.subsonic.SubsonicWriteIntegrationTest",
        "com.subtracks.data.sync.MultiSourceSyncIntegrationTest",
        "com.subtracks.data.sync.PruneSyncIntegrationTest",
        "com.subtracks.data.sync.SyncServiceIntegrationTest",
    )

val demoScreenshotClass = "com.subtracks.ui.DemoScreenshotTest"

tasks.withType<Test>().configureEach {
    if (name.endsWith("UnitTest")) {
        filter {
            (integrationTestClasses + demoScreenshotClass).forEach { excludeTestsMatching(it) }
        }
    }
    testLogging {
        exceptionFormat = TestExceptionFormat.FULL
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
    systemProperty("prune.baseUrl", providers.gradleProperty("pruneBaseUrl").getOrElse(""))
    systemProperty("prune.musicDir", providers.gradleProperty("pruneMusicDir").getOrElse(""))
}

tasks.register<Test>("demoScreenshots") {
    description = "Records the store screenshots from a real sync of the public Navidrome demo library"
    group = "verification"
    val unitTest = tasks.named<Test>("testDebugUnitTest")
    dependsOn("compileDebugUnitTestKotlin")
    testClassesDirs = files(provider { unitTest.get().testClassesDirs })
    classpath = files(provider { unitTest.get().classpath })
    filter {
        includeTestsMatching(demoScreenshotClass)
    }
    systemProperty("roborazzi.test.record", "true")
    outputs.upToDateWhen { false }
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
