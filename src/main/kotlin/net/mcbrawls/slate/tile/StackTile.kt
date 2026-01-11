package net.mcbrawls.slate.tile

import net.mcbrawls.slate.Slate
import net.minestom.server.entity.Player
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material

class StackTile(var stack: ItemStack = ItemStack.AIR) : Tile() {
    constructor(item: Material) : this(ItemStack.builder(item).build())

    override fun createBaseStack(slate: Slate, player: Player): ItemStack {
        return stack
    }

    override fun toString(): String {
        val stackStr = stack.toString()
        return "StackTile{$stackStr}"
    }

    companion object {
        val EMPTY = StackTile()
    }
}
