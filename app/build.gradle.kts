plugins { id("com.android.application"); id("org.jetbrains.kotlin.android"); id("org.jetbrains.kotlin.plugin.compose") }
kotlin { jvmToolchain(17) }
android {
 compileOptions {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
 }
 namespace = "fi.vladi.makeit"
 compileSdk = 35
 defaultConfig { applicationId = "fi.vladi.makeit"; minSdk = 24; targetSdk = 35; versionCode = 1; versionName = "0.1.0" }
 buildFeatures { compose = true }
 splits { abi { isEnable = true; reset(); include("arm64-v8a"); isUniversalApk = false } }
}
dependencies {
 implementation("org.opencv:opencv:4.10.0")
 implementation(platform("androidx.compose:compose-bom:2024.12.01"))
 implementation("androidx.activity:activity-compose:1.10.0")
 implementation("androidx.compose.material3:material3")
 implementation("androidx.compose.ui:ui")
}
