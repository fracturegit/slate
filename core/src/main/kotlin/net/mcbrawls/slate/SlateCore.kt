package net.mcbrawls.slate

/**
 * Global registry for the active [SlatePlatform].
 * Set this once at server startup before opening any slates.
 */
object SlateCore {
    var platform: SlatePlatform? = null
}
