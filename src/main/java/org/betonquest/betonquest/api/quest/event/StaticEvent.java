package org.betonquest.betonquest.api.quest.event;

import org.betonquest.betonquest.exceptions.QuestRuntimeException;

/**
 * Interface for "static" quest-events.
 * It represents the "static" event as described in the BetonQuest user documentation.
 * They may act on all players, only online player or even no player at all; this is implementation detail.
 * For the normal event variant see {@link Event}.
 */
public interface StaticEvent {
    /**
     * Executes the "static" event.
     * @throws QuestRuntimeException when the event execution fails
     */
    void execute() throws QuestRuntimeException;
}
