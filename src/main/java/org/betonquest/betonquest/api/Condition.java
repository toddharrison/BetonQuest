package org.betonquest.betonquest.api;

import org.betonquest.betonquest.Instruction;
import org.betonquest.betonquest.api.profiles.Profile;
import org.betonquest.betonquest.exceptions.InstructionParseException;
import org.betonquest.betonquest.exceptions.QuestRuntimeException;

/**
 * <p>
 * Superclass for all conditions. You need to extend it in order to create new
 * custom conditions.
 * </p>
 *
 * <p>
 * Registering your condition is done through
 * {@link org.betonquest.betonquest.BetonQuest#registerConditions(String, Class)
 * registerConditions()} method.
 * </p>
 */
public abstract class Condition extends ForceSyncHandler<Boolean> {
    /**
     * Stores instruction string for the condition.
     */
    protected Instruction instruction;

    /**
     * If a condition is static it can be used with null player. Static events
     * can be run with static conditions.
     */
    protected boolean staticness;

    /**
     * If a condition is persistent it can be checked for offline player.
     * Persistent conditions can be checked in folder events after the player
     * logs out.
     */
    protected boolean persistent;

    /**
     * Creates new instance of the condition. The condition should parse
     * instruction string at this point and extract all the data from it. If
     * anything goes wrong, throw {@link InstructionParseException} with an
     * error message describing the problem.
     *
     * @param instruction the Instruction object; you can get one from ID instance with
     *                    {@link org.betonquest.betonquest.id.ID#generateInstruction()
     *                    ID.generateInstruction()} or create it from an instruction
     *                    string
     * @param forceSync   If set to true this executes the condition on the servers main thread.
     *                    Otherwise it will just keep the current thread (which could also be the main thread!).
     */
    public Condition(final Instruction instruction, final boolean forceSync) {
        super(forceSync);
        this.instruction = instruction;
    }

    /**
     * @return if the condition is static or not. If a condition is static it
     * can be used with null profiles. Static events can be run with
     * static events.
     */
    public final boolean isStatic() {
        return staticness;
    }

    /**
     * @return if the condition is persistent or not. If a condition is
     * persistent it can be checked for {@link Profile}s. If it's neither persistent nor static an
     * {@link org.betonquest.betonquest.api.profiles.OnlineProfile} must be used. Persistent
     * conditions can be checked in folder events after the player logs
     * out.
     */
    public final boolean isPersistent() {
        return persistent;
    }

    /**
     * This method should contain all logic for the condition and use data
     * parsed by the constructor. Don't worry about inverting the condition,
     * it's done by the rest of BetonQuest's logic. When this method is called
     * all the required data must be present and parsed correctly.
     *
     * @param profile the {@link Profile} for which the condition will be checked
     * @return the result of the check
     * @throws QuestRuntimeException when an error happens at runtime (for example a numeric
     *                               variable resolves to a string)
     */
    @Override
    protected abstract Boolean execute(Profile profile) throws QuestRuntimeException;
}
