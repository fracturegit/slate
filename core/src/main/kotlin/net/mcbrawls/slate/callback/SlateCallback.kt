package net.mcbrawls.slate.callback

import net.mcbrawls.slate.Slate
import net.mcbrawls.slate.SlatePlayer

fun interface SlateCallback {
    operator fun invoke(slate: Slate, player: SlatePlayer)
}
