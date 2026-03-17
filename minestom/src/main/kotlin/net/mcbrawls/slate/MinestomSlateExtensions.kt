package net.mcbrawls.slate

import net.mcbrawls.slate.callback.handler.SlateCallbackHandler
import net.mcbrawls.slate.layer.SlateLayer
import net.mcbrawls.slate.layer.callback.SlateLayerCallbackHandler
import net.mcbrawls.slate.layer.paged.PagedSlateLayer
import net.mcbrawls.slate.screen.slot.ClickModifier
import net.mcbrawls.slate.screen.slot.SlateClickType
import net.mcbrawls.slate.tile.StackTile
import net.mcbrawls.slate.tile.SuspendedTile
import net.mcbrawls.slate.tile.Tile
import net.mcbrawls.slate.tile.TileClickCallback
import net.minestom.server.entity.Player
import net.minestom.server.inventory.click.Click
import net.minestom.server.item.ItemStack

// ---------------------------------------------------------------------------
// Platform-contract cast — only call from code guaranteed to receive
// Minestom SlatePlayer instances. Never write this cast in user code.
// ---------------------------------------------------------------------------

val SlatePlayer.player: Player get() = native as Player

// ---------------------------------------------------------------------------
// Player convenience extensions
// ---------------------------------------------------------------------------

val Player.slate: Slate? get() = MinestomSlatePlayer(this).slate

// ---------------------------------------------------------------------------
// Source-compat Slate overloads with Player param
// ---------------------------------------------------------------------------

fun Slate.open(player: Player): Boolean = open(MinestomSlatePlayer(player))
fun Slate.close(player: Player): Boolean = close(MinestomSlatePlayer(player))
fun Slate.back(player: Player): Boolean = back(MinestomSlatePlayer(player))
fun Slate.openParent(player: Player): Boolean = openParent(MinestomSlatePlayer(player))

// ---------------------------------------------------------------------------
// Typed callback overloads — unwrap SlatePlayer to Player automatically
// ---------------------------------------------------------------------------

fun SlateCallbackHandler.onOpen(callback: (Slate, Player) -> Unit) {
    registerOnOpen { slate, player -> callback(slate, player.player) }
}

fun SlateCallbackHandler.onTick(callback: (Slate, Player) -> Unit) {
    registerOnTick { slate, player -> callback(slate, player.player) }
}

fun SlateCallbackHandler.onClose(callback: (Slate, Player) -> Unit) {
    registerOnClose { slate, player -> callback(slate, player.player) }
}

fun SlateCallbackHandler.onChildClose(callback: (Slate, Player) -> Unit) {
    registerOnChildClose { slate, player -> callback(slate, player.player) }
}

@Deprecated("Not yet implemented")
fun SlateCallbackHandler.onInput(callback: (Slate, Player, String) -> Unit) {
    @Suppress("DEPRECATION")
    registerOnInput { slate, player, input -> callback(slate, player.player, input) }
}

fun SlateLayerCallbackHandler.onTick(callback: (Slate, SlateLayer, Player) -> Unit) {
    registerOnTick { slate, layer, player -> callback(slate, layer, player.player) }
}

// ---------------------------------------------------------------------------
// SuspendedTile typed constructor overload
// ---------------------------------------------------------------------------

fun SuspendedTile(baseTile: Tile, factory: suspend (Slate, Player) -> Tile?): SuspendedTile {
    return SuspendedTile(baseTile) { slate, player ->
        factory(slate, player.player)
    }
}

// ---------------------------------------------------------------------------
// PagedSlateLayer ItemStack convenience overloads
// ---------------------------------------------------------------------------

fun PagedSlateLayer.createPageChangeTile(
    title: String,
    modifier: (Int) -> Int,
    stack: ItemStack,
    callback: TileClickCallback? = null,
): Tile = createPageChangeTile(title, modifier, StackTile(stack), callback)

fun PagedSlateLayer.createNextPageTile(stack: ItemStack, callback: TileClickCallback? = null): Tile =
    createNextPageTile(StackTile(stack), callback)

fun PagedSlateLayer.createPreviousPageTile(stack: ItemStack, callback: TileClickCallback? = null): Tile =
    createPreviousPageTile(StackTile(stack), callback)

// ---------------------------------------------------------------------------
// Click parsing (moved from ClickModifier/SlateClickType companion objects)
// ---------------------------------------------------------------------------

fun parseClickModifiers(click: Click): List<ClickModifier> {
    return buildList {
        if (click is Click.LeftShift || click is Click.RightShift) {
            add(ClickModifier.SHIFT)
        }

        if (click is Click.Double) {
            add(ClickModifier.DOUBLE)
        }
    }
}

fun parseSlateClickType(click: Click): SlateClickType {
    return when (click) {
        is Click.Left -> SlateClickType.LEFT
        is Click.LeftShift -> SlateClickType.LEFT
        is Click.LeftDrag -> SlateClickType.LEFT

        is Click.Right -> SlateClickType.RIGHT
        is Click.RightShift -> SlateClickType.RIGHT
        is Click.RightDrag -> SlateClickType.RIGHT

        is Click.OffhandSwap -> SlateClickType.OFFHAND
        is Click.HotbarSwap -> SlateClickType.NUMBER_KEY
        is Click.Middle -> SlateClickType.MIDDLE
        is Click.DropCursor -> SlateClickType.THROW

        is Click.Double -> SlateClickType.LEFT
        is Click.MiddleDrag -> SlateClickType.MIDDLE
        is Click.DropSlot -> SlateClickType.THROW
    }
}
