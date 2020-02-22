

plugins {
    kotlin("jvm") version "1.3.61"
}

group = "com.jetbrains"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}


dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.amazonaws:aws-lambda-java-core:1.2.0")
    implementation(platform("com.amazonaws:aws-java-sdk-bom:1.11.228"))
    implementation("com.amazonaws:aws-java-sdk-s3")
    implementation("com.amazonaws:aws-lambda-java-log4j2:1.0.0")

    compile("org.apache.logging.log4j:log4j-core:2.12.1")
    compile("org.apache.logging.log4j:log4j-api:2.12.1")


    testCompile("junit:junit:4.12")
    testCompile("io.mockk:mockk:1.9")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    val buildZip by creating(Zip::class) {
        from(compileKotlin)
        from(processResources)
        into("lib") {
            from(configurations.runtimeClasspath)
        }
    }

    build {
        dependsOn(buildZip)
    }
}