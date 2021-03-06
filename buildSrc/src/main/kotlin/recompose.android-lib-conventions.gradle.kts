import com.github.whyrising.recompose.Ci.groupId
import com.github.whyrising.recompose.Versions

plugins {
  id("kotlin-conventions")
  id("com.android.library")
  id("recompose.publishing-conventions")
}

group = groupId

android {
  namespace = groupId
  compileSdk = 32

  buildFeatures {
    compose = true
  }

  defaultConfig {
    minSdk = 22
    targetSdk = 32
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    consumerProguardFiles("consumer-rules.pro")
    aarMetadata {
      minCompileSdk = 22
    }
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
    }
  }

  buildFeatures {
    compose = true
  }

  composeOptions {
    kotlinCompilerExtensionVersion = Versions.COMPOSE_COMPILER
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }

  testOptions {
    unitTests.isReturnDefaultValues = true
  }

  publishing {
    singleVariant("release") {
      withSourcesJar()
      withJavadocJar()
    }
  }
}
