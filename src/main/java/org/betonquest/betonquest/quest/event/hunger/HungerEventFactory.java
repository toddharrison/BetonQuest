package org.betonquest.betonquest.quest.event.hunger;

import org.betonquest.betonquest.Instruction;
import org.betonquest.betonquest.api.quest.event.Event;
import org.betonquest.betonquest.api.quest.event.EventFactory;
import org.betonquest.betonquest.exceptions.InstructionParseException;
import org.betonquest.betonquest.quest.event.OnlineProfileRequiredEvent;
import org.betonquest.betonquest.quest.event.PrimaryServerThreadEvent;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.Locale;

/**
 * Factory for the hunger event.
 */
public class HungerEventFactory implements EventFactory {
    /**
     * Server to use for syncing to the primary server thread.
     */
    private final Server server;
    /**
     * Scheduler to use for syncing to the primary server thread.
     */
    private final BukkitScheduler scheduler;
    /**
     * Plugin to use for syncing to the primary server thread.
     */
    private final Plugin plugin;

    /**
     * Create the hunger event factory.
     *
     * @param server    server to use
     * @param scheduler scheduler to use
     * @param plugin    plugin to use
     */
    public HungerEventFactory(final Server server, final BukkitScheduler scheduler, final Plugin plugin) {
        this.server = server;
        this.scheduler = scheduler;
        this.plugin = plugin;
    }

    @Override
    public Event parseEvent(final Instruction instruction) throws InstructionParseException {
        try {
            final Hunger hunger = Hunger.valueOf(instruction.next().toUpperCase(Locale.ROOT).trim());
            final int amount = instruction.getInt();
            return new PrimaryServerThreadEvent(
                    new OnlineProfileRequiredEvent(
                            new HungerEvent(hunger, amount), instruction.getPackage()
                    ), server, scheduler, plugin);
        } catch (final IllegalArgumentException e) {
            throw new InstructionParseException("Error while parsing action! Must be 'set', 'give', or 'take'.", e);
        }
    }
}
