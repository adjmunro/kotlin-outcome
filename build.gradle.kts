import org.gradle.accessors.dm.LibrariesForLibs.VersionAccessors
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.dokka)
    jacoco
    `maven-publish`
}

private val Project.semver: String by lazy {
    val major = version { project.version.major }
    val minor = version { project.version.minor }
    val patch = version { project.version.patch }
    return@lazy "$major.$minor.$patch"
}

private fun Project.version(selector: VersionAccessors.() -> Provider<String>): String {
    return this@version.libs.versions.selector().get()
}

group = version { project.group.id }
version = semver

java {
    sourceCompatibility = JavaVersion.toVersion(version { java.toolchain })
    targetCompatibility = JavaVersion.toVersion(version { java.bytecode })

    withSourcesJar()
}

kotlin {
    // Require explicit visibility & return types.
    explicitApi()

    // JDK version used by compiler & tooling.
    jvmToolchain(version { java.toolchain } .toInt())

    compilerOptions {
        // Target version of the generated JVM bytecode.
        jvmTarget = JvmTarget.fromTarget(target = version { java.bytecode })

        // Free compiler args
        freeCompilerArgs.addAll(
            "-Xcontext-parameters",
            "-Xcontext-sensitive-resolution",
            "-Xannotation-target-all",
            "-Xannotation-default-target=param-property",
            "-Xnested-type-aliases",

            "-opt-in=kotlin.experimental.ExperimentalTypeInference",
            "-opt-in=kotlin.contracts.ExperimentalContracts",
        )

        // Enable extra K2 warnings.
        extraWarnings = true
    }
}

sourceSets {
    val main by getting { kotlin.srcDirs("src/main/kotlin") }
    val test by getting { kotlin.srcDirs("src/test/kotlin") }
}

tasks.wrapper {
    gradleVersion = "latest"
    distributionType = Wrapper.DistributionType.ALL
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
    reports {
        junitXml.required = true
        html.required = true
    }

}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required = true
        html.required = true
        html.outputLocation = layout.buildDirectory.dir("reports/jacoco/html")
    }
}

jacoco {
    reportsDirectory = layout.buildDirectory.dir("reports/jacoco")
}

tasks.register<Jar>("dokkaJar") {
    from(tasks.dokkaGeneratePublicationHtml)
    dependsOn(tasks.dokkaGeneratePublicationHtml)
    archiveClassifier.set("javadoc")
}

publishing {
    publications {
        register<MavenPublication>(name = "outcome-maven-artifact") {
            from(components["kotlin"])
            groupId = version { project.group.id }
            artifactId = version { project.artifact.id }
            version = semver
        }
    }
    repositories {
        mavenLocal()

        // Publish to GitHub Packages
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/adjmunro/project-outcome")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }

        // TODO Publish to Sonatype OSSRH / Maven Central
//        maven {
//            name = "OSSRH"
//            url = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
//            credentials {
//                username = System.getenv("MAVEN_USERNAME")
//                password = System.getenv("MAVEN_PASSWORD")
//            }
//        }
    }
}

dependencies {
    implementation(platform(libs.kotlin.bom))
    implementation(libs.bundles.core)
    testImplementation(libs.bundles.test)
}
