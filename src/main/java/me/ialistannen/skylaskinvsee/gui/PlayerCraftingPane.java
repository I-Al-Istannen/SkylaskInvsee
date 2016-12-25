package me.ialistannen.skylaskinvsee.gui;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.perceivedev.perceivecore.gui.base.Component;
import com.perceivedev.perceivecore.gui.components.Label;
import com.perceivedev.perceivecore.gui.util.Dimension;
import com.perceivedev.perceivecore.util.ItemFactory;

import me.ialistannen.skylaskinvsee.util.Util;

/**
 * Displays the items in the Player's Crafting view
 */
class PlayerCraftingPane extends PlayerInventoryPartPane {

    /**
     * Creates a new {@link PlayerCraftingPane}
     *
     * @param uuid The UUID of the player
     * @param inventory The CRAFTING inventory of the player
     */
    PlayerCraftingPane(UUID uuid, Inventory inventory) {
        // 4 slots
        super(4, 1, uuid, inventory);
    }

    @Override
    public void render(Inventory inventory, Player player, int x, int y) {
        clear();

        // the 4 crafting slots
        for (int i = 1; i < 5; i++) {
            Component button = getButton(Bukkit.getPlayer(playerID), i);

            int buttonX = i - 1;

            removeComponent(buttonX, 0);
            addComponent(button, buttonX, 0);
        }

        super.render(inventory, player, x, y);
    }

    private Component getButton(Player player, int slot) {
        if (player != null) {
            Inventory inventory = player.getOpenInventory().getTopInventory();

            if (inventory.getType() != InventoryType.CRAFTING) {
                return getLockedLabel();
            }
        }
        else {
            // Should NOT happen, ...
            return getLockedLabel();
        }

        ItemStack item = inventory.getItem(slot);

        if (!mayPlayerModifyItem()) {
            return isItemEmpty(item) ? getItemLabel(new ItemStack(Material.AIR)) : getItemLabel(item);
        }

        if (isItemEmpty(item)) {
            return getButton(slot, new ItemStack(Material.AIR));
        }

        return getButton(slot, item);
    }

    private Label getLockedLabel() {
        return new Label(ItemFactory
                .builder(Material.BARRIER)
                .setName(Util.tr("craft.pane.locked.name"))
                .build(),
                Dimension.ONE);
    }

}
