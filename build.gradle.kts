// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
        jcenter()
    }
}

plugins {
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.kotlin.scripting) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven { setUrl("https://jitpack.io") }
        //jcenter()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}