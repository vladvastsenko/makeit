plugins { id("com.android.application"); id("org.jetbrains.kotlin.android"); id("org.jetbrains.kotlin.plugin.compose"); id("com.google.gms.google-services") version "4.4.2" apply false }
// Configuration is optional for offline development.
if (file("google-services.json").exists()) apply(plugin = "com.google.gms.google-services")
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
}
dependencies {
 implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
 implementation("com.google.firebase:firebase-auth")
 implementation("com.google.android.gms:play-services-auth:21.3.0")
 implementation(platform("androidx.compose:compose-bom:2024.12.01"))
 implementation("androidx.activity:activity-compose:1.10.0")
 implementation("androidx.compose.material3:material3")
 implementation("androidx.compose.ui:ui")
}
val prepareWelcomeArt by tasks.registering(Copy::class) {
 from(rootProject.file("5eff8f863dbd2a2a7ccc9e0c0632665fc37063aba20fcf9f844a0dce10047be3.png"))
 into(layout.buildDirectory.dir("generated/welcome/res/drawable-nodpi"))
 rename { "welcome_background.png" }
}
android.sourceSets.getByName("main").res.srcDir(layout.buildDirectory.dir("generated/welcome/res"))
tasks.named("preBuild").configure { dependsOn(prepareWelcomeArt) }
