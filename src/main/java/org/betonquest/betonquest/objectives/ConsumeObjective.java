package org.betonquest.betonquest.objectives;

import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.Instruction;
import org.betonquest.betonquest.api.CountingObjective;
import org.betonquest.betonquest.api.profiles.OnlineProfile;
import org.betonquest.betonquest.exceptions.InstructionParseException;
import org.betonquest.betonquest.item.QuestItem;
import org.betonquest.betonquest.utils.PlayerConverter;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;

/**
 * Requires the player to consume an item (eat food or drink a potion).
 */
@SuppressWarnings("PMD.CommentRequired")
public class ConsumeObjective extends CountingObjective implements Listener {

    /**
     * The name of the argument that determines the amount of items to consume.
     */
    public static final String AMOUNT_ARGUMENT = "amount";

    /**
     * The item to consume.
     */
    private final QuestItem item;

    /**
     * Constructs a new {@code ConsumeObjective} for the given {@code Instruction}.
     *
     * @param instruction the instruction out of a quest package
     * @throws InstructionParseException if the instruction is invalid
     */
    public ConsumeObjective(final Instruction instruction) throws InstructionParseException {
        super(instruction);
        item = instruction.getQuestItem();
        targetAmount = instruction.getVarNum(instruction.getOptional(AMOUNT_ARGUMENT, "1"));
        preCheckAmountNotLessThanOne(targetAmount);
    }


    /**
     * The listener that handles a consumed item.
     *
     * @param event the Bukkit event for consuming an item
     */
    @EventHandler(ignoreCancelled = true)
    public void onConsume(final PlayerItemConsumeEvent event) {
        final OnlineProfile onlineProfile = PlayerConverter.getID(event.getPlayer());
        if (containsPlayer(onlineProfile) && item.compare(event.getItem()) && checkConditions(onlineProfile)) {
            getCountingData(onlineProfile).progress();
            completeIfDoneOrNotify(onlineProfile);
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
