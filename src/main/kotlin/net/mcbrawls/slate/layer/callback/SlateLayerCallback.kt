package net.mcbrawls.slate.layer.callback

import net.mcbrawls.slate.Slate
import net.mcbrawls.slate.layer.SlateLayer
import net.minestom.server.entity.Player

fun interface SlateLayerCallback {
    operator fun invoke(slate: Slate, layer: SlateLayer, player: Player)
}
