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
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

import me.ialistannen.bukkitutilities.gui.ClickEvent;
import me.ialistannen.bukkitutilities.gui.base.Component;
import me.ialistannen.bukkitutilities.gui.components.Button;
import me.ialistannen.bukkitutilities.gui.components.Label;
import me.ialistannen.bukkitutilities.gui.components.panes.AnchorPane;
import me.ialistannen.bukkitutilities.gui.util.Dimension;
import me.ialistannen.bukkitutilities.utilities.item.ItemFactory;
import me.ialistannen.skylaskinvsee.SkylaskInvsee;
import me.ialistannen.skylaskinvsee.event.DragClickEvent;
import me.ialistannen.skylaskinvsee.util.Util;

import static me.ialistannen.skylaskinvsee.util.Util.isItemEmpty;

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

            if (!Util.isSimpleClick(raw) && !Util.isShiftClick(raw)) {
                return;
            }

            if (raw instanceof DragClickEvent) {
                handleDrag(clickEvent, ((DragClickEvent) raw).getDragEvent());
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

            // shift clicks from target to watcher!
            if (Util.isShiftClick(raw)) {
                if (isItemEmpty(currentItem)) {
                    // just do nothing
                    return;
                }
                Player player = clickEvent.getPlayer();
                PlayerInventory watcherInventory = player.getInventory();
                ItemStack[] contents = watcherInventory.getContents();
                int amountLeft = currentItem.getAmount();
                for (ItemStack stack : contents) {
                    if (isItemEmpty(stack)) {
                        continue;
                    }

                    if (stack.isSimilar(currentItem) && stack.getAmount() != stack.getType().getMaxStackSize()) {
                        int diff = stack.getType().getMaxStackSize() - stack.getAmount();
                        if (diff >= currentItem.getAmount()) {
                            amountLeft = 0;
                            break;
                        }
                        else if (currentItem.getAmount() > 0) {
                            amountLeft -= diff;
                        }
                    }
                }
                if (amountLeft <= 0) {
                    // remove the item from the target
                    inventory.setItem(slot, null);
                    // well, we placed it on some stack
                    clickEvent.setCancelled(false);
                    return;
                }
                // so, now look for a free space!
                int firstEmpty = watcherInventory.firstEmpty();
                if (firstEmpty < 0) {
                    // allow them to at least take a part
                    // clone it to not alter the currentItem, as otherwise the normal event will be bugged
                    // (The client thinks only the remainder was shifted)
                    inventory.setItem(slot, ItemFactory.builder(currentItem).setAmount(amountLeft).build());

                    clickEvent.setCancelled(false);
                    return;
                }
                // clear item from target
                inventory.setItem(slot, null);

                // allow the watcher client to be happy too and process the change
                clickEvent.setCancelled(false);
                return;
            }

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

    private void handleDrag(ClickEvent clickEvent, InventoryDragEvent event) {
        InventoryView inventoryView = event.getView();

        // only in his inventory
        if (!Util.isInTopInv(
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

    /**
     * Clears this pane
     */
    void clear() {
        Collection<Component> children = new ArrayList<>(getChildren());
        children.forEach(this::removeComponent);
    }
}
