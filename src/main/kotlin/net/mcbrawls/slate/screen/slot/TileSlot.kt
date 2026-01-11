package net.mcbrawls.slate.screen.slot

import net.mcbrawls.slate.screen.SlateInventory
import net.minestom.server.item.ItemStack

class TileSlot(
    val handler: SlateInventory<*>,
    val index: Int,
    val x: Int,
    val y: Int
) {
    fun createStack(): ItemStack {
        val slate = handler.slate
        val player = handler.openedPlayer

        val tile = slate[index]
        return tile?.createDisplayedStack(slate, player) ?: ItemStack.AIR
    }
}
