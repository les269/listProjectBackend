plugins {
    java
    war
    kotlin("jvm") version "2.4.21"
    kotlin("plugin.spring") version "2.4.21"
    id("org.springframework.boot") version "4.1.1"
    id("io.spring.dependency-management") version "1.1.7"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.org.springframework.boot.spring.boot.starter.web)
    implementation(libs.org.springframework.boot.spring.boot.starter.jdbc)
    implementation(libs.org.springframework.boot.spring.boot.starter.data.jpa)
    implementation(libs.org.springframework.spring.expression)
    implementation(libs.org.xerial.sqlite.jdbc)
    implementation(libs.org.postgresql.postgresql)
    // Source: https://mvnrepository.com/artifact/org.mapstruct/mapstruct
    implementation(libs.org.mapstruct.mapstruct)
    implementation(libs.org.hibernate.orm.hibernate.community.dialects)
    // Source: https://mvnrepository.com/artifact/org.jsoup/jsoup
    implementation(libs.org.jsoup.jsoup)
    implementation(libs.net.java.dev.jna.jna.platform)
    implementation(libs.com.google.guava.guava)
    implementation(libs.com.jayway.jsonpath.json.path)
    // Source: https://mvnrepository.com/artifact/org.eclipse.yasson/yasson
    implementation(libs.org.eclipse.yasson.yasson)
    // Source: https://mvnrepository.com/artifact/com.github.houbb/opencc4j
    implementation(libs.com.github.houbb.opencc4j)
    // Source: https://mvnrepository.com/artifact/org.apache.fesod/fesod-sheet
    implementation(libs.org.apache.fesod.fesod.sheet)

    compileOnly(libs.org.projectlombok.lombok)
    
    annotationProcessor(libs.org.projectlombok.lombok)
    annotationProcessor(libs.org.mapstruct.mapstruct.processor)
    annotationProcessor(libs.org.projectlombok.lombok.mapstruct.binding)
    
    testImplementation(libs.org.springframework.boot.spring.boot.starter.test)
}

group = "com.lsb"
version = "0.0.1-SNAPSHOT"
description = "listProjectBackend"
java.sourceCompatibility = JavaVersion.VERSION_27
java.targetCompatibility = JavaVersion.VERSION_27

springBoot {
    mainClass.set("com.lsb.listProjectBackend.ListProjectBackendApplication")
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootWar>("bootWar") {
    archiveFileName.set("${project.name}.war")
}

tasks.register<Copy>("copyWarToTomcat") {
    description = "將生成的 WAR 檔案複製到 Tomcat 的 webapps 目錄"
    dependsOn(tasks.named("bootWar"))
    from(layout.buildDirectory.file("libs/${project.name}.war"))
    into("C:/code/apache-tomcat/webapps")
}

tasks.named("build") {
    finalizedBy("copyWarToTomcat")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc>() {
    options.encoding = "UTF-8"
}

val deployDir = "C:/code/listProjectBackend-tomcat"
val serviceExePath = "$deployDir/listProjectBackend-service.exe"

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    archiveFileName.set("listProjectBackend.jar") // 固定產出的檔名
    finalizedBy("copyJarToDeploy")
}

tasks.register<Exec>("stopServiceForDeploy") {
    description = "停止 Windows 服務"
    commandLine(serviceExePath, "stop")
    isIgnoreExitValue = true
    onlyIf { System.getProperty("os.name").startsWith("Windows", ignoreCase = true) }
}

tasks.register<Exec>("startServiceAfterDeploy") {
    description = "啟動 Windows 服務"
    commandLine(serviceExePath, "start")
    onlyIf { System.getProperty("os.name").startsWith("Windows", ignoreCase = true) }
}

tasks.register<Copy>("copyJarToDeploy") {
    description = "將新的 JAR 檔案複製到 Windows 部署目錄，前提是已經停止了服務"
    dependsOn("stopServiceForDeploy")
    from(tasks.bootJar.get().archiveFile)
    into(deployDir) // 指向你的 Windows 部署目錄
    finalizedBy("startServiceAfterDeploy")
}