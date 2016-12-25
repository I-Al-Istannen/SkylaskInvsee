package me.ialistannen.skylaskinvsee.event;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryDragEvent;

import com.perceivedev.perceivecore.gui.Gui;

import me.ialistannen.skylaskinvsee.SkylaskInvsee;
import me.ialistannen.skylaskinvsee.manager.WatchedPlayers;

/**
 * Injects the Packet listener to players
 */
public class DragListener implements Listener {

    //<editor-fold desc="Listener">
    // +================================================================+
    // *                                                                *
    // *                            LISTENER                            *
    // *                                                                *
    // +================================================================+

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        WatchedPlayers watchedPlayers = SkylaskInvsee.getInstance().getWatchedPlayers();

        if (!watchedPlayers.isWatchingAPlayer(event.getWhoClicked().getUniqueId())) {
            return;
        }

        if (!(event.getInventory().getHolder() instanceof Gui)) {
            return;
        }

        Gui gui = (Gui) event.getInventory().getHolder();
        gui.onClick(new DragClickEvent(event));
    }

    //</editor-fold>
}
