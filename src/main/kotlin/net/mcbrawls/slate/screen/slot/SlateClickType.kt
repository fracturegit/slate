package net.mcbrawls.slate.screen.slot

import net.minestom.server.inventory.click.Click

enum class SlateClickType {
    LEFT,
    RIGHT,

    /**
     * Only works in creative mode.
     */
    MIDDLE,

    NUMBER_KEY,
    OFFHAND,
    THROW;

    companion object {
        /**
         * Parses a click type from the given data.
         */
        fun parse(click: Click): SlateClickType {
            return when (click) {
                is Click.Left -> LEFT
                is Click.LeftShift -> LEFT
                is Click.LeftDrag -> LEFT

                is Click.Right -> RIGHT
                is Click.RightShift -> RIGHT
                is Click.RightDrag -> RIGHT

                is Click.OffhandSwap -> OFFHAND
                is Click.HotbarSwap -> NUMBER_KEY
                is Click.Middle -> MIDDLE
                is Click.DropCursor -> THROW

                is Click.Double -> LEFT
                is Click.MiddleDrag -> MIDDLE
                is Click.DropSlot -> THROW
            }
        }
    }
}
