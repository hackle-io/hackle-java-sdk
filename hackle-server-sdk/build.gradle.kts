dependencies {
    api(project(":hackle-sdk-common"))
    implementation(project(":hackle-sdk-core"))

    api("org.slf4j:slf4j-api:1.7.25")

    implementation("com.google.code.gson:gson:2.8.9")
    implementation("org.apache.httpcomponents:httpclient:4.5.13")

    testImplementation("org.slf4j:slf4j-simple:1.7.25")
}

tasks.withType<ProcessResources> {
    filesMatching("hackle-server-sdk.properties") {
        expand(project.properties)
    }
}
