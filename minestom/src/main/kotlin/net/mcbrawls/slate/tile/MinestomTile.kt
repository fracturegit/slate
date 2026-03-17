package net.mcbrawls.slate.tile

import net.mcbrawls.slate.Slate
import net.minestom.server.entity.Player
import net.minestom.server.item.ItemStack

/**
 * A tile that can produce a Minestom [ItemStack] for display.
 * Implement this on [Tile] subclasses that are used in Minestom contexts.
 */
interface MinestomTile {
    fun createBaseStack(slate: Slate, player: Player): ItemStack
}
