plugins {
    id("io.micronaut.build.internal.cdk-module")
}
dependencies {
    implementation(mnLogging.logback.classic)
    implementation(mnLogging.slf4j.jul.to.slf4j)
}
