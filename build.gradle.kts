plugins {
    kotlin("jvm") version "1.4.10"
    `java-library`
    `maven-publish`
    signing
    jacoco
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
}

val groupName = "io.hackle"
val versionName = "2.29.0"

group = groupName
version = versionName

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {

    group = groupName
    version = versionName

    apply(plugin = "kotlin")
    apply(plugin = "jacoco")

    dependencies {
        implementation(kotlin("stdlib"))

        testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.2")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.5.2")
        testImplementation("io.mockk:mockk:1.10.0")
        testImplementation("org.assertj:assertj-core:3.11.1")
        testImplementation("io.strikt:strikt-core:0.32.0")
    }

    tasks {
        compileKotlin {
            kotlinOptions {
                freeCompilerArgs = listOf("-Xjsr305=strict")
                jvmTarget = "1.6"
            }
        }
        compileTestKotlin {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.test {
        extensions.configure(JacocoTaskExtension::class) {
            setDestinationFile(file("$buildDir/jacoco/jacoco.exec"))
        }

        finalizedBy("jacocoTestReport")
    }

    jacoco {
        toolVersion = "0.8.5"
    }

    tasks.jacocoTestReport {
        reports {
            xml.isEnabled = true
            html.isEnabled = true
        }
        finalizedBy("jacocoTestCoverageVerification")
    }
}

/** Configure publishing and signing */
nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
            username.set(System.getenv("SONATYPE_USERNAME"))
            password.set(System.getenv("SONATYPE_PASSWORD"))
        }
    }
}

subprojects {

    apply(plugin = "maven-publish")
    apply(plugin = "signing")

    publishing {

        java {
            withJavadocJar()
            withSourcesJar()
        }

        publications {

            create<MavenPublication>("hackleJavaSdk") {

                from(components["java"])

                pom {
                    name.set(project.name)
                    description.set("Hackle SDK for Java and Kotlin")
                    url.set("https://www.hackle.io")

                    licenses {
                        license {
                            name.set("The Apache Software License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }

                    developers {
                        developer {
                            name.set("Hackle")
                        }
                    }

                    scm {
                        connection.set("scm:git:https://github.com/hackle-io/hackle-java-sdk.git")
                        developerConnection.set("scm:git:https://github.com/hackle-io/hackle-java-sdk.git")
                        url.set("https://github.com/hackle-io/hackle-java-sdk")
                    }
                }
            }
        }
    }

    signing {
        val signingKey = System.getenv("SIGNING_KEY")
        val signingPassword = System.getenv("SIGNING_PASSWORD")
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications["hackleJavaSdk"])
    }
}
