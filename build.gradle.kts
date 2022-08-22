plugins {
    id("java")
    id("application")
    id("io.freefair.lombok") version "6.5.0.3"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "allink"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("allink.dischat.Main")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.dv8tion:JDA:5.0.0-alpha.18") {
        exclude("club.minnced")
    }
    implementation("club.minnced:discord-webhooks:0.8.2")

    implementation("org.yaml:snakeyaml:1.30")

    // JDA uses SLF4J, LogEvents is the safest choice.
    implementation("org.slf4j:slf4j-api:2.0.0-beta1")
    implementation("org.logevents:logevents:0.3.4")

    implementation("net.jodah:expiringmap:0.5.10")
}
