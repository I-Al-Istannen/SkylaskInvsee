package me.ialistannen.skylaskinvsee.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import me.ialistannen.skylaskinvsee.SkylaskInvsee;
import me.ialistannen.skylaskinvsee.gui.WatchGui;

/**
 * Manages the {@link WatchGui}s
 */
public class WatchGuiManager implements Listener {

    private Map<UUID, WatchGui> watchGuiMap = new HashMap<>();

    {
        Bukkit.getPluginManager().registerEvents(this, SkylaskInvsee.getInstance());
    }

    /**
     * Adds a {@link WatchGui} to this manager
     *
     * @param watcher The {@link UUID} of the watching player
     * @param watchGui The {@link WatchGui} to add
     */
    public void addWatchGui(UUID watcher, WatchGui watchGui) {
        watchGuiMap.put(watcher, watchGui);
    }

    /**
     * Removes the {@link WatchGui} for the given watcher
     *
     * @param watcher The {@link UUID} of the watching player
     */
    public void removeWatchGui(UUID watcher) {
        watchGuiMap.remove(watcher);
    }

    /**
     * Closes all Guis
     */
    public void closeAll() {
        List<Player> players = watchGuiMap.keySet()
                  .stream()
                  .map(Bukkit::getPlayer)
                  .filter(Objects::nonNull)
                  .collect(Collectors.toList());

        players.forEach(HumanEntity::closeInventory);
    }

    /**
     * Updates the {@link WatchGui} for the given player
     *
     * @param watcher The {@link UUID} of the watching player
     */
    private void updateWatchGui(UUID watcher) {
        if (watchGuiMap.containsKey(watcher)) {
            watchGuiMap.get(watcher).updateViewIfTargetOnline();
        }
    }

    //<editor-fold desc="Target update events">
    // +============================================================+
    // *                                                            *
    // *                    TARGET UPDATE EVENTS                    *
    // *                                                            *
    // +============================================================+

    @EventHandler
    public void onTargetUpdateInventory(InventoryClickEvent event) {
        WatchedPlayers players = SkylaskInvsee.getInstance().getWatchedPlayers();

        if (!isPlayerAndWatched(event.getWhoClicked(), players)) {
            return;
        }

        UUID playerUUID = event.getWhoClicked().getUniqueId();

        new BukkitRunnable() {
            @Override
            public void run() {
                players.getWatcher(playerUUID).forEach(WatchGuiManager.this::updateWatchGui);
            }
        }.runTask(SkylaskInvsee.getInstance());
    }
    
    @EventHandler
    public void onTargetEquipArmor(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) {
            return;
        }

        if (!event.hasItem()) {
            return;
        }

        WatchedPlayers players = SkylaskInvsee.getInstance().getWatchedPlayers();

        if (!isPlayerAndWatched(event.getPlayer(), players)) {
            return;
        }

        Material type = event.getItem().getType();
        String typeName = type.name();

        if (typeName.contains("CHESTPLATE") || typeName.contains("LEGGINGS") || typeName.contains("HELMET") || typeName.contains("BOOTS")) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    players.getWatcher(event.getPlayer().getUniqueId()).forEach(WatchGuiManager.this::updateWatchGui);
                }
            }.runTask(SkylaskInvsee.getInstance());
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        WatchedPlayers watchedPlayers = SkylaskInvsee.getInstance().getWatchedPlayers();

        if (!isPlayerAndWatched(event.getPlayer(), watchedPlayers)) {
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                watchedPlayers.getWatcher(event.getPlayer().getUniqueId())
                          .forEach(WatchGuiManager.this::updateWatchGui);
            }
        }.runTask(SkylaskInvsee.getInstance());
    }

    /**
     * Checks if a {@link HumanEntity} is a {@link Player} and being watched
     *
     * @param entity The {@link HumanEntity} to check
     * @param watchedPlayers The {@link WatchedPlayers} to use
     *
     * @return True if the {@link HumanEntity} is a {@link Player} and being watched
     */
    private boolean isPlayerAndWatched(HumanEntity entity, WatchedPlayers watchedPlayers) {
        return entity instanceof Player && watchedPlayers.isBeingWatched(entity.getUniqueId());
    }
    //</editor-fold>
}
