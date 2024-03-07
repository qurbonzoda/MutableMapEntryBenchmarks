plugins {
    kotlin("jvm") version "1.9.20"
    id("org.jetbrains.kotlinx.benchmark") version "0.4.10"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:0.4.10")
    implementation("org.openjdk.jol:jol-core:0.17")
}

benchmark {
    configurations {
        val iterate by creating {
            include("iterateEntry")
            include("iterateKeyValue")
            include("iterateAndSetValue")
            exclude("PolymorphicMapsBenchmark")
        }
        val storeEntries by creating {
            include("storeEntries")
            exclude("PolymorphicMapsBenchmark")
        }
        val polymorphicIterate by creating {
            include("PolymorphicMapsBenchmark.iterateEntry")
            include("PolymorphicMapsBenchmark.iterateKeyValue")
            include("PolymorphicMapsBenchmark.iterateAndSetValue")
        }
        val polymorphicStoreEntries by creating {
            include("PolymorphicMapsBenchmark.storeEntries")
        }
    }

    targets {
        register("main")
    }
}