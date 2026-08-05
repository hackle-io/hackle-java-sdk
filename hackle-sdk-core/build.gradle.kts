dependencies {
    api(project(":hackle-sdk-common"))
    implementation("com.google.code.gson:gson:2.8.9")
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
