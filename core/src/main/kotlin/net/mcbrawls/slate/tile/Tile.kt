package net.mcbrawls.slate.tile

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import net.mcbrawls.slate.screen.slot.ClickModifier
import net.mcbrawls.slate.screen.slot.SlateClickType
import net.mcbrawls.slate.tooltip.TooltipChunk

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
        val DEFAULT_STYLE = Style.style()
            .color(NamedTextColor.WHITE)
            .decoration(TextDecoration.ITALIC, false)
            .build()

        /**
         * Builds a tile.
         */
        inline fun <T : Tile> tile(factory: () -> T, builder: T.() -> Unit = {}): T {
            return factory.invoke().apply(builder)
        }
    }
}
