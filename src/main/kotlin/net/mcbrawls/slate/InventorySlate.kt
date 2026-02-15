package net.mcbrawls.slate

import net.mcbrawls.slate.tile.HandledTileGrid
import net.mcbrawls.slate.tile.TileGrid
import net.minestom.server.inventory.InventoryType
import org.jetbrains.annotations.ApiStatus

@ApiStatus.Experimental
class InventorySlate : Slate() {
    override var tiles: HandledTileGrid = TileGrid.create(InventoryType.CHEST_1_ROW)

    override var canPlayerClose: Boolean = false
    override var canBeClosed: Boolean = false
}
