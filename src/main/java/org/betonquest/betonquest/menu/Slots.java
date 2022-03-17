package org.betonquest.betonquest.menu;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility object that handles which items are assigned to which slots
 */
@SuppressWarnings("PMD.CommentRequired")
public class Slots {

    private final int start;
    private final int end;
    private final List<MenuItem> items;
    private final Type type;

    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.AvoidUncheckedExceptionsInSignatures"})
    public Slots(final String slots, final List<MenuItem> items) throws IllegalArgumentException {
        if (slots.matches("\\d+")) {
            this.type = Type.SINGLE;
            this.start = Integer.parseInt(slots);
            this.end = start;
        } else if (slots.matches("\\d+-\\d+")) {
            this.type = Type.ROW;
            final int index = slots.indexOf('-');
            this.start = Integer.parseInt(slots.substring(0, index));
            this.end = Integer.parseInt(slots.substring(index + 1));
            if (this.end < this.start) {
                throw new IllegalArgumentException(slots + ": slot " + end + " must be after " + start);
            }
        } else if (slots.matches("\\d+\\*\\d+")) {
            this.type = Type.RECTANGLE;
            final int index = slots.indexOf('*');
            this.start = Integer.parseInt(slots.substring(0, index));
            this.end = Integer.parseInt(slots.substring(index + 1));
            if (this.end < this.start) {
                throw new IllegalArgumentException(slots + ": slot " + end + " must be after " + start);
            }
            if ((start % 9) > (end % 9)) {
                throw new IllegalArgumentException(slots + ": invalid rectangle ");
            }
        } else {
            throw new IllegalArgumentException(slots + " is not a valid slot identifier");
        }
        this.items = items;
    }

    /**
     * checks if all defined slots are valid
     *
     * @param slots         a iterable containing all slots objects to check
     * @param inventorySize the size of the inventory in which the slots should be
     * @throws SlotException if a defined list of slots is invalid
     */
    @SuppressWarnings("PMD.PreserveStackTrace")
    public static void checkSlots(final Iterable<Slots> slots, final int inventorySize) throws SlotException {
        final boolean[] isContained = new boolean[inventorySize]; //initialized with 'false'
        for (final Slots s : slots) {
            for (final int slot : s.getSlots()) {
                try {
                    if (isContained[slot]) {
                        throw new SlotException(slot, s.toString(), "slot " + slot + " was already specified");
                    } else {
                        isContained[slot] = true;
                    }
                } catch (final IndexOutOfBoundsException e) {
                    throw new SlotException(slot, s.toString(), "slot " + slot + " exceeds inventory size");
                }
            }
        }
    }

    /**
     * @return a sorted list of all slots which are covered by this slots object
     */
    public List<Integer> getSlots() {
        final List<Integer> slots = new ArrayList<>();
        switch (type) {
            case SINGLE:
                slots.add(start);
                break;
            case ROW:
                for (int i = start; i <= end; i++) {
                    slots.add(i);
                }
                break;
            case RECTANGLE:
                int index = start;
                while (index <= end) {
                    slots.add(index);
                    //set i to next slot of rectangle
                    if ((index % 9) < (end % 9)) {
                        index++;
                    } else {
                        index += 8 - (index % 9) + (start % 9) + 1;
                    }
                }
                break;
        }
        return slots;
    }

    /**
     * @param slot the slot to check for
     * @return if this slots object covers the given slot
     */
    public boolean containsSlot(final int slot) {
        switch (type) {
            case SINGLE:
                return start == slot;
            case ROW:
                return slot <= end && slot >= start;
            case RECTANGLE:
                return slot <= end && slot >= start
                        && slot % 9 >= start % 9
                        && slot % 9 <= end % 9;
            default:
                return false;
        }
    }

    /**
     * @return all items assigned to the slots covered by this object
     */
    public List<MenuItem> getItems() {
        return items;
    }

    /**
     * @param player the player for which these slots should get displayed for
     * @return all items which should be shown to the specified player of the slots covered by this object
     */
    public List<MenuItem> getItems(final Player player) {
        final List<MenuItem> items = new ArrayList<>();
        for (final MenuItem item : this.items) {
            if (item.display(player)) {
                items.add(item);
            }
        }
        return items;
    }

    /**
     * @param slot the index of the slot in the menu
     * @return the index of the given slot within this collection of slots, -1 if slot is not within this collection
     */
    public int getIndex(final int slot) {
        if (!containsSlot(slot)) {
            return -1;
        }
        switch (type) {
            case SINGLE:
                return 0;
            case ROW:
                return slot - start;
            case RECTANGLE:
                final int rectangleLength = end % 9 - start % 9 + 1;
                final int rows = slot / 9 - start / 9;
                return rectangleLength * rows + slot % 9 - start % 9;
            default:
                return -1;
        }
    }

    /**
     * @param player the player for which these slots should get displayed for
     * @param slot   the slot which should contain this item
     * @return the menu item which should be displayed in the given slot to the player
     */
    @SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
    public MenuItem getItem(final Player player, final int slot) {
        final int index = this.getIndex(slot);
        if (index == -1) {
            throw new RuntimeException("Invalid slot for Slots '" + this + "': " + slot);
        }
        try {
            return this.getItems(player).get(index);
        } catch (final IndexOutOfBoundsException e) {
            return null;
        }
    }

    /**
     * @return the type of this slots object
     */
    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        switch (type) {
            case SINGLE:
                return String.valueOf(start);
            case ROW:
                return start + "-" + end;
            case RECTANGLE:
                return start + "*" + end;
            default:
                return super.toString();
        }
    }

    public enum Type {
        /**
         * A single slot
         */
        SINGLE,

        /**
         * Multiple slots ordered in a row, one behind each other
         */
        ROW,

        /**
         * Multiple slots ordered in a rectangle
         */
        RECTANGLE
    }

    public static class SlotException extends Exception {

        private static final long serialVersionUID = 2796975671139425046L;
        private final int slot;
        private final String slots;

        public SlotException(final int slot, final String slots, final String message) {
            super(message);
            this.slots = slots;
            this.slot = slot;
        }

        public int getSlot() {
            return slot;
        }

        public String getSlots() {
            return slots;
        }
    }
}
