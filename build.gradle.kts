import java.io.FileInputStream
import java.util.Properties

plugins {
    kotlin("jvm") version "2.1.20"
    `maven-publish`
    signing
}

group = "org.example.detekt"
version = "1.0-SNAPSHOT"

/**
 * Loads local build specified properties.
 */
val Project.buildProperties
    get() = Properties().apply {
        val buildPropertiesFile = rootProject.file("build.properties")
        if (buildPropertiesFile.exists()) {
            FileInputStream(buildPropertiesFile).use { load(it) }
        }
    }

dependencies {
    compileOnly("io.gitlab.arturbosch.detekt:detekt-api:1.23.8")

    testImplementation("io.gitlab.arturbosch.detekt:detekt-test:1.23.8")
    testImplementation("io.kotest:kotest-assertions-core:5.9.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.12.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    jvmToolchain(17)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    systemProperty("junit.jupiter.testinstance.lifecycle.default", "per_class")
    systemProperty("compile-snippet-tests", project.hasProperty("compile-test-snippets"))
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                name = "kivi-ruleset"
                description = "Detekt Rules by Kivi"
                url = "https://github.com/lucas-livefront/kivi-ruleset"
                inceptionYear = "2025"

                licenses {
                    license {
                        name = "The Apache License, Version 2.0"
                        distribution = "repo"
                        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                    }
                }

                developers {
                    developer {
                        id = "lucas-livefront"
                        name = "Lucas"
                    }
                }

                scm {
                    connection = "scm:git:git://github.com/lucas-livefront/kivi-ruleset.git"
                    url = "https://github.com/lucas-livefront/kivi-ruleset"
                }
            }
        }
    }

    repositories {
        maven {
            url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")
            credentials {
                username = buildProperties.getProperty("sonatype.username")
                password = buildProperties.getProperty("sonatype.password")
            }
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications["mavenJava"])
}
