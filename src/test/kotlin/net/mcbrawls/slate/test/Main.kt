package net.mcbrawls.slate.test

import net.kyori.adventure.text.Component
import net.mcbrawls.slate.InventorySlate
import net.mcbrawls.slate.Slate.Companion.slate
import net.mcbrawls.slate.SlateListeners
import net.mcbrawls.slate.screen.SlateInventory
import net.mcbrawls.slate.tile.Tile.Companion.tile
import net.minestom.server.Auth
import net.minestom.server.MinecraftServer
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player
import net.minestom.server.entity.PlayerHand
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.event.player.PlayerHandAnimationEvent
import net.minestom.server.event.player.PlayerPacketEvent
import net.minestom.server.event.player.PlayerSpawnEvent
import net.minestom.server.event.player.PlayerUseItemEvent
import net.minestom.server.instance.LightingChunk
import net.minestom.server.instance.block.Block
import net.minestom.server.inventory.click.Click
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.network.packet.client.play.ClientClickWindowPacket
import net.minestom.server.network.packet.client.play.ClientCloseWindowPacket
import net.minestom.server.network.packet.client.play.ClientPlayerActionPacket
import net.minestom.server.network.packet.server.play.SetCursorItemPacket
import net.minestom.server.utils.inventory.PlayerInventoryUtils

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val server = MinecraftServer.init(Auth.Online())

        val instance = MinecraftServer.getInstanceManager().createInstanceContainer()
        instance.setChunkSupplier(::LightingChunk)
        instance.timeRate = 0

        MinecraftServer.getGlobalEventHandler().let { events ->
            events.addListener(AsyncPlayerConfigurationEvent::class.java) { event ->
                event.spawningInstance = instance

                val player = event.player
                player.setGameMode(GameMode.ADVENTURE)
            }

            events.addListener(PlayerSpawnEvent::class.java) { event ->
                if (!event.isFirstSpawn) return@addListener

                val player = event.player
                player.instance.setBlock(player.position.sub(0.0, 1.0, 0.0), Block.STONE)
                player.isFlying = true
            }
        }

        SlateListeners.initialize()

        val commandManager = MinecraftServer.getCommandManager()
        commandManager.register(object : Command("test") {
            init {
                addSyntax(CommandExecutor { player, context ->
                    if (player !is Player) return@CommandExecutor

                    slate(::InventorySlate) {
                        tiles {
                            for (i in 0 until tiles.size) {
                                tiles[i] = tile(Material.values().random()) {
                                    callbacks {
                                        onGenericClick { slate, tile, context ->
                                            println("Click! ${context.click.slot()} ${context.clickType} ${tile.createBaseStack(slate, context.player).material()}")
                                        }
                                    }
                                }
                            }
                        }

                        callbacks {
                            onOpen { _, player ->
                                player.sendActionBar(Component.text("Open"))
                                println("Open")
                            }

                            onClose { _, player ->
                                player.sendActionBar(Component.text("Closed"))
                                println("Closed")
                            }
                        }
                    }.open(player)
                })
            }
        })

        server.start("0.0.0.0", 25565)

        println("Ready!")
    }
}
