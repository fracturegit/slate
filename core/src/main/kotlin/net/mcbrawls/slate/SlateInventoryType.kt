package net.mcbrawls.slate

enum class SlateInventoryType(val width: Int, val height: Int) {
    CHEST_1_ROW(9, 1),
    CHEST_2_ROW(9, 2),
    CHEST_3_ROW(9, 3),
    CHEST_4_ROW(9, 4),
    CHEST_5_ROW(9, 5),
    CHEST_6_ROW(9, 6),
    WINDOW_3X3(3, 3),
    CRAFTER_3X3(9, 3),
    ANVIL(1, 3),
    BEACON(1, 1),
    BLAST_FURNACE(1, 3),
    BREWING_STAND(1, 1),
    CRAFTING(2, 6),
    ENCHANTMENT(1, 2),
    FURNACE(1, 3),
    GRINDSTONE(1, 3),
    HOPPER(5, 1),
    LECTERN(9, 3),
    LOOM(1, 3),
    MERCHANT(1, 3),
    SHULKER_BOX(9, 3),
    SMITHING(4, 1),
    SMOKER(1, 3),
    CARTOGRAPHY(1, 3),
    STONE_CUTTER(1, 2);

    val size: Int = width * height
}
