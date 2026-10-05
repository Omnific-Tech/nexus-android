plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.google.services) apply false
}

tasks.register<Copy>("prepareHerald") {
    val version = providers.gradleProperty("herald.coreVersion").get()
    from(zipTree(file("vendor/herald-core-$version-maven.zip")))
    into("vendor")
}

subprojects {
    tasks.configureEach {
        if (name == "preBuild") dependsOn(rootProject.tasks.named("prepareHerald"))
    }
}

tasks.register("verifySignedApk") {
    doLast {
        val apk = providers.gradleProperty("apk.path").get()
        val sdk = System.getenv("ANDROID_HOME") ?: error("Set ANDROID_HOME")
        val signer = file("$sdk/build-tools/37.0.0/apksigner")
        val certificate = providers.gradleProperty("herald.signingCertificate").get()
        val output = providers.exec {
            commandLine(signer.absolutePath, "verify", "--print-certs", apk)
        }.standardOutput.asText.get()
        require(output.contains("certificate SHA-256 digest: $certificate")) {
            "APK does not match the existing signing certificate"
        }
        val packageName = providers.exec {
            commandLine("apkanalyzer", "manifest", "application-id", apk)
        }.standardOutput.asText.get().trim()
        require(packageName == providers.gradleProperty("herald.applicationId").get()) {
            "APK application ID differs from the installed app"
        }
        val versionCode = providers.exec {
            commandLine("apkanalyzer", "manifest", "version-code", apk)
        }.standardOutput.asText.get().trim().toInt()
        require(versionCode == providers.gradleProperty("herald.versionCode").get().toInt()) {
            "APK versionCode differs from the requested release"
        }
        val manifest = providers.exec {
            commandLine("apkanalyzer", "manifest", "print", apk)
        }.standardOutput.asText.get()
        for (component in listOf(
            "dev.omnific.herald.fcm.HeraldMessagingService",
            "com.google.androidbrowserhelper.trusted.ManageDataLauncherActivity",
            "firebase_messaging_installation_id_enabled",
        )) {
            require(manifest.contains(component)) { "APK is missing $component" }
        }
        println("Verified release identity and Herald components")
    }
}
