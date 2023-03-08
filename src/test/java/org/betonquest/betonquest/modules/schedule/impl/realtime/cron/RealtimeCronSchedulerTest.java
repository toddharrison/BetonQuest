package org.betonquest.betonquest.modules.schedule.impl.realtime.cron;

import com.cronutils.model.time.ExecutionTime;
import org.betonquest.betonquest.api.schedule.CatchupStrategy;
import org.betonquest.betonquest.exceptions.InstructionParseException;
import org.betonquest.betonquest.exceptions.ObjectNotFoundException;
import org.betonquest.betonquest.modules.logger.util.BetonQuestLoggerService;
import org.betonquest.betonquest.modules.logger.util.LogValidator;
import org.betonquest.betonquest.modules.schedule.LastExecutionCache;
import org.betonquest.betonquest.modules.schedule.ScheduleID;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static org.mockito.Mockito.*;

/**
 * Tests for the {@link RealtimeCronScheduler}
 */
@ExtendWith(BetonQuestLoggerService.class)
class RealtimeCronSchedulerTest {

    /**
     * Mocked schedule id.
     */
    private static final ScheduleID SCHEDULE_ID = mock(ScheduleID.class);

    static {
        when(SCHEDULE_ID.toString()).thenReturn("test.schedule");
    }

    @NotNull
    private static RealtimeCronSchedule getSchedule(final CatchupStrategy catchupStrategy, final boolean shouldRunOnReboot) {
        final RealtimeCronSchedule schedule = mock(RealtimeCronSchedule.class);
        when(schedule.shouldRunOnReboot()).thenReturn(shouldRunOnReboot);

        when(schedule.getId()).thenReturn(SCHEDULE_ID);

        final ExecutionTime executionTime = mock(ExecutionTime.class);
        when(executionTime.timeToNextExecution(any())).thenReturn(Optional.empty());
        when(schedule.getExecutionTime()).thenReturn(executionTime);

        when(schedule.getCatchup()).thenReturn(catchupStrategy);

        return schedule;
    }

    @Test
    void testStartWithoutSchedules(final LogValidator validator) {
        final LastExecutionCache cache = mock(LastExecutionCache.class);
        final RealtimeCronScheduler scheduler = spy(new RealtimeCronScheduler(cache));
        scheduler.start();

        validator.assertLogEntry(Level.FINE, "(Schedules) Starting realtime scheduler.");
        validator.assertLogEntry(Level.FINE, "(Schedules) Collecting reboot schedules...");
        validator.assertLogEntry(Level.FINE, "(Schedules) Found 0 reboot schedules. They will be run on next server tick.");
        validator.assertLogEntry(Level.FINE, "(Schedules) Found 0 missed schedule runs that will be caught up.");
        validator.assertLogEntry(Level.FINE, "(Schedules) Realtime scheduler start complete.");
        validator.assertEmpty();
        verify(scheduler, never()).schedule(any());
    }

    @Test
    void testStartWithRebootSchedules(final LogValidator validator) throws InstructionParseException, ObjectNotFoundException {
        final LastExecutionCache cache = mock(LastExecutionCache.class);
        final RealtimeCronScheduler scheduler = new RealtimeCronScheduler(cache);
        final RealtimeCronSchedule schedule = getSchedule(CatchupStrategy.NONE, true);
        scheduler.addSchedule(schedule);
        scheduler.start();

        validator.assertLogEntry(Level.FINE, "(Schedules) Starting realtime scheduler.");
        validator.assertLogEntry(Level.FINE, "(Schedules) Collecting reboot schedules...");
        validator.assertLogEntry(Level.FINE, "(Schedules) Found 1 reboot schedules. They will be run on next server tick.");
        validator.assertLogEntry(Level.FINE, "(Schedules) Schedule 'test.schedule' runs its events...");
        validator.assertLogEntry(Level.FINE, "(Schedules) Found 0 missed schedule runs that will be caught up.");
        validator.assertLogEntry(Level.FINE, "(Schedules) Realtime scheduler start complete.");
        validator.assertEmpty();
        verify(schedule, times(1)).getEvents();
    }

    @Test
    void testStartWithMissedSchedulesStrategyOne(final LogValidator validator) {
        final LastExecutionCache cache = mock(LastExecutionCache.class);
        final Instant lastExecution = Instant.now().minusSeconds(60);
        when(cache.getLastExecutionTime(SCHEDULE_ID)).thenReturn(Optional.of(lastExecution));

        final RealtimeCronScheduler scheduler = new RealtimeCronScheduler(cache);
        final RealtimeCronSchedule schedule = getSchedule(CatchupStrategy.ONE, false);
        final ExecutionTime executionTime = mock(ExecutionTime.class);
        final ZonedDateTime nextMissedExecution = lastExecution.plusSeconds(30).atZone(ZoneId.systemDefault());
        when(executionTime.nextExecution(any())).thenReturn(Optional.of(nextMissedExecution));
        when(schedule.getExecutionTime()).thenReturn(executionTime);
        scheduler.addSchedule(schedule);
        scheduler.start();

        validator.assertLogEntry(Level.FINE, "(Schedules) Starting realtime scheduler.");
        validator.assertLogEntry(Level.FINE, "(Schedules) Collecting reboot schedules...");
        validator.assertLogEntry(Level.FINE, "(Schedules) Found 0 reboot schedules. They will be run on next server tick.");
        validator.assertLogEntry(Level.FINE, "(Schedules) Schedule 'test.schedule' run missed at " + nextMissedExecution);
        validator.assertLogEntry(Level.FINE, "(Schedules) Found 1 missed schedule runs that will be caught up.");
        validator.assertLogEntry(Level.FINE, "(Schedules) Running missed schedules to catch up...");
        validator.assertLogEntry(Level.FINE, "(Schedules) Schedule 'test.schedule' runs its events...");
        validator.assertLogEntry(Level.FINE, "(Schedules) Realtime scheduler start complete.");
        validator.assertEmpty();
        verify(schedule, times(1)).getEvents();
    }

