plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.desarrollodroide.pagekeeper"
    compileSdk = project.findProperty("compileSdkVersion") as? Int

    defaultConfig {
        applicationId = "com.desarrollodroide.pagekeeper"
        minSdk = project.findProperty("minSdkVersion") as? Int
        targetSdk = project.findProperty("targetSdkVersion") as? Int
        versionCode = project.findProperty("versionCode") as? Int
        versionName = project.findProperty("versionName") as String

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.7"
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(project(":data"))
    implementation(project(":domain"))
    implementation(project(":model"))
    implementation(project(":network"))
    implementation(project(":common"))

    implementation (libs.androidx.core)
    implementation (libs.androidx.lifecycle.runtime )
    implementation (libs.androidx.activity.compose)
    implementation (libs.androidx.navigation.compose)
    implementation (libs.androidx.lifecycle.viewmodel.compose)
    implementation (libs.androidx.lifecycle.runtimeCompose)
    implementation (libs.androidx.preference)

    implementation (libs.compose.ui.ui)
    implementation (libs.compose.ui.tooling.preview)
    debugImplementation (libs.compose.ui.tooling)
    implementation (libs.compose.material3.material3)
    implementation (libs.compose.material.iconsext)
    implementation (libs.compose.runtime.livedata)

   // implementation (libs.bundles.koin)
    implementation (libs.bundles.retrofit)
    implementation (libs.accompanist.permissions)

    implementation ("io.insert-koin:koin-androidx-compose:3.4.1")
    implementation ("androidx.datastore:datastore-preferences:1.0.0")
    implementation ("io.coil-kt:coil-compose:2.4.0")

//    implementation ("io.insert-koin:koin-android:3.3.2")
//    implementation ("io.insert-koin:koin-core:3.3.2")


}
//
//tasks.register("appVersionName") {
//    doLast {
//        project.findProperty("versionName") as String
//    }
//}