package net.mcbrawls.slate

import net.kyori.adventure.text.Component

/**
 * Platform bridge for Slate. A Slate instance maps 1:1 to a single viewer at any time.
 * Register via [SlateCore.platform] before opening any slates.
 */
interface SlatePlatform {
    /**
     * Wraps a native player instance in a [SlatePlayer].
     */
    fun wrapPlayer(native: Any): SlatePlayer

    /**
     * Opens the slate for the given player.
     * Implementations must call [Slate.onOpen] after the inventory is shown.
     * @return whether the slate was opened successfully
     */
    fun openForPlayer(slate: Slate, player: SlatePlayer): Boolean

    /**
     * Closes the slate for the given player.
     * @return whether the slate was closed
     */
    fun closeForPlayer(slate: Slate, player: SlatePlayer): Boolean

    /**
     * Updates the screen title for the player currently viewing this slate.
     * No-op if the slate is not open.
     */
    fun updateScreenTitle(slate: Slate, title: Component)

    /**
     * Returns the slate currently open for the given player, or null.
     */
    fun getOpenSlate(player: SlatePlayer): Slate?
}
