package org.betonquest.betonquest.quest.event.cancel;

import org.betonquest.betonquest.api.profiles.Profile;
import org.betonquest.betonquest.api.quest.event.Event;
import org.betonquest.betonquest.config.QuestCanceler;
import org.betonquest.betonquest.exceptions.QuestRuntimeException;

/**
 * The cancel event.
 */
public class CancelEvent implements Event {

    /**
     * The canceler to use.
     */
    private final QuestCanceler canceler;

    /**
     * Creates a new cancel event.
     *
     * @param canceler the canceler to use
     */
    public CancelEvent(final QuestCanceler canceler) {
        this.canceler = canceler;
    }

    @Override
    public void execute(final Profile profile) throws QuestRuntimeException {
        canceler.cancel(profile.getOnlineProfile().get());
    }
}
