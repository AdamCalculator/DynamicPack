plugins {
    id("java")
}

group = "com.adamcalculator"
version = "1.0.9"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.bouncycastle:bcpg-jdk18on:1.77")
    implementation("org.bouncycastle:bcprov-ext-jdk18on:1.77") // so larger for mod... 8 MB
    implementation("org.json:json:20231013")

    implementation("commons-codec:commons-codec:1.15")
    implementation("net.lingala.zip4j:zip4j:2.11.5")
    implementation("org.apache.commons:commons-compress:1.24.0")
    implementation("commons-io:commons-io:2.11.0")
    implementation("org.slf4j:slf4j-api:2.0.1")
    implementation("net.lingala.zip4j:zip4j:2.11.5")
    implementation("org.slf4j:slf4j-api:2.0.1")
    implementation("net.lingala.zip4j:zip4j:2.11.5")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}