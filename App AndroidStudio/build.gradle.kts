// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    //Agregar dependencias a Google services gradle plugin
    id("com.google.gms.google-services") version "4.4.2" apply false
    alias(libs.plugins.android.application) apply false
}