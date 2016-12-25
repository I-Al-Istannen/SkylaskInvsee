package me.ialistannen.skylaskinvsee.event;

import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;

/**
 * An inventory drag event, wrapped in an InventoryClickEvent
 */
public class DragClickEvent extends InventoryClickEvent {

    private InventoryDragEvent dragEvent;

    /**
     * @param dragEvent The {@link InventoryDragEvent}
     */
    DragClickEvent(InventoryDragEvent dragEvent) {
        super(dragEvent.getView(), SlotType.CONTAINER, 0, ClickType.UNKNOWN, InventoryAction.PLACE_ALL);
        this.dragEvent = dragEvent;
    }

    /**
     * @return The wrapped Drag event
     */
    public InventoryDragEvent getDragEvent() {
        return dragEvent;
    }

    @Override
    public void setCancelled(boolean toCancel) {
        dragEvent.setCancelled(toCancel);
    }

    @Override
    public boolean isCancelled() {
        return dragEvent.isCancelled();
    }
}
