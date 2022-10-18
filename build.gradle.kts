plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2"
    java
}

group = "net.azisaba"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.ow2.asm:asm:9.4")
}

tasks {
    shadowJar {
        manifest {
            attributes["Main-Class"] = "net.azisaba.clsrefloc.Main"
        }

        archiveFileName.set("clsrefloc.jar")
    }
}
