package org.betonquest.betonquest.modules.logger.custom;

import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.modules.logger.BetonQuestLogRecord;
import org.bukkit.Bukkit;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * This {@link Handler} can hold all {@link LogRecord}s from the configured log history time frame.
 * <br>
 * If the filter returns true, a LogRecord will be passed to the target {@link Handler},
 * otherwise it will be written to the history.
 * The history can then be pushed to the target handler at any time.
 * It is automatically pushed if the filter returns true for any subsequent LogRecord.
 */
public class HistoryLogHandler extends Handler {

    /**
     * The {@link LogRecord} history.
     */
    private final Queue<LogRecord> records;

    /**
     * The target Handler to log the history to.
     */
    private final Handler target;

    /**
     * Creates a new {@link HistoryLogHandler}.
     * <p>
     * If expireAfterMinutes is 0, no history will be saved at all.
     *
     * @param target             The Handler to log the history to
     * @param expireAfterMinutes The time a {@link LogRecord} stays in the cache
     */
    public HistoryLogHandler(final Handler target, final int expireAfterMinutes) {
        super();
        if (expireAfterMinutes == 0) {
            this.records = null;
        } else {
            this.records = new ConcurrentLinkedQueue<>();
            final int expireAfterMillis = expireAfterMinutes * 60 * 1000;
            Bukkit.getScheduler().runTaskTimerAsynchronously(BetonQuest.getInstance(), () -> {
                LogRecord record = null;
                do {
                    if (record != null) {
                        records.remove();
                    }
                    record = records.peek();
                } while (record != null && record.getMillis() < System.currentTimeMillis() - expireAfterMillis);

            }, 20, 20);
        }
        this.target = target;
    }

    /**
     * Logs a LogRecord to the history or the target handler.
     *
     * @param record The {@link LogRecord} to log
     */
    @Override
    public void publish(final LogRecord record) {
        if (!(record instanceof BetonQuestLogRecord)) {
            return;
        }
        if (isLoggable(record)) {
            push();
            target.publish(record);
        } else if (records != null) {
            records.add(record);
        }
    }

    /**
     * Publishes any available history to the target handler.
     */
    public void push() {
        if (records != null && !records.isEmpty()) {
            target.publish(new LogRecord(Level.INFO, "=====START OF HISTORY====="));
            LogRecord record;
            do {
                record = records.poll();
                target.publish(record);
            } while (record != null);
            target.publish(new LogRecord(Level.INFO, "=====END OF HISTORY====="));
        }
    }

    /**
     * Flushes the target handler.
     */
    @Override
    public void flush() {
        target.flush();
    }

    /**
     * Closes the target handler.
     */
    @Override
    public void close() {
        target.close();
    }
}
