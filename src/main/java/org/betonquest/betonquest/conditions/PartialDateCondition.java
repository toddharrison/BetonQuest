package org.betonquest.betonquest.conditions;

import org.betonquest.betonquest.Instruction;
import org.betonquest.betonquest.api.Condition;
import org.betonquest.betonquest.api.profiles.Profile;
import org.betonquest.betonquest.exceptions.InstructionParseException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


/**
 * Checks if the current date matches one of the given dates
 */
@SuppressWarnings("PMD.CommentRequired")
public class PartialDateCondition extends Condition {
    private final List<TimeInterval> dayOfMonth;

    private final List<TimeInterval> month;

    private final List<TimeInterval> year;

    public PartialDateCondition(final Instruction instruction) throws InstructionParseException {
        super(instruction, false);
        super.staticness = true;
        super.persistent = true;
        final String dayOfMonthString = instruction.getOptional("day");
        if (dayOfMonthString == null) {
            this.dayOfMonth = null;
        } else {
            this.dayOfMonth = TimeInterval.parseFromString(dayOfMonthString, PartialDate.DAY);
        }
        final String monthString = instruction.getOptional("month");
        if (monthString == null) {
            this.month = null;
        } else {
            this.month = TimeInterval.parseFromString(monthString, PartialDate.MONTH);
        }
        final String yearString = instruction.getOptional("year");
        if (yearString == null) {
            this.year = null;
        } else {
            this.year = TimeInterval.parseFromString(yearString, PartialDate.YEAR);
        }
    }

    @Override
    protected Boolean execute(final Profile profile) {
        final Calendar current = Calendar.getInstance();
        if (dayOfMonth != null) {
            final int day = current.get(Calendar.DAY_OF_MONTH);
            if (!this.dayOfMonth.stream().anyMatch(interval -> interval.isWithin(day))) {
                return false;
            }
            //if day is not one of the given ones return false
        }
        if (month != null) {
            final int month = current.get(Calendar.MONTH) + 1;
            //Dont ask why +1: java.util.Calendar is a complete mess (january is 0, december is 11,...)
            if (!this.month.stream().anyMatch(interval -> interval.isWithin(month))) {
                return false;
            }
            //if month is not one of the given ones return false
        }
        if (year != null) {
            final int year = current.get(Calendar.YEAR);
            return this.year.stream().anyMatch(interval -> interval.isWithin(year));
            //if year is not one of the given ones return false
        }
        return true;
    }

    public enum PartialDate {

        DAY(31),
        MONTH(12),
        YEAR(-1);

        private final int maxValue;

        PartialDate(final int maxValue) {
            this.maxValue = maxValue;
        }

        public boolean isValid(final int max) {
            return max > 0 && (maxValue == -1 || max <= maxValue);
        }

        @Override
        public String toString() {
            return super.toString().toLowerCase(Locale.ROOT);
        }
    }

    public static class TimeInterval {
        private final int start;

        private final int end;

        public TimeInterval(final int start, final int end, final PartialDate type) throws InstructionParseException {
            this.start = start;
            this.end = end;
            if (end < start) {
                throw new InstructionParseException(type + " " + end + " is before " + start);
            }
            if (!type.isValid(start)) {
                throw new InstructionParseException(start + " is not a valid " + type);
            }
            if (!type.isValid(end)) {
                throw new InstructionParseException(end + " is not a valid " + type);
            }
        }

        public TimeInterval(final int value, final PartialDate type) throws InstructionParseException {
            this(value, value, type);
        }

        public static List<TimeInterval> parseFromString(final String string, final PartialDate type) throws InstructionParseException {
            // check if the given string is valid (example: 1,3-4,7)
            if (!string.matches("\\d+(-\\d+)?(,\\d+(-\\d+)?)*")) {
                throw new InstructionParseException("could not parse " + type + " from '" + string + "'" + " (invalid format)");
            }
            final List<TimeInterval> intervals = new ArrayList<>();
            final String[] args = string.split(",");
            //Add each interval (or single one) to the list
            for (final String arg : args) {
                try {
                    if (arg.contains("-")) {
                        final int index = arg.indexOf('-');
                        intervals.add(new TimeInterval(Integer.parseInt(arg.substring(0, index)),
                                Integer.parseInt(arg.substring(index + 1)),
                                type));
                    } else {
                        intervals.add(new TimeInterval(Integer.parseInt(arg), type));
                    }
                } catch (final InstructionParseException e) {
                    //if some value exceeded minimum or maximum throw IPE
                    throw new InstructionParseException("could not parse " + type + " from '" + string + "'"
                            + " (" + e.getMessage() + ")", e);
                }
            }
            return intervals;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        public boolean isWithin(final int value) {
            return value >= start && value <= end;
        }

        @Override
        public boolean equals(final Object other) {
            if (this == other) {
                return true;
            }
            if (other == null || getClass() != other.getClass()) {
                return false;
            }
            final TimeInterval interval = (TimeInterval) other;
            return start == interval.start && end == interval.end;
        }

        @Override
        public int hashCode() {
            return Objects.hash(start, end);
        }

        @Override
        public String toString() {
            return start == end ? String.valueOf(start) : start + "-" + end;
        }

    }
}
