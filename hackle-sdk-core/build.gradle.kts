dependencies {
    api(project(":hackle-sdk-common"))
    implementation("com.google.code.gson:gson:2.8.6")
}

tasks.jacocoTestReport {
    classDirectories.setFrom(
        fileTree(project.buildDir) {
            exclude(
                "**/Murmur3.*"
            )
            include(
                "**/classes/**/main/**"
            )
        }
    )
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                counter = "BRANCH"
                minimum = "0.95".toBigDecimal()
            }

            limit {
                counter = "LINE"
                minimum = "0.90".toBigDecimal()
            }
        }
    }
}
