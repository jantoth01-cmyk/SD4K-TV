plugins {
    id("com.android.application")
}

android {
    namespace = "com.niceprice.sd4ktv"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.niceprice.sd4ktv"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation("androidx.media3:media3-exoplayer:1.11.0")
    implementation("androidx.media3:media3-exoplayer-hls:1.11.0")
    implementation("androidx.media3:media3-exoplayer-dash:1.11.0")
    implementation("androidx.media3:media3-datasource:1.11.0")
}
