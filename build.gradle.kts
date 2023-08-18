plugins {
    id("java")
}

group = "tv.banko"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.11.0"))
    implementation("com.github.twitch4j:twitch4j:1.16.0")
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("ch.qos.logback:logback-classic:1.3.5")
}

tasks.test {
    useJUnitPlatform()
}