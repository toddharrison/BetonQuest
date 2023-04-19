package org.betonquest.betonquest.objectives;

import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.Instruction;
import org.betonquest.betonquest.api.CountingObjective;
import org.betonquest.betonquest.api.profiles.OnlineProfile;
import org.betonquest.betonquest.exceptions.InstructionParseException;
import org.betonquest.betonquest.utils.PlayerConverter;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTameEvent;

/**
 * The player must tame specified amount of specified mobs.
 */
@SuppressWarnings("PMD.CommentRequired")
public class TameObjective extends CountingObjective implements Listener {

    private final EntityType type;

    public TameObjective(final Instruction instruction) throws InstructionParseException {
        super(instruction, "animals_to_tame");

        type = instruction.getEntity();
        if (type.getEntityClass() == null || !Tameable.class.isAssignableFrom(type.getEntityClass())) {
            throw new InstructionParseException("Entity cannot be tamed: " + type);
        }

        targetAmount = instruction.getVarNum();
        preCheckAmountNotLessThanOne(targetAmount);
    }

    @EventHandler(ignoreCancelled = true)
    public void onTaming(final EntityTameEvent event) {
        if (event.getOwner() instanceof Player) {
            final OnlineProfile onlineProfile = PlayerConverter.getID((Player) event.getOwner());
            if (containsPlayer(onlineProfile)
                    && type.equals(event.getEntity().getType())
                    && checkConditions(onlineProfile)) {
                getCountingData(onlineProfile).progress();
                completeIfDoneOrNotify(onlineProfile);
            }
        }
    }

    @Override
    public void start() {
        Bukkit.getPluginManager().registerEvents(this, BetonQuest.getInstance());
    }

    @Override
    public void stop() {
        HandlerList.unregisterAll(this);
    }
}
