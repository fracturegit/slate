package net.mcbrawls.slate

/**
 * Platform-agnostic wrapper around a native player instance.
 * Use [native] to access the platform-specific player object.
 */
interface SlatePlayer {
    val native: Any
}

/**
 * Returns the slate currently open for this player, or null.
 */
val SlatePlayer.slate: Slate? get() = SlateCore.platform?.getOpenSlate(this)
