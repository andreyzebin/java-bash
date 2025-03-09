/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java library project to get you started.
 * For more details on building Java & JVM projects, please refer to https://docs.gradle.org/8.13/userguide/building_java_projects.html in the Gradle documentation.
 */
import org.gradle.api.tasks.bundling.Jar
buildscript {
    repositories {
        mavenCentral()
        //Needed only for SNAPSHOT versions
        //maven { url "http://oss.sonatype.org/content/repositories/snapshots/" }
    }
    dependencies {
        classpath("io.codearte.gradle.nexus:gradle-nexus-staging-plugin:0.30.0")
    }
}
plugins {
    id("java-library")
    id("io.freefair.lombok") version "8.12.2"
    id("maven-publish")
    id("signing")
}
java {
    withJavadocJar()
    withSourcesJar()
}
repositories {
    mavenCentral()
}
var logbackVersion = "1.5.6"
var slf4jVersion = "2.0.13"
var junitVersion = "5.10.1"


dependencies {
    // Use JUnit Jupiter for testing.
    testImplementation(libs.junit.jupiter)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    compileOnly("ch.qos.logback:logback-core:$logbackVersion")
    compileOnly("ch.qos.logback:logback-classic:$logbackVersion")

    // This dependency is exported to consumers, that is to say found on their compile classpath.
    //api(libs.commons.math3)

    // This dependency is used internally, and not exposed to consumers on their own compile classpath.
    implementation(libs.guava)


    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.1")

    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    testImplementation("ch.qos.logback:logback-core:$logbackVersion")
    testImplementation("ch.qos.logback:logback-classic:$logbackVersion")
    testImplementation("org.slf4j:slf4j-api:$slf4jVersion")
    testImplementation("org.codehaus.janino:janino:3.1.12")

}

// Apply a specific Java toolchain to ease working on different environments.
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()

    testLogging {
        events ("standardOut", "started", "passed", "skipped", "failed")
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "java-bash"
            from(components["java"])
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
            pom {
                name = "Java Bash"
                description = "Run commands in bash terminal session"
                url = "https://github.com/andreyzebin/java-bash"
                licenses {
                    license {
                        name = "MIT License"
                        url = "https://mit-license.org/"
                    }
                }
                developers {
                    developer {
                        id = "andreyzebin"
                        name = "Andrey Zabebenin"
                        email = "andrey.zebin@gmail.com"
                    }
                }
                scm {
                    connection = "https://github.com/andreyzebin/java-bash.git"
                    developerConnection = "https://github.com/andreyzebin/java-bash.git"
                    url = "https://github.com/andreyzebin/java-bash"
                }
            }
        }
    }
    repositories {
        maven {
            // change URLs to point to your repos, e.g. http://my.org/repo
            val releasesRepoUrl = uri(layout.buildDirectory.dir("repos/releases"))
            val snapshotsRepoUrl = uri(layout.buildDirectory.dir("repos/snapshots"))
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
        }
    }
}

tasks.register("printEnvVariables") {
    doLast {
        println("JAVA_HOME: ${System.getenv("JAVA_HOME")}")
    }
}

signing {
    val signingKeyId: String? by project
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
    sign(publishing.publications["mavenJava"])
}

tasks.javadoc {
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}