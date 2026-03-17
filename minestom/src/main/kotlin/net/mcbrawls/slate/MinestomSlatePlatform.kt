package net.mcbrawls.slate

import com.noxcrew.noxesium.api.NoxesiumReferences
import com.noxcrew.noxesium.core.registry.CommonItemComponentTypes
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.kyori.adventure.text.Component
import net.mcbrawls.slate.MinestomSlatePlatform.evict
import net.mcbrawls.slate.MinestomSlatePlatform.updateScreenTitle
import net.mcbrawls.slate.screen.SlateInventory
import net.mcbrawls.slate.tile.MinestomTile
import net.mcbrawls.slate.tile.RedirectType
import net.mcbrawls.slate.tile.RedirectedTile
import net.mcbrawls.slate.tile.SuspendedTile
import net.mcbrawls.slate.tile.Tile
import net.minestom.server.component.DataComponents
import net.minestom.server.entity.Player
import net.minestom.server.item.ItemStack
import net.minestom.server.item.component.CustomData
import net.minestom.server.item.component.TooltipDisplay

object MinestomSlatePlatform : SlatePlatform {
    /**
     * Tracks which player has which slate open.
     * Used for [updateScreenTitle] lookups. Evicted explicitly in [evict].
     */
    private val slateToPlayer: MutableMap<Slate, Player> = HashMap()

    private const val BUKKIT_COMPOUND_ID = "PublicBukkitValues"

    fun initialize() {
        if (SlateCore.platform != MinestomSlatePlatform) {
            SlateCore.platform = MinestomSlatePlatform
            SlateListeners.initialize()
        }
    }

    override fun wrapPlayer(native: Any): SlatePlayer = MinestomSlatePlayer(native as Player)

    override fun openForPlayer(slate: Slate, player: SlatePlayer): Boolean {
        if (slateToPlayer.containsKey(slate)) {
            Slate.logger.debug("Tried to reopen already opened slate: {}, {}", slate, player)
            return false
        }

        val minestomPlayer = player.player
        val slateInventory = SlateInventory(slate, minestomPlayer, slate.tiles.inventoryType, slate.title)
        if (minestomPlayer.openInventory(slateInventory)) {
            slateToPlayer[slate] = minestomPlayer
            slate.onOpen(player)
            return true
        }

        return false
    }

    override fun closeForPlayer(slate: Slate, player: SlatePlayer): Boolean {
        val minestomPlayer = player.player
        val openInventory = minestomPlayer.openInventory
        if (openInventory is SlateInventory<*> && openInventory.slate == slate) {
            minestomPlayer.closeInventory()
            return true
        }

        return false
    }

    override fun updateScreenTitle(slate: Slate, title: Component) {
        slateToPlayer[slate]?.also { player ->
            (player.openInventory as? SlateInventory<*>)?.title = title
        }
    }

    override fun getOpenSlate(player: SlatePlayer): Slate? {
        return (player.player.openInventory as? SlateInventory<*>)?.slate
    }

    /**
     * Called from [SlateInventory.removeViewer] to evict the slate from the tracking map.
     */
    fun evict(slate: Slate) {
        slateToPlayer.remove(slate)
    }

    /**
     * Renders a tile to an [ItemStack] for display in the inventory.
     * Handles SuspendedTile resolution, RedirectedTile delegation, and MinestomTile rendering.
     * Falls back to [ItemStack.AIR] for plain Tile instances.
     */
    fun renderTile(tile: Tile, slate: Slate, player: Player): ItemStack {
        return when (tile) {
            is SuspendedTile -> {
                val resolved = tile.updateTile(slate, MinestomSlatePlayer(player))
                renderTile(resolved, slate, player)
            }
            is RedirectedTile -> when (tile.type) {
                RedirectType.NORMAL -> renderTile(tile.parent, slate, player)
                RedirectType.INVISIBLE -> ItemStack.AIR
            }
            is MinestomTile -> {
                val builder = tile.createBaseStack(slate, player).builder()
                applyTooltip(builder, tile)
                applyImmovable(builder, tile)
                applyDisplayedCount(builder, tile)
                builder.build()
            }
            else -> ItemStack.AIR
        }
    }

    private fun applyTooltip(stack: ItemStack.Builder, tile: Tile) {
        if (tile.tooltip.isEmpty()) {
            stack.set(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay(true, ReferenceLinkedOpenHashSet()))
        } else {
            val tooltip = tile.tooltip.toMutableList()
            val name = tooltip.removeFirst()

            stack.set(DataComponents.CUSTOM_NAME, name.applyFallbackStyle(Tile.DEFAULT_STYLE))

            if (tooltip.isNotEmpty()) {
                stack.set(DataComponents.LORE, tooltip.map { text ->
                    text.applyFallbackStyle(Tile.DEFAULT_STYLE)
                })
            }
        }
    }

    private fun applyImmovable(stack: ItemStack.Builder, tile: Tile) {
        if (tile.immovable) {
            val nbt = CompoundBinaryTag.builder()

            val immovableId = CommonItemComponentTypes.IMMOVABLE.id.toString()

            val noxComponentTag = CompoundBinaryTag.builder()
            noxComponentTag.put(immovableId, CompoundBinaryTag.empty())
            nbt.put(NoxesiumReferences.COMPONENT_NAMESPACE, noxComponentTag.build())

            val bukkitNoxComponentTag = CompoundBinaryTag.builder()
            bukkitNoxComponentTag.putBoolean(immovableId, true)
            nbt.put(BUKKIT_COMPOUND_ID, bukkitNoxComponentTag.build())

            stack.set(DataComponents.CUSTOM_DATA, CustomData(nbt.build()))
        }
    }

    private fun applyDisplayedCount(stack: ItemStack.Builder, tile: Tile) {
        tile.displayedAmount?.also { amount ->
            stack.amount(amount)
        }
    }
}
