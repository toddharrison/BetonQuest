package org.betonquest.betonquest.utils.math.tokens;

import org.betonquest.betonquest.api.profiles.Profile;
import org.betonquest.betonquest.exceptions.QuestRuntimeException;
import org.betonquest.betonquest.utils.math.Operator;

/**
 * An operation performed on two tokens.
 *
 * @deprecated This should be replaced in BQ 2.0 with a real expression parsing lib like
 * https://github.com/fasseg/exp4j
 */
@Deprecated
public class Operation implements Token {

    /**
     * First value, left of the operator.
     */
    private final Token val1;

    /**
     * The operator.
     */
    private final Operator operator;

    /**
     * Second value, right of the operator.
     */
    private final Token val2;

    /**
     * Creates a new operation.
     *
     * @param val1     left token
     * @param operator operator
     * @param val2     right token
     */
    public Operation(final Token val1, final Operator operator, final Token val2) {
        this.val1 = val1;
        this.operator = operator;
        this.val2 = val2;
    }

    @Override
    public double resolve(final Profile profile) throws QuestRuntimeException {
        return operator.calculate(val1.resolve(profile), val2.resolve(profile));
    }

    @Override
    public String toString() {
        return val1.toString() + operator.toString() + val2.toString();
    }
}
