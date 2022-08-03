package org.betonquest.betonquest.exceptions;

import java.io.Serial;

/**
 * Exception thrown when there was an unexpected error.
 */
public class QuestRuntimeException extends Exception {
    /**
     * serialVersionUID.
     */
    @Serial
    private static final long serialVersionUID = 2375018439469626832L;

    /**
     * {@link Exception#Exception(String)}
     *
     * @param message the displayed message.
     */
    public QuestRuntimeException(final String message) {
        super(message);
    }

    /**
     * {@link Exception#Exception(String, Throwable)}
     *
     * @param message the exception message.
     * @param cause   the Throwable that caused this exception.
     */
    public QuestRuntimeException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * {@link Exception#Exception(Throwable)}
     *
     * @param cause the Throwable that caused this exception.
     */
    public QuestRuntimeException(final Throwable cause) {
        super(cause);
    }
}
