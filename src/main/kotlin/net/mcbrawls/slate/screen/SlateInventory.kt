package net.mcbrawls.slate.screen

import net.kyori.adventure.text.Component
import net.mcbrawls.slate.InventorySlate
import net.mcbrawls.slate.Slate
import net.mcbrawls.slate.screen.slot.ClickModifier
import net.mcbrawls.slate.screen.slot.SlateClickType
import net.mcbrawls.slate.screen.slot.TileClickContext
import net.mcbrawls.slate.tile.HandledTileGrid.Companion.HOTBAR_SIZE
import net.mcbrawls.slate.tile.HandledTileGrid.Companion.INVENTORY_SIZE
import net.minestom.server.entity.Player
import net.minestom.server.inventory.Inventory
import net.minestom.server.inventory.InventoryType
import net.minestom.server.inventory.click.Click
import net.minestom.server.item.ItemStack
import net.minestom.server.network.packet.server.play.SetPlayerInventorySlotPacket
import net.minestom.server.network.packet.server.play.SetSlotPacket

open class SlateInventory<T : Slate>(
    val slate: T,
    val openedPlayer: Player,
    type: InventoryType,
    title: Component,
) : Inventory(type, title) {
    fun tick(player: Player) {
        slate.onTick(player)
    }

    fun onAnvilInput(player: Player, input: String) {
        slate.onAnvilInput(player, input)
    }

    override fun addViewer(player: Player): Boolean {
        slate.onOpen(player, this)

        if (slate is InventorySlate) {
            if (!viewers.add(player)) return false
            update(player)
            return true
        }

        return super.addViewer(player)
    }

    override fun removeViewer(player: Player): Boolean {
        slate.onClosed(player)
        return super.removeViewer(player)
    }

    override fun handleClick(player: Player, click: Click): Boolean {
        val modifiers = ClickModifier.parse(click)
        val clickType = SlateClickType.parse(click)
        val rawSlot = click.slot()
        val mappedSlot = mapSlot(rawSlot)
        val tile = slate[mappedSlot]
        val context = TileClickContext(tile, click, clickType, modifiers, player, rawSlot != -999)
        slate.onSlotClicked(context)

        if (slate is InventorySlate) {
            sendSlot(player, rawSlot, tile?.createDisplayedStack(slate, player) ?: ItemStack.AIR)
        }

        return false
    }

    fun mapSlot(slot: Int): Int {
        if (slot == -999) return slot

        val lastContainerSlot = slate.tiles.baseSize - 1

        if (slot <= lastContainerSlot) {
            return slot
        }

        if (slot <= lastContainerSlot + 9) {
            return slot + INVENTORY_SIZE
        }

        return slot - HOTBAR_SIZE
    }

    override fun update(player: Player) {
        for (index in 0 until slate.tiles.getFullSize()) {
            val tile = slate[index]
            val stack = tile?.createDisplayedStack(slate, player)
            sendSlot(player, index, stack ?: ItemStack.AIR)
        }
    }

    private fun sendSlot(player: Player, index: Int, stack: ItemStack) {
        if (slate is InventorySlate) {
            player.sendPacket(SetPlayerInventorySlotPacket(index, stack))
            return
        }

        player.sendPacket(SetSlotPacket(windowId.toInt(), 0, index.toShort(), stack))
    }
}
