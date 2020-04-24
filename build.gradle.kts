import org.jetbrains.intellij.tasks.PatchPluginXmlTask
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val isCI = !System.getenv("CI").isNullOrBlank()
val commitHash = Runtime.getRuntime().exec("git rev-parse --short HEAD").run {
	waitFor()
	val output = inputStream.use { inputStream.use { it.readBytes().let(::String) } }
	destroy()
	output.trim()
}

val pluginComingVersion = "1.1"
val pluginVersion = if (isCI) "$pluginComingVersion-$commitHash" else pluginComingVersion
val packageName = "org.ice1000.bililive.danmaku"

group = packageName
version = pluginVersion

plugins {
	java
	id("org.jetbrains.intellij") version "0.4.18"
	kotlin("jvm") version "1.3.72"
}

fun fromToolbox(root: String, ide: String) = file(root)
	.resolve(ide)
	.takeIf { it.exists() }
	?.resolve("ch-0")
	?.listFiles()
	.orEmpty()
	.filterNotNull()
	.filter { it.isDirectory }
	.filterNot { it.name.endsWith(".plugins") }
	.maxBy {
		val (major, minor, patch) = it.name.split('.')
		String.format("%5s%5s%5s", major, minor, patch)
	}
	?.also { println("Picked: $it") }

intellij {
	updateSinceUntilBuild = false
	instrumentCode = true
	val user = System.getProperty("user.name")
	val os = System.getProperty("os.name")
	val root = when {
		os.startsWith("Windows") -> "C:\\Users\\$user\\AppData\\Local\\JetBrains\\Toolbox\\apps"
		os == "Linux" -> "/home/$user/.local/share/JetBrains/Toolbox/apps"
		else -> return@intellij
	}
	val intellijPath = sequenceOf("IDEA-C", "IDEA-U")
		.mapNotNull { fromToolbox(root, it) }.firstOrNull()
	intellijPath?.absolutePath?.let { localPath = it }
	val pycharmPath = sequenceOf("IDEA-C", "PyCharm-C", "IDEA-U")
		.mapNotNull { fromToolbox(root, it) }.firstOrNull()
	pycharmPath?.absolutePath?.let { alternativeIdePath = it }

	if (isCI) version = "2020.1"
	setPlugins("java")
}

java {
	sourceCompatibility = JavaVersion.VERSION_11
	targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<PatchPluginXmlTask> {
	changeNotes(file("res/META-INF/change-notes.html").readText())
	pluginDescription(file("res/META-INF/description.html").readText())
	version(pluginVersion)
	pluginId(packageName)
}

sourceSets {
	main {
		withConvention(KotlinSourceSet::class) {
			listOf(java, kotlin).forEach { it.srcDirs("src") }
		}
		resources.srcDirs("res")
	}

	test {
		withConvention(KotlinSourceSet::class) {
			listOf(java, kotlin).forEach { it.srcDirs("test") }
		}
		resources.srcDirs("testData")
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation(kotlin("stdlib-jdk8"))
	implementation("org.eclipse.mylyn.github", "org.eclipse.egit.github.core", "2.1.5") {
		exclude(module = "gson")
	}
	implementation(group = "com.github.cqjjjzr", name = "charlie.bililivelib", version = "rv5") {
		exclude(module = "kotlin-stdlib")
		exclude(module = "gson")
		exclude(module = "httpclient")
	}
}

task("displayCommitHash") {
	group = "help"
	description = "Display the newest commit hash"
	doFirst { println("Commit hash: $commitHash") }
}

task("isCI") {
	group = "help"
	description = "Check if it's running in a continuous-integration"
	doFirst { println(if (isCI) "Yes, I'm on a CI." else "No, I'm not on CI.") }
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		jvmTarget = "1.8"
		languageVersion = "1.3"
		apiVersion = "1.3"
		freeCompilerArgs = listOf("-Xjvm-default=enable")
	}
}
