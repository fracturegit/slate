@file:Suppress("PropertyName")

val minestom_version: String by properties

dependencies {
    implementation("net.minestom:minestom:$minestom_version")
    api("net.kyori:adventure-api:4.25.0")
}
