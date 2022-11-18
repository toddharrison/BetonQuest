package org.betonquest.betonquest.quest.event;

import org.betonquest.betonquest.api.profiles.Profile;

/**
 * Notification sender that suppresses notifications instead of sending them.
 */
public class NoNotificationSender implements NotificationSender {

    /**
     * Create the no notification sender.
     */
    public NoNotificationSender() {
    }

    @Override
    public void sendNotification(final Profile profile) {
        // null object pattern
    }
}
