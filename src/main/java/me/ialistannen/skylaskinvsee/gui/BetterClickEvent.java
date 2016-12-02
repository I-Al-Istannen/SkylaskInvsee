package me.ialistannen.skylaskinvsee.gui;

import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import com.perceivedev.perceivecore.util.ItemFactory;

/**
 * A better click Event
 */
public class BetterClickEvent extends InventoryClickEvent {

    private BetterClickType betterClickType;
    private int maxAmount = -1;

    public BetterClickEvent(InventoryView view, InventoryType.SlotType type, int slot, ClickType click, InventoryAction action,
              BetterClickType betterClickType, int amountOfSlots) {
        super(view, type, slot, click, action);
        this.betterClickType = betterClickType;

        calculateMaxAmount(amountOfSlots);
    }
    
    private void calculateMaxAmount(int amountOfSlots) {
        if (betterClickType == null) {
            return;
        }
        BetterClickType clickType = betterClickType;

        if (clickType == BetterClickType.DRAG_AND_SPLIT_ONE) {
            maxAmount = 1;
        } else {
            maxAmount = getCursor().getAmount() / amountOfSlots;
        }
    }

    @Override
    public ItemStack getCursor() {
        ItemStack cursor = super.getCursor();
        if (maxAmount < 0 || cursor == null || cursor.getType() == Material.AIR) {
            return cursor;
        }

        return ItemFactory.builder(cursor).setAmount(Math.min(cursor.getAmount(), maxAmount)).build();
    }

    /**
     * A Better clicked type
     */
    public enum BetterClickType {

        /**
         * Drags the items and splits them one by one
         */
        DRAG_AND_SPLIT_ONE,
        /**
         * Drags the items and splits them equally on all stacks.
         * <p>
         * The ones that are too much will be left on the cursor
         */
        DRAG_AND_SPLIT_EQUAL;
    }
}