package org.betonquest.betonquest.quest.event.time;

import org.betonquest.betonquest.Instruction;
import org.betonquest.betonquest.api.common.worldselector.ConstantWorldSelector;
import org.betonquest.betonquest.api.common.worldselector.PlayerWorldSelector;
import org.betonquest.betonquest.api.common.worldselector.WorldSelector;
import org.betonquest.betonquest.api.quest.event.Event;
import org.betonquest.betonquest.api.quest.event.EventFactory;
import org.betonquest.betonquest.api.quest.event.StaticEvent;
import org.betonquest.betonquest.api.quest.event.StaticEventFactory;
import org.betonquest.betonquest.exceptions.InstructionParseException;
import org.betonquest.betonquest.quest.event.DoNothingStaticEvent;
import org.betonquest.betonquest.quest.event.NullStaticEventAdapter;
import org.betonquest.betonquest.quest.event.PrimaryServerThreadEvent;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;

/**
 * Factory to create time events from {@link Instruction}s.
 */
public class TimeEventFactory implements EventFactory, StaticEventFactory {
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
     * Create the time event factory.
     *
     * @param server    server to use
     * @param scheduler scheduler to use
     * @param plugin    plugin to use
     */
    public TimeEventFactory(final Server server, final BukkitScheduler scheduler, final Plugin plugin) {
        this.server = server;
        this.scheduler = scheduler;
        this.plugin = plugin;
    }

    @Override
    public Event parseEvent(final Instruction instruction) throws InstructionParseException {
        final String timeString = instruction.next();
        final Time time = parseTimeType(timeString);
        final long rawTime = parseTime(timeString);
        final WorldSelector worldSelector = parseWorld(instruction.getOptional("world"));
        return new PrimaryServerThreadEvent(
                new TimeEvent(time, rawTime, worldSelector),
                server, scheduler, plugin);
    }

    @Override
    public StaticEvent parseStaticEvent(final Instruction instruction) throws InstructionParseException {
        if (instruction.copy().getOptional("world") == null) {
            return new DoNothingStaticEvent();
        } else {
            return new NullStaticEventAdapter(parseEvent(instruction));
        }
    }

    private Time parseTimeType(final String timeString) throws InstructionParseException {
        if (timeString.isEmpty()) {
            throw new InstructionParseException("Time cannot be empty");
        }
        return switch (timeString.charAt(0)) {
            case '+' -> Time.ADD;
            case '-' -> Time.SUBTRACT;
            default -> Time.SET;
        };
    }

    private Long parseTime(final String timeString) {
        return Math.abs((long) Float.parseFloat(timeString) * 1000);
    }

    @NotNull
    private WorldSelector parseWorld(final String worldName) {
        if (worldName == null) {
            return new PlayerWorldSelector();
        } else {
            final World world = server.getWorld(worldName);
            return new ConstantWorldSelector(world);
        }
    }
}
