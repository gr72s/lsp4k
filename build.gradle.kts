plugins {
    kotlin("jvm") version "1.9.21"
    id("java-library")
    `maven-publish`
    id("org.jreleaser") version "1.13.1"
}

val versionDefine = "1.0.2"
val isRelease = !versionDefine.endsWith("-SNAPSHOT")

group = "io.github.gr72s"
version = versionDefine

extra["name"] = "lsp4k"
extra["description"] = "JsonRPC implement"

java {
    withJavadocJar()
    withSourcesJar()
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    jvmToolchain(11)
}

repositories {
    mavenCentral()
}

dependencies {
    api("org.eclipse.jetty.websocket:websocket-jetty-client:11.0.20")
    api("org.eclipse.jetty.websocket:websocket-jetty-api:11.0.20")
    api("org.eclipse.jetty.websocket:websocket-jetty-server:11.0.20")
    api("com.google.code.gson:gson:2.10.1")

    implementation(kotlin("reflect"))

    testImplementation("org.jetbrains.kotlin:kotlin-test")

    implementation("org.apache.logging.log4j:log4j-core:2.19.0")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.19.0")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = "io.github.gr72s"
            artifactId = "lsp4k"
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
            pom {
                name = "${project.extra["name"]}"
                description = "${project.extra["description"]}"
                url = "https://github.com/gr72s/lsp4k"
                packaging = "jar"
                inceptionYear = "2024"
                licenses {
                    license {
                        name = "MIT"
                        url = "http://www.opensource.org/licenses/mit-license.php"
                    }
                }
                developers {
                    developer {
                        id = "Green"
                        name = "Alan Green"
                        email = "alan_greens@outlook.com"
                        organizationUrl = "https://github.com/gr72s"
                    }
                }
                scm {
                    url = "git@github.com:gr72s/lsp4k.git"
                    connection = "scm:git:git@github.com:gr72s/lsp4k.git"
                    developerConnection = "scm:git:git@github.com:gr72s/lsp4k.git"
                }
            }
        }
    }
    repositories {
        maven {
            url = layout.buildDirectory.dir("staging-deploy").get().asFile.toURI()
        }
    }
}

jreleaser {
    signing {
        setActive("ALWAYS")
        armored = true
    }
    deploy {
        maven {
            mavenCentral {
                create("sonatype") {
                    setActive("ALWAYS")
                    url = "https://central.sonatype.com/api/v1/publisher"
                    stagingRepository("build/staging-deploy")
                }
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
}