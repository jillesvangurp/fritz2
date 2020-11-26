plugins {
    kotlin("multiplatform")
    id("maven-publish")
    id("org.jetbrains.dokka") version "0.10.1"
}

//FIXME: move to parent
val coroutines_version = "1.3.9"

repositories {
    maven(url = "https://kotlin.bintray.com/kotlinx/") // soon will be just jcenter()
}

kotlin {


    jvm()
    js().browser {
        testTask {
            useKarma {
//                useSafari()
//                useFirefox()
//                useChrome()
                useChromeHeadless()
//                usePhantomJS()
            }
            //running test-server in background
//            dependsOn(":test-server:start")
        }
    }
    sourceSets {
        all {
            languageSettings.apply {
                enableLanguageFeature("InlineClasses") // language feature name
                useExperimentalAnnotation("kotlin.ExperimentalUnsignedTypes") // annotation FQ-name
                useExperimentalAnnotation("kotlin.ExperimentalStdlibApi")
                useExperimentalAnnotation("kotlinx.coroutines.ExperimentalCoroutinesApi")
                useExperimentalAnnotation("kotlinx.coroutines.InternalCoroutinesApi")
                useExperimentalAnnotation("kotlinx.coroutines.FlowPreview")
            }
        }

        val commonMain by getting {
            dependencies {
                implementation(project(":core"))
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.1.0")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-common"))
                implementation(kotlin("test-junit"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:$coroutines_version")
                implementation(project(":styling"))
                implementation(project(":components"))
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.1.0")
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:$coroutines_version")
            }
        }
    }
}


publishing {
    repositories {
        maven {
            name = "bintray"
            val releaseUrl = "https://api.bintray.com/maven/jwstegemann/fritz2/${project.name}/;" +
                    "publish=0;" + // Never auto-publish to allow override.
                    "override=1"
            val snapshotUrl = "https://oss.jfrog.org/artifactory/oss-snapshot-local"
            val isRelease = System.getenv("GITHUB_EVENT_NAME").equals("release", true)

            url = uri(if (isRelease && !version.toString().endsWith("SNAPSHOT")) releaseUrl else snapshotUrl)

            credentials {
                username = "jwstegemann"
                password = System.getenv("BINTRAY_API_KEY")
            }
        }
    }
}
