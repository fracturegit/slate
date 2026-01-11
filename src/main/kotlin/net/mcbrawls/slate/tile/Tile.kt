package net.mcbrawls.slate.tile

import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet
import net.kyori.adventure.key.Key
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import net.mcbrawls.slate.Slate
import net.mcbrawls.slate.screen.slot.ClickModifier
import net.mcbrawls.slate.screen.slot.SlateClickType
import net.mcbrawls.slate.tooltip.TooltipChunk
import net.minestom.server.component.DataComponents
import net.minestom.server.entity.Player
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.item.component.CustomData
import net.minestom.server.item.component.TooltipDisplay

/**
 * A slot within a slate.
 */
open class Tile {
    /**
     * The complete tooltip of the tile stack.
     * The first element is the name, and the rest is flushed to the tooltip.
     * All are formatted as reset by default, not vanilla's purple color.
     */
    val tooltip: MutableList<Component> = mutableListOf()

    /**
     * Whether this tile can be picked up and moved by the client.
     */
    var immovable: Boolean = true

    /**
     * The count displayed on the final stack.
     */
    var displayedAmount: Int? = null

    val clickCallbacks: MutableList<Pair<SlateClickType, TileClickCallback>> = mutableListOf()

    /**
     * Adds tooltips to this tile.
     */
    @JvmName("tooltipText")
    fun tooltip(tooltips: Collection<Component>) {
        tooltip.addAll(tooltips)
    }

    /**
     * Adds tooltips to this tile.
     */
    fun tooltip(vararg tooltips: Component) {
        tooltip(tooltips.toList())
    }

    /**
     * Adds tooltips to this tile.
     */
    @JvmName("tooltipString")
    fun tooltip(tooltips: Collection<String>) {
        tooltip.addAll(tooltips.map(Component::text))
    }

    /**
     * Adds tooltips to this tile.
     */
    fun tooltip(vararg tooltips: String) {
        tooltip(tooltips.toList())
    }

    /**
     * Adds tooltip chunks to this tile.
     */
    @JvmName("tooltipTooltipChunk")
    fun tooltip(chunks: List<TooltipChunk>) {
        val lastIndex = chunks.lastIndex
        chunks.forEachIndexed { index, chunk ->
            chunk.modifyTooltip(tooltip, index, lastIndex)
        }
    }

    /**
     * Adds tooltip chunks to this tile.
     */
    fun tooltip(vararg tooltips: TooltipChunk) {
        tooltip(tooltips.toList())
    }

    /**
     * Builds a chunked tooltip.
     */
    inline fun tooltipChunked(builder: MutableList<TooltipChunk>.() -> Unit) {
        tooltip(mutableListOf<TooltipChunk>().apply(builder))
    }

    /**
     * Adds a click callback for the given click type.
     */
    fun onClick(clickType: SlateClickType = SlateClickType.LEFT, callback: TileClickCallback) {
        clickCallbacks.add(clickType to callback)
    }

    /**
     * Adds a click callback for the given click type, using left click in both screen contexts.
     */
    fun onGenericClick(callback: TileClickCallback) {
        onClick(SlateClickType.LEFT) { slate, tile, context ->
            if (context.withinScreen && !context.modifiers.contains(ClickModifier.DOUBLE)) {
                callback.onClick(slate, tile, context)
            }
        }

        onClick(SlateClickType.RIGHT) { slate, tile, context ->
            if (context.withinScreen && !context.modifiers.contains(ClickModifier.DOUBLE)) {
                callback.onClick(slate, tile, context)
            }
        }
    }

    /**
     * The base item stack to be displayed.
     */
    open fun createBaseStack(slate: Slate, player: Player): ItemStack {
        return ItemStack.AIR
    }

    /**
     * Creates the final displayed stack for this tile.
     */
    open fun createDisplayedStack(slate: Slate, player: Player): ItemStack {
        val stack = createBaseStack(slate, player).builder()

        addTooltip(stack)
        addImmovable(stack)
        addDisplayedCount(stack)

        return stack.build()
    }

    fun addTooltip(stack: ItemStack.Builder) {
        if (tooltip.isEmpty()) {
            stack.set(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay(true, ReferenceLinkedOpenHashSet()))
        } else {
            val tooltip = tooltip.toMutableList()
            val name = tooltip.removeFirst()

            stack.set(DataComponents.CUSTOM_NAME, name.applyFallbackStyle(DEFAULT_STYLE))

            if (tooltip.isNotEmpty()) {
                stack.set(DataComponents.LORE, tooltip.map { text ->
                    text.applyFallbackStyle(DEFAULT_STYLE)
                })
            }
        }
    }

    fun addImmovable(stack: ItemStack.Builder) {
        if (immovable) {
            val nbt = CompoundBinaryTag.builder()

            val bukkitNbt = CompoundBinaryTag.builder()
            bukkitNbt.putBoolean(IMMOVABLE_TAG, true)

            nbt.put(BUKKIT_COMPOUND_ID, bukkitNbt.build())

            stack.set(DataComponents.CUSTOM_DATA, CustomData(nbt.build()))
        }
    }

    fun addDisplayedCount(stack: ItemStack.Builder) {
        displayedAmount?.also { amount ->
            stack.amount(amount)
        }
    }

    /**
     * Combines all callbacks for the given click type into one callable object.
     */
    internal open fun collectClickCallbacks(clickType: SlateClickType): TileClickCallback {
        return TileClickCallback { slate, tile, context ->
            clickCallbacks
                .filter { it.first == clickType }
                .map { it.second }
                .forEach { callback -> callback.onClick(slate, tile, context) }
        }
    }

    fun setMetadataFrom(tile: Tile) {
        tooltip.clear()
        tooltip.addAll(tile.tooltip)

        immovable = tile.immovable
        displayedAmount = tile.displayedAmount

        clickCallbacks.clear()
        clickCallbacks.addAll(tile.clickCallbacks)
    }

    override fun toString(): String {
        return "Tile"
    }

    companion object {
        const val BUKKIT_COMPOUND_ID = "PublicBukkitValues"
        const val NOXESIUM_NAMESPACE = "noxesium"

        val IMMOVABLE_TAG: String = Key.key(NOXESIUM_NAMESPACE, "immovable").toString()

        val DEFAULT_STYLE = Style.style()
            .color(NamedTextColor.WHITE)
            .decoration(TextDecoration.ITALIC, false)
            .build()

        /**
         * Builds a defaulted tile with an item stack.
         */
        inline fun tile(stack: ItemStack = ItemStack.AIR, builder: StackTile.() -> Unit = {}): StackTile {
            return tile({ StackTile(stack) }, builder)
        }

        /**
         * Builds a defaulted tile with an item.
         */
        inline fun tile(item: Material, builder: StackTile.() -> Unit = {}): StackTile {
            return tile(ItemStack.builder(item).build(), builder)
        }

        /**
         * Builds a tile.
         */
        inline fun <T : Tile> tile(factory: () -> T, builder: T.() -> Unit = {}): T {
            return factory.invoke().apply(builder)
        }
    }
}
