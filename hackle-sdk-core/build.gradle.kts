dependencies {
    api(project(":hackle-sdk-common"))
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
                minimum = "0.85".toBigDecimal()
            }

            limit {
                counter = "LINE"
                minimum = "0.85".toBigDecimal()
            }
        }
    }
}