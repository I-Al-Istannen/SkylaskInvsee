package me.ialistannen.skylaskinvsee.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.perceivedev.perceivecore.gui.ClickEvent;
import com.perceivedev.perceivecore.gui.base.Component;
import com.perceivedev.perceivecore.gui.components.Button;
import com.perceivedev.perceivecore.gui.components.Label;
import com.perceivedev.perceivecore.gui.components.panes.AnchorPane;
import com.perceivedev.perceivecore.gui.util.Dimension;
import com.perceivedev.perceivecore.util.ItemFactory;

import me.ialistannen.skylaskinvsee.SkylaskInvsee;
import me.ialistannen.skylaskinvsee.packet.DragClickEvent;

/**
 * A Pane that holds items from the player's inventory
 */
class PlayerInventoryPartPane extends AnchorPane {

    final Inventory inventory;
    final UUID playerID;

    PlayerInventoryPartPane(int width, int height, UUID playerUUID, Inventory inventory) {
        super(width, height);

        this.inventory = inventory;
        playerID = playerUUID;
    }

    /**
     * @return True if the player may modify the item
     */
    boolean mayPlayerModifyItem() {
        String permission = SkylaskInvsee.getInstance().getConfig().getString("permissions.other.edit");
        Player player = Bukkit.getPlayer(playerID);

        return player != null && player.hasPermission(permission);
    }

    /**
     * Returns a Component that does not allow modification and displays the item
     *
     * @param itemStack The {@link ItemStack} to display
     *
     * @return The unmodifiable display component
     */
    Component getItemLabel(ItemStack itemStack) {
        return new Label(itemStack, Dimension.ONE);
    }

    /**
     * Returns a button to use.
     * <p>
     * Respects the {@link #mayPlayerModifyItem()} property
     *
     * @param slot The slot the button is at
     * @param item The item that is currently at this slot
     *
     * @return The button to place at this slot
     */
    Component getButton(int slot, ItemStack item) {
        if (isItemEmpty(item)) {
            if (mayPlayerModifyItem()) {
                return getUpdateButton(slot, new ItemStack(Material.AIR));
            }
            return getItemLabel(new ItemStack(Material.AIR));
        }

        if (!mayPlayerModifyItem()) {
            return getItemLabel(item);
        }

        return getUpdateButton(slot, item);
    }

    /**
     * Returns a Button that allows modification
     *
     * @param slot The slot the button is at
     * @param itemStack The itemstack for the Button
     *
     * @return The Button to use
     */
    private Component getUpdateButton(int slot, ItemStack itemStack) {
        Button button = new Button(itemStack, Dimension.ONE);

        button.setAction(clickEvent -> {
            InventoryClickEvent raw = clickEvent.getRaw();

            if (!isSimpleClick(raw)) {
                return;
            }

            if (raw instanceof DragClickEvent) {
                handleDrag(clickEvent, ((DragClickEvent) raw).getDragEvent(), button);
                return;
            }

            if (raw.getClick() == ClickType.MIDDLE) {
                // only works in creative
                if (clickEvent.getPlayer().getGameMode() != GameMode.CREATIVE) {
                    return;
                }
                // just do nothing, let them clone it
                clickEvent.setCancelled(false);
                return;
            }

            ItemStack cursor = raw.getCursor();

            ItemStack currentItem = raw.getCurrentItem();

            // allow retrieving of the item
            if (isItemEmpty(cursor)) {
                // clicked an empty slot
                if (isItemEmpty(currentItem)) {
                    return;
                }

                // clicked an item with an empty cursor

                clickEvent.setCancelled(false);

                // picked up half
                if (clickEvent.getClickType() == ClickType.RIGHT) {
                    inventory.setItem(slot, ItemFactory.builder(currentItem)
                            .setAmount(currentItem.getAmount() / 2)
                            .build());
                }
                else {
                    // picked up all
                    inventory.setItem(slot, null);
                }
                return;
            }

            // they have something in their cursor
            clickEvent.setCancelled(false);

            // just placing a single item.
            if (clickEvent.getClickType() == ClickType.RIGHT && isItemEmpty(currentItem)) {
                clickEvent.setCancelled(false);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        clickEvent.getPlayer().getOpenInventory().setCursor(cursor);
                    }
                }.runTask(SkylaskInvsee.getInstance());

                inventory.setItem(slot, ItemFactory.builder(cursor).setAmount(1).build());
                return;
            }

            // handle combining stacks
            if (!isItemEmpty(currentItem) && currentItem.isSimilar(cursor)) {

                if (clickEvent.getClickType() == ClickType.RIGHT) {
                    if (currentItem.getAmount() < currentItem.getType().getMaxStackSize()) {
                        currentItem.setAmount(currentItem.getAmount() + 1);
                        cursor.setAmount(cursor.getAmount() - 1);

                        clickEvent.setCancelled(true);

                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                clickEvent.getPlayer().getOpenInventory().setCursor(cursor);
                            }
                        }.runTask(SkylaskInvsee.getInstance());

                        inventory.setItem(slot, currentItem);
                    }
                    return;
                }

