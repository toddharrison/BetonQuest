package org.betonquest.betonquest.api;

import org.betonquest.betonquest.Instruction;
import org.betonquest.betonquest.api.profiles.Profile;
import org.betonquest.betonquest.exceptions.InstructionParseException;

import java.util.Locale;

/**
 * An objective that is not completed by doing some action just once, but multiple times. It provides common properties
 * and a versatile data object to track the progress.
 */
public abstract class CountingObjective extends Objective {

    /**
     * The message name for notification messages used by default.
     */
    private final String defaultNotifyMessageName;

    /**
     * The amount of units required for completion.
     */
    protected int targetAmount;

    /**
     * Create a counting objective.
     *
     * @param instruction the objective instruction
     * @throws InstructionParseException if the syntax is wrong or any error happens while parsing
     */
    public CountingObjective(final Instruction instruction) throws InstructionParseException {
        this(instruction, null);
    }

    /**
     * Create a counting objective with default notify message name.
     *
     * @param instruction       the objective instruction
     * @param notifyMessageName the message name used for notifying by default
     * @throws InstructionParseException if the syntax is wrong or any error happens while parsing
     */
    public CountingObjective(final Instruction instruction, final String notifyMessageName) throws InstructionParseException {
        super(instruction);
        template = CountingData.class;
        defaultNotifyMessageName = notifyMessageName;
    }

    @Override
    public final String getDefaultDataInstruction() {
        return Integer.toString(targetAmount);
    }

    @Override
    public String getProperty(final String name, final Profile profile) {
        final Integer data = switch (name.toLowerCase(Locale.ROOT)) {
            case "amount" -> getCountingData(profile).getCompletedAmount();
            case "left" -> getCountingData(profile).getAmountLeft();
            case "total" -> getCountingData(profile).getTargetAmount();
            case "absoluteamount" -> Math.abs(getCountingData(profile).getCompletedAmount());
            case "absoluteleft" -> Math.abs(getCountingData(profile).getAmountLeft());
            case "absolutetotal" -> Math.abs(getCountingData(profile).getTargetAmount());
            default -> null;
        };
        return data == null ? "" : data.toString();
    }

    /**
     * Get the {@link CountingData} objective data for given profile.
     *
     * @param profile the {@link Profile} to get the data for
     * @return counting objective data of the profile
     */
    public final CountingData getCountingData(final Profile profile) {
        return (CountingData) dataMap.get(profile);
    }

    /**
     * Complete the objective if fulfilled or else notify the profile's player if required. It will use the
     * {@link #defaultNotifyMessageName} if set, otherwise no notification will be sent, even if {@link #notify} is
     * {@code true}.
     *
     * @param profile the {@link Profile} to act for
     * @return {@code true} if the objective is completed; {@code false} otherwise
     */
    protected final boolean completeIfDoneOrNotify(final Profile profile) {
        return completeIfDoneOrNotify(profile, defaultNotifyMessageName);
    }

    /**
     * Complete the objective if fulfilled or else notify the profile's player if required. It will use the provided
     * notification message name. If it is {@code null}, no notification is sent, even if a
     * {@link #defaultNotifyMessageName} was set and a notification should have been sent.
     *
     * @param profile           the {@link Profile} to act for
     * @param notifyMessageName message name for notification message
     * @return {@code true} if the objective is completed; {@code false} otherwise
     */
    protected final boolean completeIfDoneOrNotify(final Profile profile, final String notifyMessageName) {
        final CountingData data = getCountingData(profile);
        if (data.isComplete()) {
            completeObjective(profile);
            return true;
        }
        if (notify && notifyMessageName != null && shouldNotify(data) && profile.getOnlineProfile().isPresent()) {
            sendNotify(profile.getOnlineProfile().get(), notifyMessageName, Math.abs(data.getAmountLeft()));
        }
        return false;
    }

    private boolean shouldNotify(final CountingData data) {
        final int newAmount = Math.abs(data.getAmountLeft());
        final int oldAmount = Math.abs(data.getPreviousAmountLeft());
        return newAmount > oldAmount && newAmount / notifyInterval != oldAmount / notifyInterval
                || newAmount < oldAmount && (newAmount - 1) / notifyInterval != (oldAmount - 1) / notifyInterval;
    }

    /**
     * Objective data for counting objectives.
     */
    public static class CountingData extends ObjectiveData {

        /**
         * The target amount of units initially required for completion.
         */
        private final int targetAmount;

        /**
         * The direction factor. Enables {@link #progress(int)} and {@link #regress(int)} methods and fuzzy checking for
         * completion so that the target amount can be exceeded in both positive and negative direction.
         */
        private final int directionFactor;

        /**
         * The amount of units left for completion.
         */
        private int amountLeft;

        /**
         * The last applied to the change of the units left for completion.
         */
        private int lastChange;

