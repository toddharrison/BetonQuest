package org.betonquest.betonquest.quest.event.journal;

import org.betonquest.betonquest.Journal;
import org.betonquest.betonquest.modules.logger.util.BetonQuestLoggerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

/**
 * Test {@link NoActionJournalChanger}.
 */
@ExtendWith(BetonQuestLoggerService.class)
@ExtendWith(MockitoExtension.class)
class NoActionJournalChangerTest {
    @Test
    void testChangeJournalDoesNothing(@Mock final Journal journal) {
        final NoActionJournalChanger changer = new NoActionJournalChanger();
        changer.changeJournal(journal);
        verifyNoInteractions(journal);
    }
}
