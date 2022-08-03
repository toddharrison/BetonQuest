package org.betonquest.betonquest.quest.event;

import org.betonquest.betonquest.api.quest.event.StaticEvent;
import org.betonquest.betonquest.exceptions.QuestRuntimeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test {@link SequentialStaticEvent}.
 */
@ExtendWith(MockitoExtension.class)
class SequentialStaticEventTest {
    @Test
    void testCanExecuteWithZeroEvents() {
        final StaticEvent event = new SequentialStaticEvent();
        assertDoesNotThrow(event::execute, "SequentialStaticEvent should not fail with no events to execute.");
    }

    @Test
    void testExecutesOneEvent(@Mock final StaticEvent internal) throws QuestRuntimeException {
        final StaticEvent event = new SequentialStaticEvent(internal);
        event.execute();
        verify(internal).execute();
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    void testExecutesMultipleEvents(@Mock final StaticEvent first, @Mock final StaticEvent second) throws QuestRuntimeException {
        final StaticEvent event = new SequentialStaticEvent(first, second);

        event.execute();

        final InOrder order = inOrder(first, second);
        order.verify(first).execute();
        order.verify(second).execute();
    }

    @Test
    void testFailuresArePassedOn(@Mock final StaticEvent internal) throws QuestRuntimeException {
        final QuestRuntimeException exception = new QuestRuntimeException("test exception");
        doThrow(exception).when(internal).execute();
        final StaticEvent event = new SequentialStaticEvent(internal);

        final QuestRuntimeException thrown
                = assertThrows(QuestRuntimeException.class, event::execute, "The failure of an internal event should fail the sequential event immediately.");
        assertSame(exception, thrown, "The exception should not be wrapped or exchanged.");
    }
}
