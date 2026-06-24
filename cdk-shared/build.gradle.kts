plugins {
    id("io.micronaut.build.internal.cdk-module")
}
dependencies {
    implementation(mnPlatform.logback.classic)
    implementation(mnPlatform.slf4j.jul.to.slf4j)
}
