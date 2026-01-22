@file:Suppress("PropertyName")

val minestom_version: String by properties

dependencies {
    implementation("net.minestom:minestom:$minestom_version")
    api("net.kyori:adventure-api:4.25.0")
    api("com.noxcrew.noxesium:api:3.0.0")
}