        /**
         * Create a counting objective.
         *
         * @param instruction to {@link Integer} parsable string, containing the units to complete or output from
         *                    {@link #toString()}
         * @param profile     the {@link Profile} to create the data for
         * @param objID       id of the objective, used by BetonQuest to store this {@link ObjectiveData} in the database
         */
        public CountingData(final String instruction, final Profile profile, final String objID) {
            super(instruction, profile, objID);
            final String countingInstruction = instruction.split(";", 2)[0];
            final String[] instructionParts = countingInstruction.split("/");
            switch (instructionParts.length) {
                case 1:
                    targetAmount = Integer.parseInt(countingInstruction);
                    amountLeft = targetAmount;
                    directionFactor = amountLeft < 0 ? -1 : 1;
                    lastChange = 0;
                    break;
                case 4:
                    targetAmount = Integer.parseInt(instructionParts[0]);
                    amountLeft = Integer.parseInt(instructionParts[1]);
                    directionFactor = Integer.parseInt(instructionParts[2]);
                    lastChange = Integer.parseInt(instructionParts[3]);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid instruction string: " + instruction);
            }
        }

        /**
         * Get the initially required amount of units that are needed to complete the objective after it just got
         * started.
         *
         * @return the target amount
         */
        public int getTargetAmount() {
            return targetAmount;
        }

        /**
         * Get the amount of units that are still required to complete the objective.
         *
         * @return the amount left
         */
        public int getAmountLeft() {
            return amountLeft;
        }

        /**
         * Get the amount ob units that were already completed since the objective has been started.
         *
         * @return the completed amount
         */
        public int getCompletedAmount() {
            return targetAmount - amountLeft;
        }

        /**
         * Get the direction factor. If the initial target amount was positive or zero it is {@code 1}, if it was
         * negative it is {@code -1}. The factor is used to determine completion and to let the objective progress when
         * not knowing if counting up or down is required to get nearer to the completion.
         *
         * @return the direction factor; either 1 or -1
         */
        public int getDirectionFactor() {
            return directionFactor;
        }

        /**
         * Get the last change of the amount of units completed that happened. The change is directed, thus reapplying
         * it can be done by using {@link #add(int)} and undoing by using {@link #subtract(int)}.
         *
         * @return the last change of the amount
         */
        public int getLastChange() {
            return lastChange;
        }

        /**
         * Get the amount of units that were still required to complete the objective before the last change.
         *
         * @return the amount left
         */
        public int getPreviousAmountLeft() {
            return amountLeft - lastChange;
        }

        /**
         * Check for completion of the objective. It is true if the target amount has been reached or exceeded.
         *
         * @return true if the objective is completed; false otherwise
         */
        public boolean isComplete() {
            return amountLeft * directionFactor <= 0;
        }

        /**
         * Move the one of unit towards completion of the objective. This is direction-aware.
         *
         * @return self for chaining statements
         */
        public CountingData progress() {
            return progress(1);
        }

        /**
         * Move the given amount of units towards completion of the objective. This is direction-aware.
         *
         * @param amount units to progress
         * @return self for chaining statements
         */
        public CountingData progress(final int amount) {
            return add(amount * directionFactor);
        }

        /**
         * Move the one of unit away from completion of the objective. This is direction-aware.
         *
         * @return self for chaining statements
         */
        public CountingData regress() {
            return regress(1);
        }

        /**
         * Move the given amount of units away from completion of the objective. This is direction-aware.
         *
         * @param amount units to regress
         * @return self for chaining statements
         */
        public CountingData regress(final int amount) {
            return subtract(amount * directionFactor);
        }

        /**
         * Move one unit in positive direction. Same as {@link #progress()} when the target unit amount is positive.
         * This is <b>not</b> direction-aware.
         *
         * @return self for chaining statements
         */
        public CountingData add() {
            return add(1);
        }

        /**
         * Move the given amount of units in positive direction. Same as {@link #progress(int)} when the target unit
         * amount is positive. This is <b>not</b> direction-aware.
         *
         * @param amount units to progress
         * @return self for chaining statements
         */
        public CountingData add(final int amount) {
            return change(-amount);
        }

        /**
         * Move one unit in negative direction. Same as {@link #regress()} when the target unit amount is negative. This
         * is <b>not</b> direction-aware.
         *
         * @return self for chaining statements
         */
        public CountingData subtract() {
            return subtract(1);
        }

        /**
         * Move the given amount of units in negative direction. Same as {@link #regress(int)} when the target unit
         * amount is negative. This is <b>not</b> direction-aware.
         *
         * @param amount units to progress
         * @return self for chaining statements
         */
        public CountingData subtract(final int amount) {
            return change(amount);
        }

        private CountingData change(final int amount) {
            amountLeft += amount;
            lastChange = amount;
            update();
            return this;
        }

        @Override
        public String toString() {
            return String.join("/",
                    Integer.toString(targetAmount),
                    Integer.toString(amountLeft),
                    Integer.toString(directionFactor),
                    Integer.toString(lastChange));
        }
    }
}
