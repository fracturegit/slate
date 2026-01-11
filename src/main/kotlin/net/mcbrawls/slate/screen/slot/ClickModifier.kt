package net.mcbrawls.slate.screen.slot

import net.minestom.server.inventory.click.Click

enum class ClickModifier {
    SHIFT,
    DOUBLE;

    companion object {
        /**
         * Parses click modifiers from the given data.
         */
        fun parse(click: Click): List<ClickModifier> {
            return buildList {
                if (click is Click.LeftShift || click is Click.RightShift) {
                    add(SHIFT)
                }

                if (click is Click.Double) {
                    add(DOUBLE)
                }
            }
        }
    }
}
