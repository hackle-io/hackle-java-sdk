dependencies {
    api(project(":hackle-sdk-common"))
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