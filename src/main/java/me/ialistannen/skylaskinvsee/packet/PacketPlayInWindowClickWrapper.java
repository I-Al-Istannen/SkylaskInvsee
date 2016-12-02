package me.ialistannen.skylaskinvsee.packet;

import static com.perceivedev.perceivecore.reflection.ReflectionUtil.NameSpace.NMS;

import java.lang.reflect.Field;
import java.util.Objects;

import com.perceivedev.perceivecore.reflection.ReflectionUtil;
import com.perceivedev.perceivecore.reflection.ReflectionUtil.FieldPredicate;

/**
 * A Wrapper for the NMS PacketPlayInWindowClick packet
 */
class PacketPlayInWindowClickWrapper {

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    static final Class<?> PACKET_CLASS = ReflectionUtil.getClass(NMS, "PacketPlayInWindowClick").get();

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private static final Class<?> INVENTORY_CLICK_TYPE = ReflectionUtil.getClass(NMS, "InventoryClickType").get();

    private static final Field BUTTON_FIELD     = ReflectionUtil.getField(PACKET_CLASS, new FieldPredicate().withName("button")).getValue();
    private static final Field SLOT_FIELD       = ReflectionUtil.getField(PACKET_CLASS, new FieldPredicate().withName("slot")).getValue();
    private static final Field CLICK_TYPE_FIELD = ReflectionUtil.getField(PACKET_CLASS, new FieldPredicate(INVENTORY_CLICK_TYPE)).getValue();

    private int slot, button;
    private Enum<?> clickType;

    /**
     * @param packet The packet object
     */
    PacketPlayInWindowClickWrapper(Object packet) {
        Objects.requireNonNull(packet, "packet can not be null!");

        if (!packet.getClass().equals(PACKET_CLASS)) {
            throw new ClassCastException("Packet is not of type " + PACKET_CLASS.getName() + " but " + packet.getClass());
        }

        slot = (int) ReflectionUtil.getFieldValue(SLOT_FIELD, packet).getValue();
        button = (int) ReflectionUtil.getFieldValue(BUTTON_FIELD, packet).getValue();
        clickType = (Enum<?>) ReflectionUtil.getFieldValue(CLICK_TYPE_FIELD, packet).getValue();
    }

    /**
     * @return The button of the event.
     */
    int getButton() {
        return button;
    }

    /**
     * @return The slot of the event. -999 for outside
     */
    int getSlot() {
        return slot;
    }

    /**
     * @return The click type
     */
    Enum<?> getClickType() {
        return clickType;
    }
}
