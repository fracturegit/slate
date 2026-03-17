package net.mcbrawls.slate.tile

import net.mcbrawls.slate.screen.slot.SlateClickType

class RedirectedTile(
    val parent: Tile,
    val type: RedirectType,
) : Tile() {
    override fun collectClickCallbacks(clickType: SlateClickType): TileClickCallback {
        return parent.collectClickCallbacks(clickType)
    }
}
