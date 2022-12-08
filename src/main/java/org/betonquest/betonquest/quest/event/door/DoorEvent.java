package org.betonquest.betonquest.quest.event.door;

import org.betonquest.betonquest.api.profiles.Profile;
import org.betonquest.betonquest.api.quest.event.Event;
import org.betonquest.betonquest.exceptions.QuestRuntimeException;
import org.betonquest.betonquest.utils.location.CompoundLocation;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Openable;

import java.util.function.Consumer;

/**
 * The door event. It applies an action to the {@link Openable} block meta if it could be found at the location.
 * It fails when no {@link Openable} block is found at the location.
 */
public class DoorEvent implements Event {

    /**
     * The {@link Openable}'s location.
     */
    private final CompoundLocation location;

    /**
     * The action to do to the {@link Openable}.
     */
    private final Consumer<Openable> action;

    /**
     * Create the event to change the {@link Openable} at the given location with the given action.
     *
     * @param location location to act on
     * @param action action to do
     */
    public DoorEvent(final CompoundLocation location, final Consumer<Openable> action) {
        this.location = location;
        this.action = action;
    }

    @Override
    public void execute(final Profile playerId) throws QuestRuntimeException {
        final Location resolvedLocation = location.getLocation(playerId);
        final Block block = resolvedLocation.getBlock();
        final BlockData blockData = block.getBlockData();

        if (blockData instanceof Openable) {
            action.accept((Openable) blockData);
            block.setBlockData(blockData);
        } else {
            final String message = String.format("There is no door at x: %d y: %d z: %d in world '%s'.",
                    resolvedLocation.getBlockX(), resolvedLocation.getBlockY(), resolvedLocation.getBlockZ(),
                    resolvedLocation.getWorld().getName());
            throw new QuestRuntimeException(message);
        }
    }
}
