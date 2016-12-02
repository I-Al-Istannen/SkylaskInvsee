package me.ialistannen.skylaskinvsee.packet;

import java.util.Collection;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.perceivedev.perceivecore.packet.PacketListener;
import com.perceivedev.perceivecore.packet.PacketManager;

/**
 * Injects the Packet listener to players
 */
public class PacketInjectListener implements Listener {

    private PacketListener listener = new ItemDragPacketListener();

    //<editor-fold desc="Inject methods">
    // +================================================================+
    // *                                                                *
    // *                         INJECT METHODS                         *
    // *                                                                *
    // +================================================================+

    /**
     * Injects the listener in the Player
     *
     * @param player The {@link Player} to inject the listener for
     */
    public void inject(Player player) {
        PacketManager.getInstance().addListener(listener, player);
    }

    /**
     * Injects the listener in the Players
     *
     * @param players The {@link Player}s to inject the listener for
     *
     * @see #inject(Player)
     */
    public void injectAll(Collection<? extends Player> players) {
        players.forEach(this::inject);
    }

    /**
     * Removes the listener for the Player
     *
     * @param player The {@link Player} to un-inject the listener for
     */
    public void unInject(Player player) {
        PacketManager.getInstance().removeListener(listener, player);
    }

    /**
     * Removes the listener for the Players
     *
     * @param players The {@link Player}s to un-inject the listener for
     *
     * @see #inject(Player)
     */
    public void unInjectAll(Collection<? extends Player> players) {
        players.forEach(this::unInject);
    }
    //</editor-fold>

    //<editor-fold desc="Listener">
    // +================================================================+
    // *                                                                *
    // *                            LISTENER                            *
    // *                                                                *
    // +================================================================+

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        inject(event.getPlayer());
    }
    //</editor-fold>
}
