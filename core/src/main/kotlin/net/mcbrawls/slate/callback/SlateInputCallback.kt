package net.mcbrawls.slate.callback

import net.mcbrawls.slate.Slate
import net.mcbrawls.slate.SlatePlayer

@Deprecated("Not yet implemented")
fun interface SlateInputCallback {
    fun onInput(slate: Slate, player: SlatePlayer, input: String)
}
