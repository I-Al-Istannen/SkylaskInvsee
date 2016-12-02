package me.ialistannen.skylaskinvsee.gui;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.perceivedev.perceivecore.gui.base.Component;

/**
 * A Pane that represents a Player Inventory
 */
class PlayerInventoryPane extends PlayerInventoryPartPane {

    /**
     * Creates a new PlayerInventoryPane
     */
    PlayerInventoryPane(UUID uuid, Inventory inventory) {
        // 1 hotbar, 3 inventory
        super(9, 4, uuid, inventory);
    }

    @Override
    public void render(Inventory inventory, Player player, int x, int y) {
        clear();

        // player's hotbar AND the 3 inventory rows
        for (int i = 0; i < 36; i++) {
            Component button = getButton(i, this.inventory.getItem(i));

            int[] translatedSlot = translateSlot(i);
            int buttonX = translatedSlot[0];
            int buttonY = translatedSlot[1];

            removeComponent(buttonX, buttonY);
            addComponent(button, buttonX, buttonY);
        }

        super.render(inventory, player, x, y);
    }
    
    private static int[] translateSlot(int slot) {
        if (slot < 9) {
            return new int[] { slot, 3 };
        }

        int adjustedSlot = slot - 9;
        int x = adjustedSlot % 9;
        int y = adjustedSlot / 9;

        return new int[] { x, y };
    }
}
