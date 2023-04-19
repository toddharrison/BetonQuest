package org.betonquest.betonquest.api.schedule;

import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.BetonQuestLogger;
import org.betonquest.betonquest.id.EventID;
import org.betonquest.betonquest.modules.schedule.ScheduleID;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Superclass of all event schedulers.
 * While {@link Schedule} holds the data and settings of a schedule, children of this class should contain the logic for
 * scheduling of events.
 * </p>
 * <p>
 * When loading the configs,
 * new schedules are parsed and registered in the matching Scheduler by calling {@link #addSchedule(Schedule)}.
 * After everything is loaded {@link #start()} is called. It should start the scheduler.
 * Once a time defined in the schedule is met,
 * the referenced events shall be executed using {@link #executeEvents(Schedule)}.
 * On shutdown or before reloading all data, {@link #stop()} is called to stop all schedules.
 * Also, this class should implement the {@link CatchupStrategy} required by the schedule.
 * </p>
 *
 * @param <S> Type of Schedule
 */
@SuppressWarnings("PMD.AbstractClassWithoutAbstractMethod")
public abstract class Scheduler<S extends Schedule> {
    /**
     * Custom {@link BetonQuestLogger} instance for this class.
     */
    private static final BetonQuestLogger LOG = BetonQuestLogger.create("Schedules");

    /**
     * Map containing all schedules that belong to this scheduler.
     */
    protected final Map<ScheduleID, S> schedules;

    /**
     * Flag stating if this scheduler is currently running.
     */
    private boolean running;

    /**
     * Default constructor.
     */
    public Scheduler() {
        schedules = new HashMap<>();
        running = false;
    }

    /**
     * Register a new schedule to the list of schedules managed by this scheduler.
     * The schedule shall remain inactive till method {@link #start()} is called to activate all schedules.
     *
     * @param schedule schedule object to register
     */
    public void addSchedule(final S schedule) {
        schedules.put(schedule.getId(), schedule);
    }

    /**
     * <p>
     * Start all schedules that have been added to this scheduler.
     * This method is called on startup and reload of BetonQuest to activate/resume all schedules.
     * </p>
     * <p>
     * As well as handling the actual scheduling logic this method shall also handle catching up schedules that
     * were missed during reloading or shutdown of the server, based on their {@link CatchupStrategy}.
     * </p>
     * <p><b>
     * When overriding this method, make sure to call {@code super.start()} at some point to update the running flag.
     * </b></p>
     */
    public void start() {
        running = true;
    }

    /**
     * <p>
     * Stop the scheduler and unregister all schedules that belong to this scheduler.
     * Typically this method is called on reload and server shutdown.
     * </p>
     * <p><b>
     * When overriding this method, make sure to call {@code super.stop()} at some point to clear the map of schedules.
     * </b></p>
     */
    public void stop() {
        running = false;
        schedules.clear();
    }

    /**
     * This method shall be called whenever the execution time of a schedule is reached.
     * It executes all events that should be run by the schedule.
     *
     * @param schedule a schedule that reached execution time, providing a list of events to run
     */
    protected void executeEvents(final S schedule) {
        LOG.debug(schedule.getId().getPackage(), "Schedule '" + schedule.getId() + "' runs its events...");
        for (final EventID eventID : schedule.getEvents()) {
            BetonQuest.event(null, eventID);
        }
    }

    /**
     * Check if this scheduler is currently running.
     *
     * @return true if currently running, false if not (e.g. during startup or reloading)
     */
    public boolean isRunning() {
        return running;
    }
}
