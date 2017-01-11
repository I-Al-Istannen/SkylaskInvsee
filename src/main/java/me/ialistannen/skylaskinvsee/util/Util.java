package me.ialistannen.skylaskinvsee.util;

import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import me.ialistannen.skylaskinvsee.SkylaskInvsee;
import me.ialistannen.skylaskinvsee.event.DragClickEvent;

/**
 * Some static utility functions
 */
public class Util {

    /**
     * Translates a message
     *
     * @param key The key to translate
     * @param formattingObjects The formatting objects
     *
     * @return The translated message
     */
    public static String trWithPrefix(String key, Object... formattingObjects) {
        return tr("prefix") + tr(key, formattingObjects);
    }

    /**
     * Translates a message
     *
     * @param key The key to translate
     * @param formattingObjects The formatting objects
     *
     * @return The translated message
     */
    public static String tr(String key, Object... formattingObjects) {
        return SkylaskInvsee.getInstance().getLanguage().tr(key, formattingObjects);
    }

    /**
     * @param rawSlot The raw slot
     * @param view The {@link InventoryView} to check
     *
     * @return True if it is in the top inv
     */
    public static boolean isInTopInv(int rawSlot, InventoryView view) {
        return rawSlot < view.getTopInventory().getSize();
    }

    /**
     * Checks if it is a simple click
     *
     * @param event The {@link InventoryClickEvent} to check
     *
     * @return True if it is a simple click
     */
    public static boolean isSimpleClick(InventoryClickEvent event) {
        ClickType type = event.getClick();
        return (event instanceof DragClickEvent)
                || type == ClickType.LEFT
                || type == ClickType.RIGHT
                || type == ClickType.MIDDLE;
    }

    /**
     * Checks if it is a shift click
     *
     * @param event The {@link InventoryClickEvent} to check
     *
     * @return True if it is a shift click
     */
    public static boolean isShiftClick(InventoryClickEvent event) {
        ClickType type = event.getClick();
        return type == ClickType.SHIFT_LEFT || type == ClickType.SHIFT_RIGHT;
    }

    /**
     * @param itemStack The {@link ItemStack} to check
     *
     * @return True if the item is null or AIR
     */
    public static boolean isItemEmpty(ItemStack itemStack) {
        return itemStack == null || itemStack.getType() == Material.AIR;
    }

}
