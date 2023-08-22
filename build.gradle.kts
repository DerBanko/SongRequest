plugins {
    id("java")
    application
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
    implementation("org.mongodb:mongodb-driver-reactivestreams:4.10.2")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks {
    withType<Jar> {
        val classpath = configurations.runtimeClasspath

        inputs.files(classpath).withNormalizer(ClasspathNormalizer::class.java)

        manifest {
            attributes["Main-Class"] = "tv.banko.songrequest.Main"

            attributes(
                    "Class-Path" to classpath.map { cp -> cp.joinToString(" ") { "./lib/" + it.name } }
            )
        }
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(17)
    }
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("tv.banko.songrequest.Main")
}