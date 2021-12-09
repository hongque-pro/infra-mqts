plugins {
    id("com.labijie.infra") version Versions.infraPlugin
    id("org.springframework.boot") version "2.6.1" apply false
}

allprojects {
    group = "com.labijie.infra"
    version = "2.2.0"

    infra {
        useDefault {
            includeSource = true
            infraBomVersion = Versions.infraBom
            kotlinVersion = Versions.kotlin
            useMavenProxy = true
        }
    }

}
subprojects {
    if(!project.name.startsWith("dummy")){
        infra {
            usePublish {
                description = "A distributed transaction library based on message queues (Similar to TCC but simpler)"
                githubUrl("hongque-pro", "infra-mqts")
                artifactId {
                    "mqts-${it.name}"
                }
            }
        }
    }
}



