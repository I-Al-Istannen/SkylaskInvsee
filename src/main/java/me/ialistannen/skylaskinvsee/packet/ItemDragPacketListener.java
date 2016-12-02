package me.ialistannen.skylaskinvsee.packet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftInventoryView;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;
import org.bukkit.scheduler.BukkitRunnable;

import com.perceivedev.perceivecore.packet.PacketAdapter;
import com.perceivedev.perceivecore.packet.PacketEvent;
import com.perceivedev.perceivecore.packet.PacketListener;

import me.ialistannen.skylaskinvsee.SkylaskInvsee;
import me.ialistannen.skylaskinvsee.gui.BetterClickEvent;
import me.ialistannen.skylaskinvsee.gui.BetterClickEvent.BetterClickType;

/**
 * A {@link PacketListener} trying to correctly identify and handle the DRAG packet
 */
class ItemDragPacketListener extends PacketAdapter {

    private Map<UUID, PlayerClickObject> playerClickMap = new HashMap<>();

    @Override
    public void onPacketReceived(PacketEvent packetEvent) {
        if (!packetEvent.getPacket().getPacketClass().equals(PacketPlayInWindowClickWrapper.PACKET_CLASS)) {
            return;
        }

        Player target = packetEvent.getPlayer();

        PacketPlayInWindowClickWrapper clickPacket = new PacketPlayInWindowClickWrapper(packetEvent.getPacket().getNMSPacket());

        if (!clickPacket.getClickType().name().equals("QUICK_CRAFT")) {
            return;
        }

        if (!SkylaskInvsee.getInstance().getWatchedPlayers().isBeingWatched(target.getUniqueId())) {
            packetEvent.setCancelled(true);

            // We do not want to be on the Netty thread, my friend!
            // I do not even want to imagine the bugs this might cause
            new BukkitRunnable() {
                // I know :(
                @SuppressWarnings("deprecation")
                @Override
                public void run() {
                    target.updateInventory();
                }
            }.runTask(SkylaskInvsee.getInstance());
            return;
        }

        PlayerClickObject playerClickObject = playerClickMap.get(target.getUniqueId());

        int button = clickPacket.getButton();
        int slot = clickPacket.getSlot();

        if (playerClickObject == null) {
            playerClickObject = new PlayerClickObject();
            playerClickMap.put(target.getUniqueId(), playerClickObject);
            return;
        }

        if (playerClickObject.getClickType() == null) {
            playerClickObject.setClickType(button);
        }

        if (slot == -999) {
            playerClickMap.remove(target.getUniqueId());
            dispatchEvents(playerClickObject, target);
        }

        playerClickObject.addSlot(clickPacket.getSlot());
    }

    private void dispatchEvents(PlayerClickObject clickObject, Player player) {
        BetterClickType clickType = clickObject.getClickType();

        for (int slot : clickObject.getSlots()) {
            InventoryView inventory = player.getOpenInventory();
            InventoryType.SlotType type = CraftInventoryView.getSlotType(inventory, slot);

            if (inventory.getType() != InventoryType.CRAFTING && inventory.getType() != InventoryType.CREATIVE) {
                if (slot < inventory.getTopInventory().getSize()) {
                    return;
                }
            }

            BetterClickEvent clickEvent = new BetterClickEvent(inventory, type, slot,
                      ClickType.UNKNOWN, InventoryAction.UNKNOWN,
                      clickType, clickObject.getSlots().size());

            Bukkit.getPluginManager().callEvent(clickEvent);
        }
    }

    /**
     * A helper object to keep track of things
     */
    private static class PlayerClickObject {
        private List<Integer> slots;
        private BetterClickType clickType = null;

        PlayerClickObject() {
            slots = new ArrayList<>();
        }

        void setClickType(int clickType) {
            this.clickType = clickType == 5 ? BetterClickType.DRAG_AND_SPLIT_ONE : BetterClickType.DRAG_AND_SPLIT_EQUAL;
        }

        BetterClickType getClickType() {
            return clickType;
        }

        List<Integer> getSlots() {
            return new ArrayList<>(slots);
        }

        void addSlot(int slot) {
            slots.add(slot);
        }
    }
}
