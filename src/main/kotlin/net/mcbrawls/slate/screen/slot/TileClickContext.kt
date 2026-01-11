package net.mcbrawls.slate.screen.slot

import net.mcbrawls.slate.tile.Tile
import net.minestom.server.entity.Player
import net.minestom.server.inventory.click.Click

data class TileClickContext(
    /**
     * The clicked slot.
     */
    val tile: Tile?,

    /**
     * The click object.
     */
    val click: Click,

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
    val player: Player,

    /**
     * Whether this click took place within a screen.
     * False if an inventory slate.
     */
    val withinScreen: Boolean,
)
