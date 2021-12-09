
dependencies {
    implementation (project(":all-starter")) {
        exclude(module = "extensions-kafka-starter")
    }
    //implementation(project(":all-starter"))
    implementation("mysql:mysql-connector-java")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
}