plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.deltasoft.pharmatracker"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.deltasoft.pharmatracker"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")//TODO: Change

        }
    }

    flavorDimensions += "environment"

    productFlavors {
        create("staging") {
            dimension = "environment"
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
            buildConfigField("String", "BASE_API_URL", "\"https://staging.pharmatracker.in/api/\"")
            buildConfigField("String", "DD_APP_ID", "\"staging-pharma-tracker-android\"")

        }
        create("production") {
            dimension = "environment"
            buildConfigField("String", "BASE_API_URL", "\"https://pharmatracker.in/api/\"")
            buildConfigField("String", "DD_APP_ID", "\"production-pharma-tracker-android\"")

        }
    }

    // ✅ disable production locally
    //Wrap it in a condition that checks if you’re running inside CI (GitHub Actions sets CI=true in env):
    androidComponents {
        beforeVariants { variantBuilder ->
            val isCi = System.getenv("CI")?.toBoolean() ?: false
            if (!isCi && variantBuilder.flavorName == "production") {
                variantBuilder.enable = false
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.google.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.datadog.core)
    implementation(libs.datadog.logs)
    implementation(libs.datadog.rum)
    implementation(libs.datadog.session.replay)   // ✅ new

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)




    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.lifecycle)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.navigation.compose)

    // pager
    implementation(libs.accompanist.pager)
    implementation(libs.accompanist.pager.indicators)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)

    implementation(libs.androidx.camera.core.v131)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle.v131)
    implementation(libs.androidx.camera.view.v131)


    implementation(libs.barcode.scanning)

    implementation(libs.accompanist.permissions)
}

