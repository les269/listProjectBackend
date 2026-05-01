plugins {
    java
    war
    kotlin("jvm") version "2.1.20"
    kotlin("plugin.spring") version "2.1.20"
    id("org.springframework.boot") version "4.0.5"
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
    implementation(libs.org.mapstruct.mapstruct)
    implementation(libs.org.mapstruct.mapstruct.jdk8)
    implementation(libs.org.hibernate.orm.hibernate.community.dialects)
    implementation(libs.org.jsoup.jsoup)
    implementation(libs.net.java.dev.jna.jna.platform)
    implementation(libs.com.google.guava.guava)
    implementation(libs.com.jayway.jsonpath.json.path)
    implementation("org.eclipse:yasson:3.0.4")

    compileOnly(libs.org.projectlombok.lombok)
    
    annotationProcessor(libs.org.projectlombok.lombok)
    annotationProcessor(libs.org.mapstruct.mapstruct.processor)
    annotationProcessor(libs.org.projectlombok.lombok.mapstruct.binding)
    
    testImplementation(libs.org.springframework.boot.spring.boot.starter.test)
}

group = "com.lsb"
version = "0.0.1-SNAPSHOT"
description = "listProjectBackend"
java.sourceCompatibility = JavaVersion.VERSION_21
java.targetCompatibility = JavaVersion.VERSION_21

springBoot {
    mainClass.set("com.lsb.listProjectBackend.ListProjectBackendApplication")
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootWar>("bootWar") {
    archiveFileName.set("${project.name}.war")
}

tasks.register<Copy>("copyWarToTomcat") {
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
