plugins {
    kotlin("jvm") version "2.2.20"
    application
    id("cv.dsl.generation")
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    // The included build substitutes this with the repository root. A real
    // consumer uses com.github.tolikttaaa:cv-dsl:<release> through JitPack.
    implementation("io.github.tolikttaaa:cv-dsl:0.1.0-SNAPSHOT")
}

application {
    mainClass.set("example.MainKt")
}

cvGeneration {
    mainClass.set("example.MainKt")
}
