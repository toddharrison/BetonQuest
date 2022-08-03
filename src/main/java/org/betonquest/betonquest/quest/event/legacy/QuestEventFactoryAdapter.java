package org.betonquest.betonquest.quest.event.legacy;

import org.betonquest.betonquest.Instruction;
import org.betonquest.betonquest.api.QuestEvent;
import org.betonquest.betonquest.api.quest.event.Event;
import org.betonquest.betonquest.api.quest.event.EventFactory;
import org.betonquest.betonquest.api.quest.event.StaticEvent;
import org.betonquest.betonquest.api.quest.event.StaticEventFactory;
import org.betonquest.betonquest.exceptions.InstructionParseException;

/**
 * Adapter to let {@link EventFactory EventFactories} create {@link QuestEvent}s from the {@link Event}s and
 * {@link StaticEvent}s they create.
 */
public class QuestEventFactoryAdapter implements QuestEventFactory {

    /**
     * The event factory to be adapted.
     */
    private final EventFactory factory;

    /**
     * The static event factory to be adapted.
     */
    private final StaticEventFactory staticFactory;

    /**
     * Create the factory from an {@link EventFactory}.
     *
     * @param factory event factory to use
     * @param staticFactory static event factory to use
     */
    public QuestEventFactoryAdapter(final EventFactory factory, final StaticEventFactory staticFactory) {
        this.factory = factory;
        this.staticFactory = staticFactory;
    }

    @Override
    public QuestEventAdapter parseEventInstruction(final Instruction instruction) throws InstructionParseException {
        final Event event = factory.parseEvent(instruction.copy());
        final StaticEvent staticEvent = staticFactory.parseStaticEvent(instruction.copy());
        return new QuestEventAdapter(instruction, event, staticEvent);
    }
}
