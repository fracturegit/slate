package net.mcbrawls.slate.layer.callback

import net.mcbrawls.slate.Slate
import net.mcbrawls.slate.SlatePlayer
import net.mcbrawls.slate.layer.SlateLayer

fun interface SlateLayerCallback {
    operator fun invoke(slate: Slate, layer: SlateLayer, player: SlatePlayer)
}
