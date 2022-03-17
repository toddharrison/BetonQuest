package org.betonquest.betonquest.objectives;

import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.Instruction;
import org.betonquest.betonquest.api.Objective;
import org.betonquest.betonquest.exceptions.InstructionParseException;
import org.betonquest.betonquest.utils.PlayerConverter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;

import java.util.Locale;
import java.util.function.Function;

/**
 * Player needs to get specified experience level.
 */
@SuppressWarnings("PMD.CommentRequired")
public class ExperienceObjective extends Objective implements Listener {

    private final int amount;
    private final String notifyMessageName;
    private final Function<Player, Integer> toData;
    private final Listener eventListener;

    public ExperienceObjective(final Instruction instruction) throws InstructionParseException {
        super(instruction);
        this.amount = instruction.getInt();
        if (amount <= 0) {
            throw new InstructionParseException("Amount cannot be less than 1");
        }
        if (instruction.hasArgument("level")) {
            notifyMessageName = "level_to_gain";
            toData = Player::getLevel;
            eventListener = new LevelChangeListener();
        } else {
            notifyMessageName = "experience_to_gain";
            toData = Player::getTotalExperience;
            eventListener = new ExperienceChangeListener();
        }
    }

    private void onExperienceChange(final OfflinePlayer player, final int newAmount, final int oldAmount) {
        final String playerID = PlayerConverter.getID(player);
        if (containsPlayer(playerID)) {
            if (newAmount >= amount && checkConditions(playerID)) {
                completeObjective(playerID);
            } else if (notify && (amount - newAmount) / notifyInterval != (amount - oldAmount) / notifyInterval) {
                sendNotify(playerID, notifyMessageName, amount - newAmount);
            }
        }
    }

    @Override
    public void start() {
        Bukkit.getPluginManager().registerEvents(eventListener, BetonQuest.getInstance());
    }

    @Override
    public void stop() {
        HandlerList.unregisterAll(eventListener);
    }

    @Override
    public String getDefaultDataInstruction() {
        return "";
    }

    @Override
    public String getProperty(final String name, final String playerID) {
        switch (name.toLowerCase(Locale.ROOT)) {
            case "amount":
                return PlayerConverter.getOptionalPlayer(playerID)
                        .map(toData)
                        .map(String::valueOf)
                        .orElse("");
            case "left":
                return PlayerConverter.getOptionalPlayer(playerID)
                        .map(toData)
                        .map(exp -> amount - exp)
                        .map(String::valueOf)
                        .orElse("");
            case "total":
                return Integer.toString(amount);
            default:
                return "";
        }
    }

    private class LevelChangeListener implements Listener {
        public LevelChangeListener() {
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onLevelChangeEvent(final PlayerLevelChangeEvent event) {
            onExperienceChange(event.getPlayer(), event.getNewLevel(), event.getOldLevel());
        }
    }

    private class ExperienceChangeListener implements Listener {
        public ExperienceChangeListener() {
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onExpChangeEvent(final PlayerExpChangeEvent event) {
            final int oldExperience = event.getPlayer().getTotalExperience();
            onExperienceChange(event.getPlayer(), oldExperience + event.getAmount(), oldExperience);
        }
    }
}
