plugins {
    kotlin("jvm")
    id("com.jakewharton.mosaic")
    id("application")
}

application {
    mainClass.set("cz.root.reader.console.MainKt")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation(project(":shared"))
}