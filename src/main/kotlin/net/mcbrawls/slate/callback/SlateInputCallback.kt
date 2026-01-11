package net.mcbrawls.slate.callback

import net.mcbrawls.slate.Slate
import net.minestom.server.entity.Player

fun interface SlateInputCallback {
    fun onInput(slate: Slate, player: Player, input: String)
}
