rootProject.name = "mqts"
include("core")
include("springboot")

include("extensions-kafka-starter")
include("extensions-discovery-ack-starter")
include("extensions-mybatis-starter")
include("telemetry-starter")
include("all-starter")
include("dummy-server")


pluginManagement {

    repositories {
        mavenLocal()

        gradlePluginPortal()
    }
}
