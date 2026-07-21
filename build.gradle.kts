plugins {
    java
    id("com.gradleup.shadow") version "9.4.3"
}

val targetJavaVersion = 21

allprojects {
    group = "ru.matveylegenda"
    version = "1.0.3"

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://jitpack.io")
    }
}

subprojects {
    pluginManager.apply("java")

    dependencies {
        compileOnly(rootProject.files("libs/tiAuth-1.4.1.jar"))

        compileOnly("net.kyori:adventure-api:5.2.0")
        compileOnly("net.kyori:adventure-text-minimessage:5.2.0")
        compileOnly("net.kyori:adventure-text-serializer-legacy:5.2.0")

        implementation("net.dv8tion:JDA:6.5.0") {
            exclude(module = "opus-java")
        }
        implementation("org.telegram:telegrambots-client:10.0.0")
        implementation("org.telegram:telegrambots-longpolling:10.0.0")
        implementation("com.github.1050TIt0p:java-vk-bots-long-poll-api:4.1.9")

        implementation("ru.etsft.max:max-bot-api-client:0.3.1")
        implementation("ru.etsft.max:max-bot-api-jackson:0.3.1")
        implementation("ru.etsft.max:max-bot-api-longpolling:0.3.1")

        compileOnly("org.projectlombok:lombok:1.18.46")
        annotationProcessor("org.projectlombok:lombok:1.18.46")
    }

    extensions.configure<JavaPluginExtension> {
        val javaVersion = JavaVersion.toVersion(targetJavaVersion)

        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion

        if (JavaVersion.current() < javaVersion) {
            toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(targetJavaVersion)
    }
}

dependencies {
    implementation(project(":common"))
    implementation(project(":bungee"))
    implementation(project(":velocity"))
}

tasks.shadowJar {
    archiveClassifier.set("")

    exclude("META-INF/maven/**")
    exclude("org/slf4j/**")
    exclude("google/protobuf/**")
    exclude("com/google/protobuf/**")
    exclude("org/checkerframework/**")
    exclude("**/package-info.class")

    minimize {
        exclude(project(":common"))
        exclude(project(":bungee"))
        exclude(project(":velocity"))

        exclude("ru/matveylegenda/.*")
    }

    relocate("net.dv8tion", "ru.matveylegenda.socialaddon.net.dv8tion")
    relocate("org.telegram", "ru.matveylegenda.socialaddon.org.telegram")
}

tasks.jar {
    enabled = false
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
