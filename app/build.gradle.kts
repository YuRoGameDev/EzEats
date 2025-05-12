import java.io.FileInputStream
import java.util.Properties

val localProperties = Properties().apply {
    load(FileInputStream(rootProject.file("local.properties")))
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")


}
android {
    namespace = "com.example.ezeats"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.ezeats"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "AWS_API_KEY", "\"${localProperties["aws.api.key"]}\"")
        buildConfigField("String", "AWS_SECRET_KEY", "\"${localProperties["aws.secret.key"]}\"")
        buildConfigField("String", "AWS_DYNAMO_DB_TABLE", "\"${localProperties["aws.dynamodb.table"]}\"")
        buildConfigField("String", "GOOGLE_CSE_ID", "\"${localProperties["google.cse.id"]}\"")
        buildConfigField("String", "GOOGLE_SEARCH_API_KEY", "\"${localProperties["google.search.api.key"]}\"")
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/io.netty.versions.properties"
        }
    }
}


dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation("org.jsoup:jsoup:1.16.1")
    implementation("io.coil-kt:coil-compose:2.4.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp("androidx.room:room-compiler:2.7.0")
    implementation("androidx.compose.material:material:1.8.0-rc03")

    implementation("software.amazon.awssdk:dynamodb:2.20.86")
    implementation("software.amazon.awssdk:auth:2.20.86")
    implementation("software.amazon.awssdk:regions:2.20.86")
    implementation("software.amazon.awssdk:core:2.20.86")
    implementation("software.amazon.awssdk:url-connection-client:2.20.86")
}

