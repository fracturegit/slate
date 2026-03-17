package net.mcbrawls.slate.callback.handler

import net.mcbrawls.slate.Slate
import net.mcbrawls.slate.callback.ChildSlateCloseCallback
import net.mcbrawls.slate.callback.SlateCallback
import net.mcbrawls.slate.callback.SlateCloseCallback
import net.mcbrawls.slate.callback.SlateInputCallback
import net.mcbrawls.slate.callback.SlateOpenCallback
import net.mcbrawls.slate.callback.SlateTickCallback

/**
 * Handles callbacks for instances of [Slate].
 */
open class SlateCallbackHandler {
    val callbacks: MutableList<SlateCallback> = mutableListOf()
    val inputCallbacks: MutableList<SlateInputCallback> = mutableListOf()

    /**
     * Adds a callback invoked when the slate is opened.
     */
    fun registerOnOpen(callback: SlateOpenCallback) {
        callbacks.add(callback)
    }

    /**
     * Adds a callback invoked on the slate every game tick.
     */
    fun registerOnTick(callback: SlateTickCallback) {
        callbacks.add(callback)
    }

    /**
     * Adds a callback invoked when the slate is closed.
     */
    fun registerOnClose(callback: SlateCloseCallback) {
        callbacks.add(callback)
    }

    /**
     * Adds a callback invoked when any child slates are closed.
     */
    fun registerOnChildClose(callback: ChildSlateCloseCallback) {
        callbacks.add(callback)
    }

    /**
     * Adds a callback invoked when the slate input changes, namely for anvil screen handler types.
     */
    @Deprecated("Not yet implemented")
    fun registerOnInput(callback: SlateInputCallback) {
        inputCallbacks.add(callback)
    }

    /**
     * Combines all callbacks for the given type into one callable object.
     */
    inline fun <reified T : SlateCallback> collectCallbacks(): SlateCallback {
        return SlateCallback { slate, player ->
            callbacks
                .filterIsInstance<T>()
                .forEach { callback -> callback.invoke(slate, player) }
        }
    }

    /**
     * Combines all callbacks for the given type into one callable object.
     */
    fun collectInputCallbacks(): SlateInputCallback {
        return SlateInputCallback { slate, player, input ->
            inputCallbacks.forEach { callback -> callback.onInput(slate, player, input) }
        }
    }
}
