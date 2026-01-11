package net.mcbrawls.slate.callback

import net.mcbrawls.slate.Slate
import net.minestom.server.entity.Player

fun interface SlateCallback {
    operator fun invoke(slate: Slate, player: Player)
}