                int newAmount = currentItem.getAmount() + cursor.getAmount();

                if (newAmount > currentItem.getType().getMaxStackSize()) {
                    cursor.setAmount(newAmount - currentItem.getType().getMaxStackSize());
                    currentItem.setAmount(currentItem.getType().getMaxStackSize());
                    inventory.setItem(slot, currentItem);
                }
                else {
                    currentItem.setAmount(newAmount);
                    cursor.setAmount(0);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            clickEvent.getPlayer().getOpenInventory().setCursor(cursor);
                        }
                    }.runTask(SkylaskInvsee.getInstance());
                    inventory.setItem(slot, currentItem);
                    clickEvent.setCancelled(true);
                }
                return;
            }

            // swap items or replace
            inventory.setItem(slot, cursor);
        });

        return button;
    }

    private void handleDrag(ClickEvent clickEvent, InventoryDragEvent event, Button button) {
        InventoryView inventoryView = event.getView();

        // only in his inventory
        if (!isInTopInv(
                event.getRawSlots().stream()
                        .mapToInt(Integer::intValue)
                        .min()
                        .orElse(-1) // outside the inventory is this for example
                , inventoryView)) {
            clickEvent.setCancelled(false);
            return;
        }

        // any click in a blocked slot
        boolean cancel = false;
        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot >= 5 && rawSlot <= 8) {     // offhand and crafting
                cancel = true;
            }
        }
        if (cancel) {
            clickEvent.setCancelled(true);
            return;
        }

        for (Entry<Integer, ItemStack> entry : event.getNewItems().entrySet()) {
            ItemStack current = inventoryView.getItem(entry.getKey());

            if (current == null || current.getType() == Material.AIR) {
                inventory.setItem(translateSlot(entry.getKey()), entry.getValue());
                continue;
            }

            if (current.isSimilar(entry.getValue())) {
                int totalAmount = current.getAmount() + entry.getValue().getAmount();
                if (totalAmount < current.getType().getMaxStackSize()) {
                    inventory.setItem(translateSlot(entry.getKey()),
                            ItemFactory.builder(current).setAmount(totalAmount).build()
                    );
                }
            }
        }
        clickEvent.setCancelled(false);
    }

    private int translateSlot(int slot) {
        if (slot < 4) {
            return 39 - slot;   // armor
        }
        else if (slot == 4) {
            return 40;  // offhand
        }
        else if (slot < 36) {
            return slot;    // inventory
        }
        else if (slot < 54) {
            return slot - 36;   // hotbar
        }
        return slot;
    }

    private boolean isInTopInv(int rawSlot, InventoryView view) {
        return rawSlot < view.getTopInventory().getSize();
    }

    /**
     * Checks if it is a simple click
     *
     * @param event The {@link InventoryClickEvent} to check
     *
     * @return True if it is a simple click
     */
    static boolean isSimpleClick(InventoryClickEvent event) {
        ClickType type = event.getClick();
        return (event instanceof DragClickEvent) || type == ClickType.LEFT || type == ClickType.RIGHT
                || type == ClickType.MIDDLE;
    }

    /**
     * Clears this pane
     */
    void clear() {
        Collection<Component> children = new ArrayList<>(getChildren());
        children.forEach(this::removeComponent);
    }

    /**
     * @param itemStack The {@link ItemStack} to check
     *
     * @return True if the item is null or AIR
     */
    boolean isItemEmpty(ItemStack itemStack) {
        return itemStack == null || itemStack.getType() == Material.AIR;
    }
}
