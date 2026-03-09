package net.mcbrawls.slate.tile

import net.minestom.server.inventory.InventoryType

open class TileGrid(val width: Int, val height: Int) {
    /**
     * The size of the slate WITHOUT the player's inventory.
     */
    val baseSize: Int = width * height

    /**
     * A fixed-size array of all tiles stored in this grid.
     */
    val tiles: Array<Tile?> = arrayOfNulls(getFullSize())

    /**
     * Redirects a tile index to another tile index.
     */
    val redirects: MutableMap<Int, Pair<Int, RedirectType>> = mutableMapOf()

    /**
     * The last available tile slot index.
     */
    val lastIndex: Int get() = tiles.lastIndex

    /**
     * The start of the hotbar slot indexes.
     */
    val hotbarStartIndex: Int get() = lastIndex - 8

    open fun getFullSize(): Int {
        return baseSize
    }

    /**
     * Sets a slot tile at the given index.
     */
    operator fun set(index: Int, tile: Tile?): Boolean {
        if (checkSlotIndex(index)) {
            if (tile != tiles[index]) {
                tiles[index] = tile
                return true
            }
        }

        return false
    }

    /**
     * Sets a slot tile from the given coordinates.
     * @return the calculated index
     */
    operator fun set(x: Int, y: Int, tile: Tile?): Int {
        val index = indexAt(x, y)
        set(index, tile)
        return index
    }

    /**
     * Sets a slot tile within the player's hotbar.
     * @return the calculated index
     */
    fun setHotbar(index: Int, tile: Tile?): Int {
        val hotbarIndex = hotbarStartIndex + index
        set(hotbarIndex, tile)
        return hotbarIndex
    }

    /**
     * Gets a slot tile from the given index.
     */
    operator fun get(index: Int): Tile? {
        redirects[index]?.also { (trueIndex, type) ->
            tiles.getOrNull(trueIndex)?.also { tile ->
                return RedirectedTile(tile, type)
            }
        }

        return tiles.getOrNull(index)
    }

    /**
     * Gets a slot tile from the given coordinates.
     */
    operator fun get(x: Int, y: Int): Tile? {
        return this[indexAt(x, y)]
    }

    /**
     * Gets a slot tile from the hotbar.
     */
    fun getHotbar(index: Int): Tile? {
        val hotbarIndex = hotbarStartIndex + index
        return this[hotbarIndex]
    }

    /**
     * Clears a slot tile from the given index.
     */
    fun clear(index: Int): Boolean {
        return set(index, null)
    }

    /**
     * Clears all tile slots.
     */
    fun clear() {
        return tiles.fill(null)
    }

    /**
     * Sets a redirect on this tile grid.
     */
    fun redirect(index: Int, otherIndex: Int, type: RedirectType = RedirectType.NORMAL) {
        redirects[index] = otherIndex to type
    }

    fun forEach(action: (Int, Tile?) -> Unit) {
        tiles.toList().forEachIndexed(action)
    }

    /**
     * Verifies that an index is within the slot size bounds.
     */
    private fun checkSlotIndex(index: Int): Boolean {
        return index <= lastIndex
    }

    fun indexAt(x: Int, y: Int): Int {
        return toIndex(x, y, width)
    }

    companion object {
        /**
         * Converts coordinates to a tile grid index.
         */
        fun toIndex(x: Int, y: Int, width: Int): Int {
            return y * width + x
        }

        /**
         * Creates a tile grid from the dimensions of the given screen handler type.
         */
        fun create(type: InventoryType): HandledTileGrid {
            return HandledTileGrid(type)
        }

        /**
         * The width of this screen handler type.
         */
        val InventoryType.width: Int get() {
            return when (this) {
                InventoryType.CRAFTING -> 2
                InventoryType.SMITHING -> 4
                InventoryType.WINDOW_3X3 -> 3
                InventoryType.HOPPER -> 5
                InventoryType.BREWING_STAND -> 1
                InventoryType.ENCHANTMENT -> 1
                InventoryType.STONE_CUTTER -> 1
                InventoryType.BEACON -> 1
                InventoryType.BLAST_FURNACE -> 1
                InventoryType.FURNACE -> 1
                InventoryType.SMOKER -> 1
                InventoryType.ANVIL -> 1
                InventoryType.GRINDSTONE -> 1
                InventoryType.MERCHANT -> 1
                InventoryType.CARTOGRAPHY -> 1
                InventoryType.LOOM -> 1
                else -> 9
            }
        }

        /**
         * The height of this screen handler type.
         */
        val InventoryType.height: Int get() {
            return when (this) {
                InventoryType.CHEST_6_ROW -> 6
                InventoryType.CRAFTING -> 6
                InventoryType.CHEST_5_ROW -> 5
                InventoryType.CHEST_4_ROW -> 4
                InventoryType.CHEST_3_ROW -> 3
                InventoryType.CHEST_2_ROW -> 2
                InventoryType.ENCHANTMENT -> 2
                InventoryType.STONE_CUTTER -> 2
                InventoryType.CHEST_1_ROW -> 1
                InventoryType.BEACON -> 1
                InventoryType.HOPPER -> 1
                InventoryType.BREWING_STAND -> 1
                InventoryType.SMITHING -> 1
                else -> 3
            }
        }
    }
}
