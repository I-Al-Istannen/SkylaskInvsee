package me.ialistannen.skylaskinvsee.gui;

import java.util.Objects;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import com.perceivedev.perceivecore.gui.Gui;
import com.perceivedev.perceivecore.gui.components.panes.AnchorPane;

import me.ialistannen.skylaskinvsee.SkylaskInvsee;
import me.ialistannen.skylaskinvsee.manager.WatchedPlayers;

/**
 * A Gui for watching a Player
 */
public class WatchGui extends Gui {

    private static int counter;

    private final int ID = counter++;

    private UUID    targetPlayer;
    private boolean modifiable;

    public WatchGui(String name, Player target, boolean modifiable) {
        super(name, 5, new AnchorPane(9, 5));

        targetPlayer = target.getUniqueId();

        PlayerInventoryPane pane = new PlayerInventoryPane(targetPlayer, target.getInventory());
        getRootAsFixedPosition().addComponent(pane, 0, 1);

        PlayerArmorOffhandPane miscPane = new PlayerArmorOffhandPane(targetPlayer, target.getInventory());
        getRootAsFixedPosition().addComponent(miscPane, 0, 0);

        PlayerCraftingPane craftingPane = new PlayerCraftingPane(targetPlayer, target.getOpenInventory().getTopInventory());
        getRootAsFixedPosition().addComponent(craftingPane, 5, 0);
    }

    @Override
    protected void onClose() {
        getPlayerID().ifPresent(watcherID -> {
            WatchedPlayers watchedPlayers = SkylaskInvsee.getInstance().getWatchedPlayers();
            watchedPlayers.removeWatcher(targetPlayer, watcherID);

            SkylaskInvsee.getInstance().getWatchGuiManager().removeWatchGui(watcherID);
        });
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        if (!modifiable) {
            event.setCancelled(true);
            return;
        }
        super.onClick(event);

        // Allow the player to modify his own inventory, as long as it are just simple clicks :)
        if (event.isCancelled() && PlayerInventoryPartPane.isSimpleClick(event)) {
            int slot = event.getRawSlot();
            if (slot > event.getInventory().getSize()) {
                event.setCancelled(false);
            }
        }
    }

    /**
     * Updates the view for the player, if the target is present
     * <p>
     * If the target is offline, it closes it
     */
    public void updateViewIfTargetOnline() {
        Player player = Bukkit.getPlayer(targetPlayer);
        if (player == null) {
            close();
            return;
        }

        reRender();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof WatchGui))
            return false;
        if (!super.equals(o))
            return false;
        WatchGui watchGui = (WatchGui) o;
        return ID == watchGui.ID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), ID);
    }
}