@file:Suppress("DeferredResultUnused", "OPT_IN_USAGE")

package net.mcbrawls.slate.tile

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import net.mcbrawls.slate.Slate
import net.mcbrawls.slate.Slate.Companion.slate
import net.minestom.server.entity.Player
import net.minestom.server.item.ItemStack
import org.apache.commons.lang3.mutable.MutableLong

/**
 * A tile provided by a suspended function.
 */
class SuspendedTile(
    /**
     * The tile to display when no tile has been calculated.
     */
    val baseTile: Tile,

    /**
     * The suspended function factory.
     */
    val tileFactory: ChildFactory,
) : Tile() {
    /**
     * The created tile.
     */
    private var tile: Tile? = null
        set(value) {
            field = value
            value?.also(::setMetadataFrom)
        }

    private var latestCallTimestamp = MutableLong(0L)

    var factoryState: FactoryState = FactoryState.EMPTY
        private set

    fun updateTile(slate: Slate, player: Player): Tile {
        if (factoryState == FactoryState.EMPTY) {
            factoryState = FactoryState.SUSPENDED

            val timestamp = getMeasuringTimeMs()
            latestCallTimestamp.value = timestamp

            GlobalScope.async {
                val newTile = tileFactory.create(slate, player)

                // only update for latest function call
                synchronized(latestCallTimestamp) {
                    if (timestamp == latestCallTimestamp.value) {
                        tile = newTile
                        factoryState = FactoryState.FINISHED

                        if (player.slate == slate) {
                            player.openInventory?.update(player)
                        }
                    }
                }
            }
        }

        return tile ?: baseTile
    }

    fun refreshTile() {
        factoryState = FactoryState.EMPTY
    }

    override fun createDisplayedStack(slate: Slate, player: Player): ItemStack {
        val trueTile = updateTile(slate, player)
        return trueTile.createDisplayedStack(slate, player)
    }

    fun interface ChildFactory {
        suspend fun create(slate: Slate, player: Player): Tile?
    }

    enum class FactoryState {
        EMPTY,
        SUSPENDED,
        FINISHED
    }

    companion object {
        fun tile(baseTile: Tile, tileFactory: ChildFactory): SuspendedTile {
            return SuspendedTile(baseTile, tileFactory)
        }

        fun getMeasuringTimeMs(): Long {
            return getMeasuringTimeNano() / 1_000_000L
        }

        fun getMeasuringTimeNano(): Long {
            return System.nanoTime()
        }
    }
}
