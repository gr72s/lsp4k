import org.jreleaser.gradle.plugin.dsl.deploy.maven.MavenCentralMavenDeployer

plugins {
    kotlin("jvm") version "1.9.21"
    id("java-library")
    `maven-publish`
    id("org.jreleaser") version "1.13.1"
    signing
}

val versionDefine = "1.0"
val isRelease = !versionDefine.endsWith("-SNAPSHOT")

group = "io.github.gr72s"
version = versionDefine

extra["name"] = "LSP for Kotlin"
extra["description"] = "JsonRPC implement"

java {
    withJavadocJar()
    withSourcesJar()
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.eclipse.jetty.websocket:jetty-websocket-jetty-client:12.0.5")
    implementation("org.eclipse.jetty.websocket:jetty-websocket-jetty-api:12.0.5")
    implementation("org.eclipse.jetty.websocket:jetty-websocket-jetty-server:12.0.5")

    implementation(kotlin("reflect"))
    implementation("com.google.code.gson:gson:2.10.1")

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

signing {
    sign(publishing.publications.getByName("mavenJava"))
}

tasks.withType<Sign> {
    onlyIf { isRelease }
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
                    url  = "https://central.sonatype.com/api/v1/publisher"
                    stagingRepository("build/staging-deploy")
                }
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
}