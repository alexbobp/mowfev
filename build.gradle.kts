import org.jetbrains.kotlin.gradle.dsl.KotlinJsOptions
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilationToRunnableFiles

buildscript {
    repositories {
        jcenter()
    }
}

plugins {
    id("kotlin-multiplatform") version "1.3.31"
}
repositories {
    jcenter()
    maven(url = "https://dl.bintray.com/kotlin/ktor")
    mavenCentral()
}
val ktor_version = "1.1.3"
val logback_version = "1.2.3"

kotlin {
    jvm()
    js() {
        compilations.all {
            kotlinOptions {
                languageVersion = "1.3"
                moduleKind = "umd"
                sourceMap = true
                metaInfo = true
            }
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
                implementation("io.ktor:ktor-server-netty:$ktor_version")
                implementation("io.ktor:ktor-html-builder:$ktor_version")
                implementation("ch.qos.logback:logback-classic:$logback_version")
                api(project("dbsystem"))
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
            }
        }
        val jsMain by getting {
            dependencies {
                implementation(kotlin("stdlib-js"))
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}

val webFolder = project.buildDir.resolve("../src/jsMain/web")
val jsCompilations = kotlin.targets["js"].compilations

val jsMainClasses = tasks.findByName("jsMainClasses")

val populateWebFolder by tasks.creating {
    dependsOn(jsMainClasses)
    doLast {
        copy {
            from(jsCompilations["main"].output)
            from(kotlin.sourceSets.getByName("jsMain").resources.srcDirs)
            val test = jsCompilations["test"] as KotlinCompilationToRunnableFiles<KotlinJsOptions>
            test.runtimeDependencyFiles.forEach {
                if (it.exists() && !it.isDirectory()) {
                    from(zipTree(it.absolutePath).matching { include("*.js") })
                }
            }
            into(webFolder)
        }
    }
}

val jsJar by tasks.getting {
    dependsOn(populateWebFolder)
}

val jvmMainClasses by tasks.getting {}

val run = tasks.create<JavaExec>("run") {
    dependsOn(jvmMainClasses)
    dependsOn(jsJar)
    main = "sample.SampleJvmKt"
    classpath(
        kotlin.targets["jvm"].compilations["main"].output.allOutputs.files,
        configurations["jvmRuntimeClasspath"]
    )
    args = mutableListOf()
}
