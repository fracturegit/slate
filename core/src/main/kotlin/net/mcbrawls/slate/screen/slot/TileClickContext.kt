package net.mcbrawls.slate.screen.slot

import net.mcbrawls.slate.SlatePlayer
import net.mcbrawls.slate.tile.Tile

data class TileClickContext(
    /**
     * The clicked slot.
     */
    val tile: Tile?,

    /**
     * The interpreted base button click type.
     */
    val clickType: SlateClickType,

    /**
     * The interpreted modifiers of the click.
     */
    val modifiers: Collection<ClickModifier>,

    /**
     * The player who clicked the slot.
     */
    val player: SlatePlayer,

    /**
     * Whether this click took place within a screen.
     * False if an inventory slate.
     */
    val withinScreen: Boolean,
)
