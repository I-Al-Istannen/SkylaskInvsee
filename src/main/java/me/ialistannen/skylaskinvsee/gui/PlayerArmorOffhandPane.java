package me.ialistannen.skylaskinvsee.gui;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.perceivedev.perceivecore.gui.base.Component;

/**
 * A pane displaying the player armor and offhand
 */
public class PlayerArmorOffhandPane extends PlayerInventoryPartPane {

    /**
     * Creates a new {@link PlayerArmorOffhandPane}
     *
     * @param uuid The UUID of the player
     * @param inventory The inventory of the player
     */
    public PlayerArmorOffhandPane(UUID uuid, Inventory inventory) {
        // 4 armor, 1 offhand
        super(5, 1, uuid, inventory);
    }

    @Override
    public void render(Inventory inventory, Player player, int x, int y) {
        clear();

        // player's hotbar AND the 3 inventory rows
        for (int i = 36; i < 41; i++) {
            Component button = getButton(i, this.inventory.getItem(i));

            // adjust armor to first
            int buttonX = 39 - i;

            // adjust offhand to end
            if (i == 40) {
                buttonX = 4;
            }

            removeComponent(buttonX, 0);
            addComponent(button, buttonX, 0);
        }

        super.render(inventory, player, x, y);
    }
}