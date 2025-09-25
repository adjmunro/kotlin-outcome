import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.dokka)
    jacoco
    `maven-publish`
}

data class ProjectInfo(
    val groupId: String = libs.versions.project.group.id.get(),
    val artifactId: String = libs.versions.project.artifact.id.get(),
    val moduleId: String? = null,
    val major: String = libs.versions.project.version.major.get(),
    val minor: String = libs.versions.project.version.minor.get(),
    val patch: String = libs.versions.project.version.patch.get(),
    val javaToolchain: String = libs.versions.java.toolchain.get(),
    val javaBytecode: String = libs.versions.java.bytecode.get(),
    val androidCompileSdk: Int = libs.versions.project.android.compileSdk.get().toInt(),
    val androidMinSdk: Int = libs.versions.project.android.minSdk.get().toInt(),
) {
    val androidNamespace: String by lazy {
        moduleId?.let { "${groupId}.${artifactId}.$it" } ?: "${groupId}.${artifactId}"
    }
    val moduleArtifactId: String by lazy {
        moduleId?.let { "${artifactId}-$it" } ?: artifactId
    }
    val semanticVersion: String by lazy {
        "$major.$minor.$patch"
    }
}

val info = ProjectInfo()
group = info.groupId
version = info.semanticVersion

java {
    sourceCompatibility = JavaVersion.toVersion(/* value = */ info.javaToolchain)
    targetCompatibility = JavaVersion.toVersion(/* value = */ info.javaBytecode)
    withSourcesJar()
}

kotlin {
    // Require explicit visibility & return types.
    explicitApi()

    // JDK version used by compiler & tooling.
    jvmToolchain(jdkVersion = info.javaToolchain.toInt())

    compilerOptions {
        // Target version of the generated JVM bytecode.
        jvmTarget = JvmTarget.fromTarget(target = info.javaBytecode)

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
    getting { kotlin.srcDirs("src/main/kotlin") }
    getting { kotlin.srcDirs("src/test/kotlin") }
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
    dependsOn(tasks.dokkaGeneratePublicationHtml)
    from(tasks.dokkaGeneratePublicationHtml)
    archiveClassifier.set("javadoc")
}

publishing {
    publications {
        register<MavenPublication>(name = "${info.moduleArtifactId}-maven-artifact") {
            from(/* component = */ components["kotlin"])
            groupId = info.groupId
            artifactId = info.moduleArtifactId
            version = info.semanticVersion
        }
    }
    repositories {
        mavenLocal()

        // Publish to GitHub Packages
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/adjmunro/kotlin-outcome")
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
