@file:Suppress("UnstableApiUsage")

import com.android.build.api.dsl.ManagedVirtualDevice
import java.util.Properties

plugins {
  id("voice.app")
  id("voice.compose")
  id("kotlin-parcelize")
  id("kotlin-kapt")
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.anvil)
}

kapt {
  arguments {
    arg("dagger.fastInit", "enabled")
    arg("dagger.fullBindingGraphValidation", "ERROR")
  }
}

android {

  namespace = "voice.app"

  androidResources {
    generateLocaleConfig = true
  }

  defaultConfig {
    applicationId = "io.github.dermanu.oneaudiobook"
    versionCode = libs.versions.versionCode.get().toInt()
    versionName = libs.versions.versionName.get()

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    ndk {
      abiFilters.clear()
      // Die mitgelieferte FFmpeg-Bibliothek gibt es nur fuer 64-Bit-ARM (siehe README).
      abiFilters += setOf("arm64-v8a")
    }
  }

  signingConfigs {
    // Debug-Builds nutzen einen festen, oeffentlichen Schluessel (signing/debug), damit sich neue Debug-Versionen
    // ueber alte installieren lassen. Er schuetzt nichts und ist ausdruecklich KEIN Release-Schluessel.
    getByName("debug") {
      val properties = Properties()
      val propertiesFile = rootProject.file("signing/debug/signing.properties")
      propertiesFile.inputStream().use { properties.load(it) }
      storeFile = File(propertiesFile.parentFile, "signing.keystore")
      storePassword = properties["STORE_PASSWORD"] as String
      keyAlias = properties["KEY_ALIAS"] as String
      keyPassword = properties["KEY_PASSWORD"] as String
    }

    // Release-Signierung nur, wenn die Umgebung sie bereitstellt (GitHub-Secrets, siehe docs/RELEASE.md).
    // Ohne diese Variablen entsteht ein unsigniertes APK, das z. B. F-Droid selbst signiert.
    val releaseKeystore = System.getenv("RELEASE_KEYSTORE_FILE")
    if (!releaseKeystore.isNullOrBlank()) {
      create("release") {
        storeFile = rootProject.file(releaseKeystore)
        storePassword = System.getenv("RELEASE_KEYSTORE_PASSWORD")
        keyAlias = System.getenv("RELEASE_KEY_ALIAS")
        keyPassword = System.getenv("RELEASE_KEY_PASSWORD")
      }
    }
  }

  buildTypes {
    getByName("release") {
      isMinifyEnabled = true
      isShrinkResources = true
      signingConfig = signingConfigs.findByName("release")
    }
    getByName("debug") {
      isMinifyEnabled = false
      isShrinkResources = false
      applicationIdSuffix = ".debug"
    }
    all {
      setProguardFiles(
        listOf(
          getDefaultProguardFile("proguard-android-optimize.txt"),
          "proguard.pro",
        ),
      )
    }
  }

  testOptions {
    unitTests.isReturnDefaultValues = true
    animationsDisabled = true
    unitTests.isIncludeAndroidResources = true
    execution = "ANDROIDX_TEST_ORCHESTRATOR"
    managedDevices {
      devices.create<ManagedVirtualDevice>("pixel5") {
        device = "Pixel 5"
        apiLevel = 31
      }
      devices.create<ManagedVirtualDevice>("nexus7") {
        device = "Nexus 7"
        apiLevel = 31
      }
      devices.create<ManagedVirtualDevice>("nexus10") {
        device = "Nexus 10"
        apiLevel = 31
      }
      groups.create("screenshotDevices") {
        targetDevices.addAll(devices.toList())
      }
    }
  }

  lint {
    checkDependencies = true
    ignoreTestSources = true
    warningsAsErrors = true
    lintConfig = rootProject.file("lint.xml")
  }

  packaging {
    with(resources.pickFirsts) {
      add("META-INF/atomicfu.kotlin_module")
      add("META-INF/core.kotlin_module")
    }
  }

  buildFeatures {
    viewBinding = true
    buildConfig = true
  }
}

dependencies {
  implementation(projects.strings)
  implementation(projects.datastore)
  implementation(projects.common)
  implementation(projects.data)
  implementation(projects.playback)
  implementation(projects.ffmpeg)
  implementation(projects.scanner)
  implementation(projects.playbackScreen)
  implementation(projects.sleepTimer)
  implementation(projects.settings)
  implementation(projects.folderPicker)
  implementation(projects.bookOverview)
  implementation(projects.migration)
  implementation(projects.search)
  implementation(projects.cover)
  implementation(projects.documentfile)
  implementation(projects.onboarding)
  implementation(projects.bookmark)

  implementation(libs.appCompat)
  implementation(libs.material)
  implementation(libs.datastore)
  implementation(libs.appStartup)

  implementation(libs.serialization.json)

  implementation(libs.materialDialog.core)
  implementation(libs.materialDialog.input)
  implementation(libs.coil)

  debugImplementation(projects.logging.debug)

  implementation(libs.dagger.core)
  kapt(libs.dagger.compiler)

  implementation(libs.androidxCore)

  testImplementation(libs.junit)
  testImplementation(libs.mockk)

  implementation(projects.pref)

  implementation(libs.media3.exoplayer)
  implementation(libs.media3.session)

  implementation(libs.conductor)

  implementation(projects.pref)

  testImplementation(libs.androidX.test.runner)
  testImplementation(libs.androidX.test.junit)
  testImplementation(libs.androidX.test.core)
  testImplementation(libs.robolectric)
  testImplementation(libs.coroutines.test)

  androidTestUtil(libs.androidTest.orchestrator)
  androidTestUtil(libs.androidTest.services.testServices)
  androidTestImplementation(libs.androidTest.services.storage)

  androidTestImplementation(libs.androidTest.espresso)
  androidTestImplementation(libs.androidTest.rules)
  androidTestImplementation(libs.koTest.assert)
  androidTestImplementation(libs.junit)
  androidTestImplementation(libs.compose.ui.test)
  debugImplementation(libs.compose.ui.testManifest)
  androidTestImplementation(libs.compose.ui.testJunit)
  androidTestImplementation(libs.androidX.test.runner)
  androidTestImplementation(libs.androidX.test.core)
  androidTestImplementation(libs.androidX.test.junit)
  androidTestImplementation(libs.uiautomator)

  //PlayBook
}
