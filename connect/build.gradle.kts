import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    id("com.vanniktech.maven.publish") version "0.30.0"
}

android {
    namespace = "com.thirdweb"
    compileSdk = 35

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    api(libs.androidx.core.ktx)
    api(libs.androidx.browser)
    api(libs.kotlinx.serialization)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    signAllPublications()

    coordinates("com.thirdweb", "connect", "0.0.1")

    pom {
        name = "thirdweb connect"
        description = "thirdweb connect SDK for Android"
        url = "http://www.example.com/library"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "thirdweb"
                name = "thirdweb"
                email = "support@thirdweb.com"
            }
        }
        scm {
            connection = "scm:git:github.com/thirdweb-dev/android.git"
            developerConnection = "scm:git:ssh://github.com/thirdweb-dev/android.git"
            url = "https://github.com/thirdweb-dev/android"
        }
    }
}


