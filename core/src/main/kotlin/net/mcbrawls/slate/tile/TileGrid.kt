package net.mcbrawls.slate.tile

import net.mcbrawls.slate.SlateInventoryType

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
         * Creates a tile grid from the dimensions of the given inventory type.
         */
        fun create(type: SlateInventoryType): HandledTileGrid {
            return HandledTileGrid(type)
        }
    }
}
