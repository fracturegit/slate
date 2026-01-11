package net.mcbrawls.slate.tile

import net.mcbrawls.slate.Slate
import net.mcbrawls.slate.screen.slot.SlateClickType
import net.minestom.server.entity.Player
import net.minestom.server.item.ItemStack

class RedirectedTile(
    val parent: Tile,
    val type: RedirectType,
) : Tile() {
    override fun collectClickCallbacks(clickType: SlateClickType): TileClickCallback {
        return parent.collectClickCallbacks(clickType)
    }

    override fun createDisplayedStack(slate: Slate, player: Player): ItemStack {
        return when (type) {
            RedirectType.NORMAL -> parent.createDisplayedStack(slate, player)
            RedirectType.INVISIBLE -> ItemStack.AIR
        }
    }
}
