plugins {
    kotlin("jvm") version "2.1.20"
}

group = "org.example"
version = "1.0-SNAPSHOT"

val kotestVersion = "5.9.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.3")

    implementation(platform("org.http4k:http4k-bom:6.9.0.0"))
    implementation("org.http4k:http4k-core")
    implementation("org.http4k:http4k-server-undertow")
    implementation("org.http4k:http4k-client-apache")

    implementation("io.arrow-kt:arrow-core:2.1.0")

    testImplementation(kotlin("test"))

    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-extensions-jvm:${kotestVersion}")
    testImplementation("io.kotest.extensions:kotest-assertions-arrow:2.0.0")
}

tasks.withType<Test>().configureEach {
    jvmArgs("--add-opens=java.base/java.util=ALL-UNNAMED")
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(22)
}