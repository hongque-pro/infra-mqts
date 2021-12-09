

infra {
    useMybatis(
        configFile = file("src/main/mybatis/mybatis-generator-config.xml").absolutePath,
        propertiesFile = file("src/main/mybatis/mybatis-generator.properties").absolutePath
    )
}

dependencies {
    api("com.labijie.infra:commons-mybatis-dynamic-starter:${Versions.infraCommons}")
    api(project(":springboot"))
}