package net.mcbrawls.slate.tile

import net.mcbrawls.slate.SlateInventoryType

class HandledTileGrid(
    /**
     * The screen handler type sent to the client.
     * Affects what the client sees and how it communicates back to the server.
     */
    val inventoryType: SlateInventoryType,
) : TileGrid(inventoryType.width, inventoryType.height) {
    override fun getFullSize(): Int {
        return baseSize + FULL_INVENTORY_SIZE
    }

    /**
     * Sets a slot tile from the given coordinates, within the player inventory space.
     * @return the calculated index
     */
    fun setInventory(x: Int, y: Int, tile: Tile?): Int {
        val index = toIndex(x, y, INVENTORY_WIDTH) + baseSize
        set(index, tile)
        return index
    }

    /**
     * Gets a slot tile from the given coordinates.
     */
    fun getInventory(x: Int, y: Int): Tile? {
        val index = toIndex(x, y, INVENTORY_WIDTH) + baseSize
        return this[index]
    }

    /**
     * Asserts that this tile grid's width matches the default player inventory width.
     * Used to prevent usage of coordinates in non-matching tile grids.
     *
     * @throws IllegalStateException if the width does not match that of the player inventory
     */
    private fun assertWidthMatchesInventory() {
        if (width != INVENTORY_WIDTH) {
            throw IllegalStateException("Width is not consistent throughout tile grid")
        }
    }

    companion object {
        const val INVENTORY_WIDTH = 9
        const val INVENTORY_HEIGHT = 3

        const val INVENTORY_SIZE = INVENTORY_WIDTH * INVENTORY_HEIGHT

        const val HOTBAR_SIZE = INVENTORY_WIDTH
        const val FULL_INVENTORY_SIZE = INVENTORY_SIZE + HOTBAR_SIZE
    }
}
