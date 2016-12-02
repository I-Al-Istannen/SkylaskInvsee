package me.ialistannen.skylaskinvsee.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import me.ialistannen.skylaskinvsee.util.Util;

/**
 * A class that keeps track on who watches who
 */
public class WatchedPlayers {

    private Map<UUID, UUID>      watcherTargetMap = new HashMap<>();
    private Multimap<UUID, UUID> targetWatcherMap = HashMultimap.create();

    /**
     * Adds a watching Player
     *
     * @param target The player who is watched
     * @param watcher The player who is watching
     */
    public void addWatcher(UUID target, UUID watcher) {
        watcherTargetMap.put(watcher, target);
        targetWatcherMap.put(target, watcher);
    }

    /**
     * Removes a watching player
     *
     * @param target The player who is watched
     * @param watcher The player who is watching
     */
    public void removeWatcher(UUID target, UUID watcher) {
        watcherTargetMap.remove(watcher);
        targetWatcherMap.remove(target, watcher);

        Optional.ofNullable(Bukkit.getPlayer(watcher)).ifPresent(player -> {
            Player targetPlayer = Bukkit.getPlayer(target);

            String targetName = Util.trWithPrefix("general.target.not.online");
            if (targetPlayer != null) {
                targetName = targetPlayer.getDisplayName();
            }
            {
                // do not send the message if it is an empty one
                if (!Util.tr("general.status.stopped.watching", targetName).trim().isEmpty()) {
                    player.sendMessage(Util.trWithPrefix("general.status.stopped.watching", targetName));
                }
            }
        });
    }

    /**
     * @param target The player who is watched
     *
     * @return True if this player is being watched
     */
    public boolean isBeingWatched(UUID target) {
        return targetWatcherMap.containsKey(target);
    }

    /**
     * @param target The player who is watched
     *
     * @return All watcher for the given Player
     */
    Collection<UUID> getWatcher(UUID target) {
        return targetWatcherMap.get(target);
    }

    /**
     * @param watcher The player who is watching
     *
     * @return The {@link UUID} of the player who is being watched
     */
    public UUID getTarget(UUID watcher) {
        return watcherTargetMap.get(watcher);
    }

    /**
     * @param watcher The player who is watching
     *
     * @return True if this player is watching another
     */
    public boolean isWatchingAPlayer(UUID watcher) {
        return watcherTargetMap.containsKey(watcher);
    }
}
