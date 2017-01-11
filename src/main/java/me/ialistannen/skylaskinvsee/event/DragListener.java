package me.ialistannen.skylaskinvsee.event;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryDragEvent;

import com.perceivedev.perceivecore.gui.Gui;

import me.ialistannen.skylaskinvsee.SkylaskInvsee;
import me.ialistannen.skylaskinvsee.manager.WatchedPlayers;
import me.ialistannen.skylaskinvsee.util.Util;

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

        // do not drag between inventories
        boolean isInTopInv = Util.isInTopInv(event.getRawSlots().iterator().next(), event.getView());
        for (Integer integer : event.getRawSlots()) {
            if (!isInTopInv && Util.isInTopInv(integer, event.getView())) {
                event.setCancelled(true);
                return;
            }
            else if (isInTopInv && !Util.isInTopInv(integer, event.getView())) {
                event.setCancelled(true);
                return;
            }
        }

        Gui gui = (Gui) event.getInventory().getHolder();
        gui.onClick(new DragClickEvent(event));
    }

    //</editor-fold>
}
