import com.vanniktech.maven.publish.SonatypeHost

plugins {
    kotlin("jvm") version "2.1.20"
    id("com.vanniktech.maven.publish") version "0.31.0"
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


mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

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

signing {
    useInMemoryPgpKeys(
        findProperty("signing.inMemoryKey")?.toString(),
        findProperty("signing.password")?.toString(),
    )
}
