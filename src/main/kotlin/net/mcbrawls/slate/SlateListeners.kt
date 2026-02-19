package net.mcbrawls.slate

import net.mcbrawls.slate.Slate.Companion.slate
import net.mcbrawls.slate.screen.SlateInventory
import net.mcbrawls.slate.screen.slot.ClickModifier
import net.mcbrawls.slate.screen.slot.SlateClickType
import net.mcbrawls.slate.screen.slot.TileClickContext
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player
import net.minestom.server.entity.PlayerHand
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.event.player.PlayerAnvilInputEvent
import net.minestom.server.event.player.PlayerHandAnimationEvent
import net.minestom.server.event.player.PlayerPacketEvent
import net.minestom.server.event.player.PlayerSpawnEvent
import net.minestom.server.event.player.PlayerTickEvent
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.instance.block.Block
import net.minestom.server.inventory.click.Click
import net.minestom.server.item.ItemStack
import net.minestom.server.network.packet.client.play.ClientClickWindowPacket
import net.minestom.server.network.packet.client.play.ClientCloseWindowPacket
import net.minestom.server.network.packet.client.play.ClientPlayerActionPacket
import net.minestom.server.network.packet.server.play.SetCursorItemPacket
import net.minestom.server.utils.inventory.PlayerInventoryUtils

object SlateListeners {
    fun initialize() {
        MinecraftServer.getGlobalEventHandler().let { events ->
            events.addListener(PlayerHandAnimationEvent::class.java) { event ->
                val player = event.player
                player.openInventory?.handleClick(player, Click.Left(player.heldSlot.toInt()))
            }

            events.addListener(PlayerUseItemEvent::class.java) { event ->
                val player = event.player
                if (player.slate is InventorySlate) {
                    val slot = if (event.hand == PlayerHand.OFF) PlayerInventoryUtils.OFFHAND_SLOT else player.heldSlot.toInt()
                    player.openInventory?.handleClick(player, Click.Right(slot))
                }
            }

            events.addListener(PlayerPacketEvent::class.java) { event ->
                val packet = event.packet
                val player = event.player
                if (player.slate is InventorySlate) {
                    when (packet) {
                        is ClientCloseWindowPacket -> {
                            event.isCancelled = true
                        }

                        is ClientClickWindowPacket -> {
                            val inventory = player.openInventory
                            if (inventory is SlateInventory<*>) {
                                player.sendPacket(SetCursorItemPacket(ItemStack.AIR))
                                inventory.update()

                                player.clickPreprocessor.processClick(packet, inventory.size)?.let { click ->
                                    inventory.handleClick(player, click)
                                }
                            }

                            event.isCancelled = true
                        }

                        is ClientPlayerActionPacket -> {
                            val status = packet.status
                            if (status == ClientPlayerActionPacket.Status.DROP_ITEM || status == ClientPlayerActionPacket.Status.DROP_ITEM_STACK) {
                                player.openInventory?.update()
                                event.isCancelled = true
                            }
                        }
                    }
                }
            }
        }
    }
}
