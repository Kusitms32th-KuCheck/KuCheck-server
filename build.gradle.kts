plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	kotlin("plugin.serialization") version "2.1.0"
	id("org.springframework.boot") version "3.5.6"
	id("io.spring.dependency-management") version "1.1.7"
	kotlin("plugin.jpa") version "1.9.25"
}

group = "onku"
version = "0.0.1-SNAPSHOT"
description = "Demo project for Spring Boot"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	runtimeOnly("com.mysql:mysql-connector-j")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	// swagger
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.7.0")
	// h2
	runtimeOnly("com.h2database:h2")
	// jwt
	implementation("io.jsonwebtoken:jjwt-api:0.12.5")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.5")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.5")
	// redis
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	//s3
	implementation("software.amazon.awssdk:s3:2.25.34")
	implementation("software.amazon.awssdk:auth:2.25.34")
	implementation("software.amazon.awssdk:regions:2.25.34")
	implementation("software.amazon.awssdk:url-connection-client:2.25.30")
	//serializable
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
	//okhttp3
	implementation("com.squareup.okhttp3:okhttp:4.12.0")
	//google auth
	implementation("com.google.auth:google-auth-library-oauth2-http:1.33.1")
	//test
	testImplementation("io.mockk:mockk:1.13.5")
	//aws secretmanager
	implementation(platform("io.awspring.cloud:spring-cloud-aws-dependencies:3.1.0"))
	implementation("io.awspring.cloud:spring-cloud-aws-starter-secrets-manager")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

allOpen {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.MappedSuperclass")
	annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
