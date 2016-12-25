package me.ialistannen.skylaskinvsee.event;

import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.scheduler.BukkitRunnable;

import me.ialistannen.skylaskinvsee.SkylaskInvsee;
import me.ialistannen.skylaskinvsee.manager.WatchGuiManager;
import me.ialistannen.skylaskinvsee.manager.WatchedPlayers;

/**
 * Listens for misc player events
 */
public class MiscPlayerListener implements Listener {

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        updateWatcherIfNeeded(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        updateWatcherIfNeeded(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        updateWatcherIfNeeded(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onItemPickup(PlayerPickupItemEvent event) {
        updateWatcherIfNeeded(event.getPlayer().getUniqueId());
    }

    private void updateWatcherIfNeeded(UUID target) {
        WatchedPlayers watchedPlayers = SkylaskInvsee.getInstance().getWatchedPlayers();
        if (!watchedPlayers.isBeingWatched(target)) {
            return;
        }

        // Update Guis, as items were dropped!
        new BukkitRunnable() {

            @Override
            public void run() {
                WatchGuiManager watchGuiManager = SkylaskInvsee.getInstance().getWatchGuiManager();

                for (UUID uuid : watchedPlayers.getWatcher(target)) {
                    watchGuiManager.updateWatchGui(uuid);
                }
            }
        }.runTask(SkylaskInvsee.getInstance());
    }
}
