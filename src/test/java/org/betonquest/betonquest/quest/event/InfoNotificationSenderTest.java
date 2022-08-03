package org.betonquest.betonquest.quest.event;

import org.betonquest.betonquest.api.config.QuestPackage;
import org.betonquest.betonquest.config.Config;
import org.betonquest.betonquest.exceptions.QuestRuntimeException;
import org.betonquest.betonquest.modules.logger.util.BetonQuestLoggerService;
import org.betonquest.betonquest.modules.logger.util.LogValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test {@link InfoNotificationSender}.
 */
@ExtendWith(BetonQuestLoggerService.class)
@ExtendWith(MockitoExtension.class)
class InfoNotificationSenderTest {
    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    void testSendNotifyIsCalled(@Mock final QuestPackage questPackage) {
        when(questPackage.getPackagePath()).thenReturn("package.path");
        final NotificationSender sender = new InfoNotificationSender("message-name", questPackage, "full.id");

        try (MockedStatic<Config> config = mockStatic(Config.class)) {
            sender.sendNotification("player-id");
            config.verify(() -> Config.sendNotify("package.path", "player-id", "message-name", null, "message-name,info"));
        }
    }

    @Test
    void testSendNotifyHandlesError(@Mock final QuestPackage questPackage, final LogValidator logValidator) {
        when(questPackage.getPackagePath()).thenReturn("package.path");
        final NotificationSender sender = new InfoNotificationSender("message-name", questPackage, "full.id");

        try (MockedStatic<Config> config = mockStatic(Config.class)) {
            config.when(() -> Config.sendNotify(any(), any(String.class), any(), any(), any()))
                    .thenThrow(new QuestRuntimeException("Test cause."));
            assertDoesNotThrow(() -> sender.sendNotification("player-id"), "Failing to send a notification should not throw an exception.");
        }
        logValidator.assertLogEntry(Level.WARNING, "The notify system was unable to play a sound for the 'message-name' category in 'full.id'. Error was: 'Test cause.'");
        logValidator.assertLogEntry(Level.FINE, "Additional stacktrace:", QuestRuntimeException.class, "Test cause.");
        logValidator.assertEmpty();
    }
}
