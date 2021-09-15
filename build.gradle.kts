plugins {
    kotlin("jvm") version "1.4.10"
    `java-library`
    `maven-publish`
    signing
    jacoco
}

allprojects {
    repositories {
        mavenCentral()
        jcenter()
    }
}

subprojects {

    group = "io.hackle"
    version = "2.1.0"

    apply(plugin = "kotlin")
    apply(plugin = "jacoco")

    dependencies {
        implementation(kotlin("stdlib-jdk8"))
        implementation(kotlin("reflect"))

        testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.2")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.5.2")
        testImplementation("io.mockk:mockk:1.10.0")
        testImplementation("org.assertj:assertj-core:3.11.1")
        testImplementation("io.strikt:strikt-core:0.27.0")
    }

    tasks {
        compileKotlin {
            kotlinOptions {
                freeCompilerArgs = listOf("-Xjsr305=strict")
                jvmTarget = "1.8"
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
subprojects {

    val project = this@subprojects

    apply(plugin = "maven-publish")
    apply(plugin = "signing")

    publishing {

        java {
            withJavadocJar()
            withSourcesJar()
        }

        repositories {
            maven {
                name = "MavenCentral"
                val releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2"
                val snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots"

                val snapshotRepoTags = listOf("RC", "SNAPSHOT")
                val repoUrl =
                    if (snapshotRepoTags.any { version.toString().contains(it) }) snapshotsRepoUrl else releasesRepoUrl

                url = uri(repoUrl)
                credentials {
                    username = System.getenv("SONATYPE_USERNAME")
                    password = System.getenv("SONATYPE_PASSWORD")
                }
            }
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
                        connection.set("scm:git:https://github.com/hackle-io/hackle-jvm-sdk.git")
                        developerConnection.set("scm:git:https://github.com/hackle-io/hackle-jvm-sdk.git")
                        url.set("https://github.com/hackle-io/hackle-jvm-sdk")
                    }
                }
            }
        }
    }

    signing {
        sign(publishing.publications["hackleJavaSdk"])
    }
}
