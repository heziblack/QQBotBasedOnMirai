plugins {
    val kotlinVersion = "1.8.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.16.0"
}

group = "org.hezistudio"
version = "0.1.3"

repositories {
    maven("https://maven.aliyun.com/repository/public")
    mavenCentral()
}
val exposedVersion:String = "0.44.1"
dependencies{
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-crypt:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-json:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-money:$exposedVersion")
//    implementation("org.jetbrains.exposed:exposed-spring-boot-starter:$exposedVersion")
    // https://mvnrepository.com/artifact/org.xerial/sqlite-jdbc
    implementation("org.xerial:sqlite-jdbc:3.42.0.0")

    // https://mvnrepository.com/artifact/org.apache.poi/poi
//    implementation("org.apache.poi:poi:3.9")
    implementation("org.apache.poi:poi-ooxml:4.1.1")
    implementation("com.squareup.okhttp3:okhttp:4.9.0")
    // https://mvnrepository.com/artifact/com.google.code.gson/gson
    implementation("com.google.code.gson:gson:2.8.9")

}