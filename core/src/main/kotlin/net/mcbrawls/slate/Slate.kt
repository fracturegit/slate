package net.mcbrawls.slate

import net.kyori.adventure.text.Component
import net.mcbrawls.slate.callback.ChildSlateCloseCallback
import net.mcbrawls.slate.callback.SlateCloseCallback
import net.mcbrawls.slate.callback.SlateOpenCallback
import net.mcbrawls.slate.callback.SlateTickCallback
import net.mcbrawls.slate.callback.handler.SlateCallbackHandler
import net.mcbrawls.slate.layer.SlateLayer
import net.mcbrawls.slate.layer.paged.PagedSlateLayer
import net.mcbrawls.slate.screen.slot.TileClickContext
import net.mcbrawls.slate.tile.HandledTileGrid
import net.mcbrawls.slate.tile.Tile
import net.mcbrawls.slate.tile.TileGrid
import org.slf4j.Logger
import org.slf4j.LoggerFactory

open class Slate {
    /**
     * An identifiable key for this slate. Use how you wish.
     */
    open var key: String? = null

    /**
     * Identifiable tags for this slate. Use how you wish.
     */
    val tags: MutableSet<String> = mutableSetOf()

    /**
     * The title of the screen handler.
     */
    open var title: Component = Component.empty()
        set(value) {
            field = value
            SlateCore.platform?.updateScreenTitle(this, value)
        }

    /**
     * The base tile grid of this slate.
     */
    open var tiles: HandledTileGrid = TileGrid.create(SlateInventoryType.CHEST_6_ROW)

    /**
     * Layers displayed on top of the base tile grid.
     */
    val layers: MutableList<LayerWithIndex> = mutableListOf()

    /**
     * Handles all callbacks for this slate.
     */
    open var callbackHandler: SlateCallbackHandler = SlateCallbackHandler()

    /**
     * Whether this slate can be closed manually by the player.
     */
    open var canPlayerClose: Boolean = true

    /**
     * Whether this slate can or should be closed naturally at all.
     */
    open var canBeClosed: Boolean = true

    /**
     * The parent of this slate, which can be returned to.
     */
    var parent: Slate? = null

    /**
     * Whether this slate should be synced.
     */
    @Volatile
    var dirty: Boolean = true

    /**
     * Whether this slate is currently open for a player.
     */
    internal var isOpen: Boolean = false

    val inventoryType: SlateInventoryType get() = tiles.inventoryType

    val size: Int get() = tiles.getFullSize()

    /**
     * Provides a factory for setting up callbacks of this slate.
     */
    inline fun callbacks(factory: SlateCallbackHandler.() -> Unit) {
        callbackHandler = callbackHandler.apply(factory)
    }

    /**
     * Modifies the tile grid on this slate.
     */
    inline fun tiles(action: HandledTileGrid.() -> Unit) {
        action.invoke(tiles)
    }

    /**
     * Adds a layer to this slate at the given tile index.
     */
    fun addLayer(index: Int, layer: SlateLayer) {
        layers.add(LayerWithIndex(index, layer))
    }

    /**
     * Adds a layer to this slate at the given coordinates.
     */
    fun addLayer(x: Int, y: Int, layer: SlateLayer) {
        addLayer(TileGrid.toIndex(x, y, tiles.width), layer)
    }

    /**
     * Adds a layer to this slate.
     * @return the created layer
     */
    inline fun addLayer(
        index: Int,
        width: Int,
        height: Int,
        factory: SlateLayer.Factory = SlateLayer.Factory(::SlateLayer),
        builder: SlateLayer.() -> Unit,
    ) : SlateLayer {
        val layer = factory
            .create(width, height)
            .apply(builder)

        addLayer(index, layer)

        return layer
    }

    /**
     * Adds a paged layer to this slate.
     * @return the created layer
     */
    inline fun addPagedLayer(
        index: Int,
        width: Int,
        height: Int,
        maxCount: Int,
        slotFactory: PagedSlateLayer.SlotFactory,
        builder: SlateLayer.() -> Unit = {},
    ) : PagedSlateLayer {
        val layer = object : PagedSlateLayer(maxCount, width, height) {
            override fun createTile(index: Int, oldTiles: Array<Tile?>): Tile? {
                return slotFactory.createSlot(this, index, oldTiles)
            }
        }.apply(builder)

        addLayer(index, layer)

        return layer
    }

    /**
     * Adds tags to the tag list.
     */
    fun tags(vararg tags: String) {
        this.tags.addAll(tags)
    }