    @Test
    void testStartWithMissedSchedulesStrategyAll(final LogValidator validator) {
        final LastExecutionCache cache = mock(LastExecutionCache.class);
        final Instant lastExecution = Instant.now().minusSeconds(60);
        when(cache.getLastExecutionTime(SCHEDULE_ID)).thenReturn(Optional.of(lastExecution));

        final RealtimeCronScheduler scheduler = new RealtimeCronScheduler(cache);
        final RealtimeCronSchedule schedule = getSchedule(CatchupStrategy.ALL, false);
        final ExecutionTime executionTime = mock(ExecutionTime.class);
        final ZonedDateTime nextMissedExecution1 = lastExecution.plusSeconds(20).atZone(ZoneId.systemDefault());
        final ZonedDateTime nextMissedExecution2 = lastExecution.plusSeconds(40).atZone(ZoneId.systemDefault());
        final ZonedDateTime nextMissedExecution3 = lastExecution.plusSeconds(60).atZone(ZoneId.systemDefault());
        final ZonedDateTime nextMissedExecution4 = lastExecution.plusSeconds(80).atZone(ZoneId.systemDefault());
        when(executionTime.nextExecution(any())).thenReturn(
                Optional.of(nextMissedExecution1),
                Optional.of(nextMissedExecution2),
                Optional.of(nextMissedExecution3),
                Optional.of(nextMissedExecution4));
        when(schedule.getExecutionTime()).thenReturn(executionTime);
        scheduler.addSchedule(schedule);
        scheduler.start();

        validator.assertLogEntry(Level.FINE, "(Schedules) Starting realtime scheduler.");
        validator.assertLogEntry(Level.FINE, "(Schedules) Collecting reboot schedules...");
        validator.assertLogEntry(Level.FINE, "(Schedules) Found 0 reboot schedules. They will be run on next server tick.");
        validator.assertLogEntry(Level.FINE, "(Schedules) Schedule 'test.schedule' run missed at " + nextMissedExecution1);
        validator.assertLogEntry(Level.FINE, "(Schedules) Schedule 'test.schedule' run missed at " + nextMissedExecution2);
        validator.assertLogEntry(Level.FINE, "(Schedules) Schedule 'test.schedule' run missed at " + nextMissedExecution3);
        validator.assertLogEntry(Level.FINE, "(Schedules) Found 3 missed schedule runs that will be caught up.");
        validator.assertLogEntry(Level.FINE, "(Schedules) Running missed schedules to catch up...");
        validator.assertLogEntry(Level.FINE, "(Schedules) Schedule 'test.schedule' runs its events...");
        validator.assertLogEntry(Level.FINE, "(Schedules) Schedule 'test.schedule' runs its events...");
        validator.assertLogEntry(Level.FINE, "(Schedules) Schedule 'test.schedule' runs its events...");
        validator.assertLogEntry(Level.FINE, "(Schedules) Realtime scheduler start complete.");
        validator.assertEmpty();
        verify(schedule, times(3)).getEvents();
    }

    @Test
    @SuppressWarnings("PMD.DoNotUseThreads")
    void testStartSchedule(final LogValidator validator) {
        final Duration duration = Duration.ofSeconds(20);
        final LastExecutionCache cache = mock(LastExecutionCache.class);
        final ScheduledExecutorService executorService = mock(ScheduledExecutorService.class);
        when(executorService.schedule(any(Runnable.class), eq(duration.toMillis()), eq(TimeUnit.MILLISECONDS))).then(invocation -> {
            final Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        });
        final RealtimeCronScheduler scheduler = new RealtimeCronScheduler(() -> executorService, cache);
        final RealtimeCronSchedule schedule = getSchedule(CatchupStrategy.NONE, false);
        final ExecutionTime executionTime = mock(ExecutionTime.class);
        when(executionTime.timeToNextExecution(any())).thenReturn(
                Optional.of(duration),
                Optional.of(duration),
                Optional.of(duration),
                Optional.empty());
        when(schedule.getExecutionTime()).thenReturn(executionTime);
        scheduler.addSchedule(schedule);
        scheduler.start();

        validator.assertLogEntry(Level.FINE, "(Schedules) Starting realtime scheduler.");
        validator.assertLogEntry(Level.FINE, "(Schedules) Collecting reboot schedules...");
        validator.assertLogEntry(Level.FINE, "(Schedules) Found 0 reboot schedules. They will be run on next server tick.");
        validator.assertLogEntry(Level.FINE, "(Schedules) Found 0 missed schedule runs that will be caught up.");
        validator.assertLogEntry(Level.FINE, "(Schedules) Schedule 'test.schedule' runs its events...");
        validator.assertLogEntry(Level.FINE, "(Schedules) Schedule 'test.schedule' runs its events...");
        validator.assertLogEntry(Level.FINE, "(Schedules) Schedule 'test.schedule' runs its events...");
        validator.assertLogEntry(Level.FINE, "(Schedules) Realtime scheduler start complete.");
        validator.assertEmpty();
        verify(schedule, times(3)).getEvents();
    }
}
