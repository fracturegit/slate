package net.mcbrawls.slate.test

import net.kyori.adventure.text.Component
import net.mcbrawls.slate.InventorySlate
import net.mcbrawls.slate.MinestomSlatePlatform
import net.mcbrawls.slate.Slate.Companion.slate
import net.mcbrawls.slate.onClose
import net.mcbrawls.slate.onOpen
import net.mcbrawls.slate.open
import net.mcbrawls.slate.openParent
import net.mcbrawls.slate.player
import net.mcbrawls.slate.tile.tile
import net.minestom.server.Auth
import net.minestom.server.MinecraftServer
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.event.player.PlayerSpawnEvent
import net.minestom.server.instance.LightingChunk
import net.minestom.server.instance.block.Block
import net.minestom.server.item.Material

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

        MinestomSlatePlatform.initialize()

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
                                        onGenericClick { slate, tile, ctx ->
                                            println("Click! ${ctx.clickType} $tile")

                                            subslate {
                                                tiles {
                                                    addLayer(indexAt(3, 3), 4, 4) {
                                                        tiles {
                                                            tiles.fill(tile(Material.DIORITE))
                                                        }
                                                    }
                                                }

                                                tiles.tiles.indices.forEach { i ->
                                                    if (i % 2 == 0) return@forEach
                                                    tiles.tiles[i] = tile(Material.STONE)
                                                }

                                                callbacks {
                                                    onClose { _, player ->
                                                        openParent(player)
                                                    }
                                                }
                                            }.open(ctx.player.player)
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
