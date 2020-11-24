dependencies {
    api(project(":hackle-sdk-common"))
    implementation(project(":hackle-sdk-core"))

    api("org.slf4j:slf4j-api:1.7.25")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.11.0")
    implementation("org.apache.httpcomponents:httpclient:4.5.12")

    testImplementation("org.slf4j:slf4j-simple:1.7.25")
}

tasks.withType<ProcessResources> {
    filesMatching("hackle-server-sdk.properties") {
        expand(project.properties)
    }
}
