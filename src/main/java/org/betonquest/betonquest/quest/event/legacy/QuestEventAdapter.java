package org.betonquest.betonquest.quest.event.legacy;

import lombok.CustomLog;
import org.betonquest.betonquest.Instruction;
import org.betonquest.betonquest.api.QuestEvent;
import org.betonquest.betonquest.api.quest.event.Event;
import org.betonquest.betonquest.api.quest.event.StaticEvent;
import org.betonquest.betonquest.exceptions.InstructionParseException;
import org.betonquest.betonquest.exceptions.QuestRuntimeException;

/**
 * Adapter for {@link Event} and {@link StaticEvent} to fit the old convention of {@link QuestEvent}.
 */
@CustomLog
public class QuestEventAdapter extends QuestEvent {

    /**
     * The normal event to be adapted.
     */
    private final Event event;

    /**
     * The "static" event to be adapted if present. May be {@code null}!
     */
    private final StaticEvent staticEvent;

    /**
     * Create a quest event from an {@link Event} and a {@link StaticEvent}. If the event does not support "static"
     * execution ({@code staticness = false}) then no {@link StaticEvent} instance must be provided.
     *
     * @param instruction instruction used to create the events
     * @param event event to use
     * @param staticEvent static event to use or null if no static execution is supported
     * @throws InstructionParseException if the instruction contains errors
     */
    public QuestEventAdapter(final Instruction instruction, final Event event, final StaticEvent staticEvent) throws InstructionParseException {
        super(instruction, false);
        this.event = event;
        this.staticEvent = staticEvent;
        staticness = staticEvent != null;
    }

    @Override
    protected Void execute(final String playerId) throws QuestRuntimeException {
        if (playerId == null) {
            staticEvent.execute();
        } else {
            event.execute(playerId);
        }
        return null;
    }
}
