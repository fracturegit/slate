package net.mcbrawls.slate.screen

import net.kyori.adventure.text.Component
import net.mcbrawls.slate.InventorySlate
import net.mcbrawls.slate.MinestomSlatePlatform
import net.mcbrawls.slate.MinestomSlatePlayer
import net.mcbrawls.slate.Slate
import net.mcbrawls.slate.SlateInventoryType
import net.mcbrawls.slate.parseClickModifiers
import net.mcbrawls.slate.parseSlateClickType
import net.mcbrawls.slate.screen.slot.TileClickContext
import net.mcbrawls.slate.tile.HandledTileGrid
import net.mcbrawls.slate.tile.Tile
import net.mcbrawls.slate.toMinestom
import net.minestom.server.entity.Player
import net.minestom.server.inventory.Inventory
import net.minestom.server.inventory.click.Click
import net.minestom.server.item.ItemStack
import net.minestom.server.network.packet.server.play.SetPlayerInventorySlotPacket
import net.minestom.server.network.packet.server.play.SetSlotPacket

open class SlateInventory<T : Slate>(
    val slate: T,
    val openedPlayer: Player,
    type: SlateInventoryType,
    title: Component,
) : Inventory(type.toMinestom(), title) {
    fun tick(player: Player) {
        val slatePlayer = MinestomSlatePlayer(player)
        // Check dirty first (preserves existing tick order: sync before callbacks)
        if (slate.dirty) {
            update(player)
            slate.dirty = false
        }
        // Callbacks and layer ticks (may set dirty for the next tick)
        slate.onTick(slatePlayer)
    }

    fun onAnvilInput(player: Player, input: String) {
        slate.onAnvilInput(MinestomSlatePlayer(player), input)
    }

    override fun addViewer(player: Player): Boolean {
        // onOpen is called by MinestomSlatePlatform.openForPlayer, not here

        if (slate is InventorySlate) {
            if (!viewers.add(player)) return false
            update(player)
            return true
        }

        return super.addViewer(player)
    }

    override fun removeViewer(player: Player): Boolean {
        slate.onClosed(MinestomSlatePlayer(player))
        MinestomSlatePlatform.evict(slate)
        return super.removeViewer(player)
    }

    override fun handleClick(player: Player, click: Click): Boolean {
        val modifiers = parseClickModifiers(click)
        val clickType = parseSlateClickType(click)
        val rawSlot = click.slot()
        val mappedSlot = mapSlot(rawSlot)
        val tile = slate[mappedSlot]
        val context = TileClickContext(tile, clickType, modifiers, MinestomSlatePlayer(player), rawSlot != -999)
        slate.onSlotClicked(context)
        return false
    }

    fun mapSlot(slot: Int): Int {
        if (slot == -999) return slot

        val lastContainerSlot = slate.tiles.baseSize - 1

        if (slot <= lastContainerSlot) {
            return slot
        }

        if (slot <= lastContainerSlot + 9) {
            return slot + HandledTileGrid.INVENTORY_SIZE
        }

        return slot - HandledTileGrid.HOTBAR_SIZE
    }

    override fun update(player: Player) {
        for (index in 0 until slate.tiles.getFullSize()) {
            val tile = slate[index]
            val stack = MinestomSlatePlatform.renderTile(tile ?: Tile(), slate, player)
            sendSlot(player, index, stack)
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