    /**
     * Builds a slate with this slate as the parent.
     */
    inline fun subslate(factory: () -> Slate = ::Slate, builder: Slate.() -> Unit = {}): Slate {
        val slate = factory.invoke()
        slate.parent = this
        return slate.apply(builder)
    }

    /**
     * Modifies the given slate to be a subslate of this slate.
     */
    fun subslate(slate: Slate): Slate {
        slate.parent = this
        return slate
    }

    operator fun get(tileIndex: Int): Tile? {
        val x = tileIndex % tiles.width
        val y = tileIndex / tiles.width

        val layerTile = layers
            .asReversed()
            .firstNotNullOfOrNull { (layerIndex, layer) ->
                // convert global position to layer-local position
                val layerStartX = layerIndex % tiles.width
                val layerStartY = layerIndex / tiles.width
                val layerX = x - layerStartX
                val layerY = y - layerStartY

                // check if the position is within layer bounds
                if (layerX in 0 until layer.width && layerY in 0 until layer.height) {
                    layer.tiles[layerY * layer.width + layerX]
                } else {
                    null
                }
            }

        return layerTile ?: tiles[tileIndex]
    }

    fun onOpen(player: SlatePlayer) {
        isOpen = true

        // invoke callbacks
        callbackHandler.collectCallbacks<SlateOpenCallback>().invoke(this, player)

        dirty = true
    }

    fun onTick(player: SlatePlayer) {
        // invoke callbacks
        callbackHandler.collectCallbacks<SlateTickCallback>().invoke(this, player)

        // layer ticks
        layers.forEach { indexedLayer ->
            indexedLayer.layer.onTick(this, player)
        }
    }

    fun onClosed(player: SlatePlayer) {
        if (!isOpen) return
        isOpen = false

        // invoke callbacks
        callbackHandler.collectCallbacks<SlateCloseCallback>().invoke(this, player)

        parent?.also { firstParent ->
            val parents: List<Slate> = buildList {
                add(firstParent)

                // add nested parents
                var nestedParent = firstParent.parent
                while (nestedParent != null) {
                    add(nestedParent)
                    nestedParent = nestedParent.parent
                }
            }

            parents.forEach { parent ->
                parent.callbackHandler.collectCallbacks<ChildSlateCloseCallback>().invoke(this, player)
            }
        }
    }

    /**
     * Called when any slot is clicked on the client.
     */
    open fun onSlotClicked(context: TileClickContext) {
        context.tile?.also { tile ->
            val clickType = context.clickType
            val callback = tile.collectClickCallbacks(clickType)
            callback.onClick(this, tile, context)
        }
    }

    /**
     * Called when the client input changes.
     */
    fun onAnvilInput(player: SlatePlayer, input: String) {
        callbackHandler.collectInputCallbacks().onInput(this, player, input)
    }

    /**
     * Opens a slate for the given player.
     * @return whether the slate was opened successfully
     */
    open fun open(player: SlatePlayer): Boolean {
        return SlateCore.platform?.openForPlayer(this, player) ?: false
    }

    /**
     * Closes this slate if it is open.
     */
    fun close(player: SlatePlayer): Boolean {
        return SlateCore.platform?.closeForPlayer(this, player) ?: false
    }

    /**
     * Returns to the previous slate, or closes the slate.
     */
    fun back(player: SlatePlayer): Boolean {
        return if (parent != null) {
            openParent(player)
        } else {
            close(player)
        }
    }

    /**
     * Opens the parent of this slate, if present.
     */
    fun openParent(player: SlatePlayer): Boolean {
        parent?.also { parent -> return parent.open(player) }
        return false
    }

    /**
     * Removes a layer from this slate.
     * @return if the layer was removed
     */
    fun removeLayer(layer: SlateLayer): Boolean {
        return layers.removeIf { it.layer == layer }
    }

    override fun toString(): String {
        return "Slate{${tiles.inventoryType}:$title, $tiles}"
    }

    /**
     * The stored data for an active layer.
     */
    data class LayerWithIndex(
        val index: Int,
        val layer: SlateLayer,
    )

    companion object {
        val logger: Logger = LoggerFactory.getLogger(Slate::class.java)

        /**
         * Builds a default slate.
         */
        inline fun slate(factory: () -> Slate = ::Slate, builder: Slate.() -> Unit = {}): Slate {
            return factory.invoke().apply(builder)
        }

        /**
         * Checks if this slate (nullable) has the checked key.
         */
        fun Slate?.hasKey(checkedKey: String): Boolean {
            return this != null && key == checkedKey
        }

        /**
         * Checks if this slate (nullable) contains the checked tag.
         */
        fun Slate?.hasTag(checkedTag: String): Boolean {
            return this != null && tags.contains(checkedTag)
        }
    }
}
