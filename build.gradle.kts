plugins {
    id("java")
    id("application")
    id("com.github.johnrengelman.shadow") version("7.1.2")

}

group = "com.attentive.dp"
version = "1.0-SNAPSHOT"


val flinkVersion = "1.16.0"
val log4jVersion = "2.17.1"

repositories {
    mavenCentral()
}
tasks.named<Jar>("jar") {
    enabled = false
}

application {
    mainClass.set("com.attentive.dp.CDCPull")
}

tasks.shadowJar {
    archiveClassifier.set("")
    isZip64 = true
    dependencies {
        exclude(dependency("org.apache.flink:flink-streaming-java:.*"))
        exclude(dependency("org.apache.flink:flink-table-api-java:.*"))
        exclude(dependency("org.apache.flink:flink-table-api-java-bridge:.*"))
        exclude(dependency("org.slf4j:.*"))
        exclude(dependency("log4j:.*"))
    }
    manifest {
        attributes["Main-Class"] = "com.attentive.ep.JDBCPull"
    }
    mergeServiceFiles()
}

tasks.assemble { dependsOn(tasks.shadowJar) }

dependencies {
    // local dev with web ui
    implementation("org.apache.flink:flink-runtime-web:${flinkVersion}")
    implementation("org.apache.flink:flink-clients:${flinkVersion}")
    implementation("org.apache.flink:flink-connector-files:${flinkVersion}")

    implementation("org.postgresql:postgresql:42.2.25")
    implementation("com.ververica:flink-connector-postgres-cdc:2.3.0")

    runtimeOnly("org.apache.logging.log4j:log4j-slf4j-impl:${log4jVersion}")
    runtimeOnly("org.apache.logging.log4j:log4j-api:${log4jVersion}")
    runtimeOnly("org.apache.logging.log4j:log4j-core:${log4jVersion}")

}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}