import dev.detekt.gradle.Detekt
import dev.detekt.gradle.extensions.DetektExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport

// Standalone reusable toolkit and Gradle plugin. Consumers depend on the
// library for the model/DSL and apply cv.dsl.generation for artifact tasks.
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.detekt)
    `java-gradle-plugin`
    `maven-publish`
    jacoco
}

group = "io.github.tolikttaaa"
version = libs.versions.cv.dsl.get()

kotlin {
    jvmToolchain(libs.versions.java.get().toInt())
}

java {
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test-junit5"))
    testImplementation(gradleTestKit())
    testImplementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlin.get()}")
}

gradlePlugin {
    plugins {
        create("cvGeneration") {
            id = "cv.dsl.generation"
            implementationClass = "cv.gradle.CvGenerationPlugin"
            displayName = "CV DSL generation"
            description = "Registers reusable LaTeX, PDF, web-site and preview tasks for a cv-dsl consumer."
        }
    }
}

extensions.configure<DetektExtension> {
    toolVersion = libs.versions.detekt.get()
    buildUponDefaultConfig = true
    parallel = true
}

tasks.withType<Detekt>().configureEach {
    reports {
        checkstyle.required.set(true)
        html.required.set(true)
        sarif.required.set(true)
        markdown.required.set(true)
    }
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
}

tasks.withType<JacocoReport>().configureEach {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}

tasks.withType<JacocoCoverageVerification>().configureEach {
    dependsOn(tasks.test)
    violationRules {
        rule {
            limit {
                minimum = libs.versions.minimum.coverage.get().toBigDecimal()
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        pom {
            name.set("cv-dsl")
            description.set("A type-safe Kotlin DSL that renders one CV model as a static portfolio and LuaLaTeX PDF sources.")
            url.set("https://github.com/tolikttaaa/cv-dsl")
            licenses {
                license {
                    name.set("MIT License")
                    url.set("https://opensource.org/license/mit")
                }
            }
            developers {
                developer {
                    id.set("tolikttaaa")
                    name.set("Anatolii Anishchenko")
                    url.set("https://github.com/tolikttaaa")
                }
            }
            scm {
                connection.set("scm:git:https://github.com/tolikttaaa/cv-dsl.git")
                developerConnection.set("scm:git:ssh://git@github.com/tolikttaaa/cv-dsl.git")
                url.set("https://github.com/tolikttaaa/cv-dsl")
            }
        }
    }
}
