package net.mcbrawls.slate

import net.mcbrawls.slate.screen.SlateInventory
import net.mcbrawls.slate.screen.slot.ClickModifier
import net.mcbrawls.slate.screen.slot.SlateClickType
import net.mcbrawls.slate.screen.slot.TileClickContext
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import net.minestom.server.entity.PlayerHand
import net.minestom.server.event.player.PlayerAnvilInputEvent
import net.minestom.server.event.player.PlayerHandAnimationEvent
import net.minestom.server.event.player.PlayerTickEvent
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.inventory.click.Click

object SlateListeners {
    fun initialize() {
        val events = MinecraftServer.getGlobalEventHandler()

        events.addListener(PlayerUseItemEvent::class.java) { event ->
            if (!onUse(event.player, event.hand)) {
                // event.isCancelled = true
            }
        }

        events.addListener(PlayerHandAnimationEvent::class.java) { event ->
            if (!onSwing(event.player, event.hand)) {
                event.isCancelled = true
            }
        }

        events.addListener(PlayerTickEvent::class.java) { event ->
            val player = event.player
            val inventory = player.openInventory
            if (inventory is SlateInventory<*>) {
                inventory.tick(player)

                /*if (player.aliveTicks % 20 == 0L) {
                    println(inventory.slate.hashCode())
                }*/
            }
        }

        events.addListener(PlayerAnvilInputEvent::class.java) { event ->
            val player = event.player
            val inventory = event.inventory
            if (inventory is SlateInventory<*>) {
                inventory.onAnvilInput(player, event.input)
            }
        }
    }

    fun getClickModifiers(player: Player): Collection<ClickModifier> {
        return buildSet {
            if (player.isSneaking) {
                add(ClickModifier.SHIFT)
            }
        }
    }

    private fun interact(player: Player, hand: PlayerHand, clickFactory: (Int) -> Click): Boolean {
        if (hand != PlayerHand.MAIN) {
            return true
        }

        val slateInventory = player.openInventory
        if (slateInventory is SlateInventory<*>) {
            val slate = slateInventory.slate
            val click = clickFactory.invoke(player.heldSlot.toInt())
            val tile = slate.tiles[click.slot()]
            val context = TileClickContext(tile, click, SlateClickType.parse(click), getClickModifiers(player), player, true)
            slate.onSlotClicked(context)

            slateInventory.update(player)

            return false
        }

        return true
    }

    internal fun onUse(player: Player, hand: PlayerHand): Boolean {
        return interact(player, hand, Click::Right)
    }

    internal fun onSwing(player: Player, hand: PlayerHand): Boolean {
        return interact(player, hand, Click::Left)
    }
}
