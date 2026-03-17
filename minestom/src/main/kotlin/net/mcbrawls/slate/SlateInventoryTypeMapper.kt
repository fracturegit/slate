package net.mcbrawls.slate

import net.minestom.server.inventory.InventoryType

fun SlateInventoryType.toMinestom(): InventoryType = when (this) {
    SlateInventoryType.CHEST_1_ROW -> InventoryType.CHEST_1_ROW
    SlateInventoryType.CHEST_2_ROW -> InventoryType.CHEST_2_ROW
    SlateInventoryType.CHEST_3_ROW -> InventoryType.CHEST_3_ROW
    SlateInventoryType.CHEST_4_ROW -> InventoryType.CHEST_4_ROW
    SlateInventoryType.CHEST_5_ROW -> InventoryType.CHEST_5_ROW
    SlateInventoryType.CHEST_6_ROW -> InventoryType.CHEST_6_ROW
    SlateInventoryType.WINDOW_3X3 -> InventoryType.WINDOW_3X3
    SlateInventoryType.CRAFTER_3X3 -> InventoryType.CRAFTER_3X3
    SlateInventoryType.ANVIL -> InventoryType.ANVIL
    SlateInventoryType.BEACON -> InventoryType.BEACON
    SlateInventoryType.BLAST_FURNACE -> InventoryType.BLAST_FURNACE
    SlateInventoryType.BREWING_STAND -> InventoryType.BREWING_STAND
    SlateInventoryType.CRAFTING -> InventoryType.CRAFTING
    SlateInventoryType.ENCHANTMENT -> InventoryType.ENCHANTMENT
    SlateInventoryType.FURNACE -> InventoryType.FURNACE
    SlateInventoryType.GRINDSTONE -> InventoryType.GRINDSTONE
    SlateInventoryType.HOPPER -> InventoryType.HOPPER
    SlateInventoryType.LECTERN -> InventoryType.LECTERN
    SlateInventoryType.LOOM -> InventoryType.LOOM
    SlateInventoryType.MERCHANT -> InventoryType.MERCHANT
    SlateInventoryType.SHULKER_BOX -> InventoryType.SHULKER_BOX
    SlateInventoryType.SMITHING -> InventoryType.SMITHING
    SlateInventoryType.SMOKER -> InventoryType.SMOKER
    SlateInventoryType.CARTOGRAPHY -> InventoryType.CARTOGRAPHY
    SlateInventoryType.STONE_CUTTER -> InventoryType.STONE_CUTTER
}
