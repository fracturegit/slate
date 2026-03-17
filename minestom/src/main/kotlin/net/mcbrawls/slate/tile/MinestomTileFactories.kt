package net.mcbrawls.slate.tile

import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material

/**
 * Builds a tile with an item stack.
 */
fun tile(stack: ItemStack = ItemStack.AIR, builder: StackTile.() -> Unit = {}): StackTile {
    return Tile.tile({ StackTile(stack) }, builder)
}

/**
 * Builds a tile with a material.
 */
fun tile(item: Material, builder: StackTile.() -> Unit = {}): StackTile {
    return tile(ItemStack.builder(item).build(), builder)
}
