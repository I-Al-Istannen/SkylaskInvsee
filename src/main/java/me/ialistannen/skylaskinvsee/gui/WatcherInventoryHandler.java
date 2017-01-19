package me.ialistannen.skylaskinvsee.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.perceivedev.perceivecore.utilities.item.ItemFactory;

import me.ialistannen.skylaskinvsee.SkylaskInvsee;
import me.ialistannen.skylaskinvsee.util.Util;

import static me.ialistannen.skylaskinvsee.util.Util.isItemEmpty;

/**
 * A small class to deal with clicks the watcher makes in his own inventory
 */
class WatcherInventoryHandler {

    /**
     * Handles an {@link InventoryClickEvent}
     *
     * @param event The {@link InventoryClickEvent}
     * @param targetInventory The {@link Inventory} of the target {@link Player}
     */
    void onClick(InventoryClickEvent event, Inventory targetInventory) {
        if (Util.isInTopInv(event.getRawSlot(), event.getView())) {
            // we only care about events in the player inv. Just as a safeguard.
            return;
        }

        if (event.isCancelled() && Util.isSimpleClick(event)) {
            // do what you want, as long as it is a simple click
            event.setCancelled(false);
            return;
        }
        if (event.isCancelled() && Util.isShiftClick(event)) {
            handleShiftClick(event, targetInventory);
        }
    }

    private void handleShiftClick(InventoryClickEvent event, Inventory targetInventory) {
        ItemStack currentItem = event.getCurrentItem();

        // do nothing if there is no actual item. Minecraft's way.
        if (isItemEmpty(currentItem)) {
            return;
        }
        event.setCancelled(false);

        int needed = currentItem.getAmount();
        {
            boolean found = false;
            ItemStack[] contents = targetInventory.getContents();
            for (int i = 0; i < contents.length; i++) {
                ItemStack itemStack = contents[i];

                // we only care about stacks now
                if (isItemEmpty(itemStack)) {
                    continue;
                }

                // skip full stacks or of wrong type
                if (!currentItem.isSimilar(itemStack) || itemStack.getAmount() >= itemStack.getType()
                        .getMaxStackSize()) {
                    continue;
                }

                // we found a non-full stack of correct type
                int freeSpace = itemStack.getType().getMaxStackSize() - itemStack.getAmount();
                if (freeSpace >= needed) {
                    int total = needed + itemStack.getAmount();
                    // clear item from target, allow watcher to take it
                    targetInventory.setItem(
                            i,
                            ItemFactory.builder(currentItem)
                                    .setAmount(total)
                                    .build()
                    );
                    updateInv(event);
                    return;
                }
                else {
                    needed -= freeSpace;
                    targetInventory.setItem(
                            i,
                            ItemFactory.builder(itemStack)
                                    .setAmount(itemStack.getType().getMaxStackSize())
                                    .build()
                    );
                    found = true;
                }
            }

            if (needed <= 0) {
                updateInv(event);
                return;
            }
            else if (found) {
                event.setCancelled(true);
                updateInv(event);
                event.getView().getBottomInventory().setItem(
                        event.getSlot(),
                        ItemFactory.builder(currentItem)
                                .setAmount(needed)
                                .build()
                );
            }
        }

        int firstEmpty = targetInventory.firstEmpty();
        if (firstEmpty < 0) {
            event.setCancelled(true);
            return;
        }
        // just apply it to the target
        targetInventory.setItem(
                firstEmpty,
                ItemFactory.builder(currentItem)
                        .setAmount(needed)
                        .build()
        );

        updateInv(event);
    }

    private void updateInv(InventoryClickEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                SkylaskInvsee.getInstance().getWatchGuiManager().updateWatchGui(event.getWhoClicked().getUniqueId());
            }
        }.runTask(SkylaskInvsee.getInstance());
    }
}
