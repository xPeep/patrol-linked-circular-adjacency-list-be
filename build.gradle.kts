import com.sun.net.httpserver.SimpleFileServer
import java.net.InetSocketAddress

plugins {
	kotlin("jvm") version "2.3.21"
	kotlin("plugin.spring") version "2.3.21"
	id("org.springframework.boot") version "4.1.1"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "cz.csas.datastructures"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("tools.jackson.module:jackson-module-kotlin")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.springframework.boot:spring-boot-restclient")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("org.junit.jupiter:junit-jupiter-params")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
	testLogging {
		events("passed", "skipped", "failed")
		showStandardStreams = false
	}
}

// The delivered frontend is a single static file. It is served on a different port
// than the API on purpose, so the CORS configuration of the server really gets exercised.
// Every candidate port below is already listed in application.yaml under
// patrol.cors.allowed-origins, so whichever one is free will work.
val frontendDir = layout.projectDirectory.dir("frontend").asFile.toPath()
val frontendPorts: List<Int> = (findProperty("frontendPort") as String?)
	?.let { listOf(it.toInt()) }
	?: listOf(5500, 4200, 8081, 5555)

tasks.register("frontend") {
	group = "application"
	description = "Serves the delivered frontend on the first free port of $frontendPorts"
	doLast {
		var lastError: Exception? = null
		val server = frontendPorts.firstNotNullOfOrNull { port ->
			try {
				SimpleFileServer.createFileServer(
					InetSocketAddress(port),
					frontendDir,
					SimpleFileServer.OutputLevel.NONE,
				)
			} catch (failure: Exception) {
				lastError = failure
				logger.lifecycle("  port $port is already taken, trying the next one")
				null
			}
		} ?: throw GradleException(
			"None of the ports $frontendPorts is free. Free one of them, or run " +
				"gradlew frontend -PfrontendPort=<port> and add that origin to " +
				"patrol.cors.allowed-origins in application.yaml. Last error: " + lastError
		)

		server.start()
		val port = server.address.port
		logger.lifecycle("")
		logger.lifecycle("  Frontend runs on http://localhost:$port")
		logger.lifecycle("  The API is expected on http://localhost:8080 - start it with: gradlew bootRun")
		logger.lifecycle("  Stop with Ctrl+C")
		logger.lifecycle("")
		Thread.currentThread().join()
	}
}
