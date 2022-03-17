package org.betonquest.betonquest.variables;

import lombok.CustomLog;
import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.Instruction;
import org.betonquest.betonquest.api.Objective;
import org.betonquest.betonquest.api.Variable;
import org.betonquest.betonquest.exceptions.InstructionParseException;
import org.betonquest.betonquest.exceptions.ObjectNotFoundException;
import org.betonquest.betonquest.id.ObjectiveID;

/**
 * Resolves to a specified property of an objective.
 */
@SuppressWarnings("PMD.CommentRequired")
@CustomLog
public class ObjectivePropertyVariable extends Variable {

    private String propertyName;
    private ObjectiveID objective;

    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    public ObjectivePropertyVariable(final Instruction instruction) throws InstructionParseException {
        super(instruction);

        final String rawInstruction = instruction.getInstruction();
        final String[] parts = rawInstruction.split("\\.");
        String objectiveID = "";
        if (parts.length == 3) {
            objectiveID = parts[1];
            propertyName = parts[2];
        } else if (parts.length == 4) {
            objectiveID = parts[1] + "." + parts[2];
            propertyName = parts[3];
        }

        try {
            objective = new ObjectiveID(instruction.getPackage(), objectiveID);
        } catch (final ObjectNotFoundException e) {
            LOG.warn(instruction.getPackage(), "Error in objective property variable '" + instruction + "' " + e.getMessage());
        }
    }

    @Override
    public String getValue(final String playerID) {
        final Objective objective = BetonQuest.getInstance().getObjective(this.objective);
        // existence of an objective is checked now because it may not exist yet
        // when variable is created (in case of "message" event)
        if (objective == null) {
            return "";
        }
        return objective.containsPlayer(playerID) ? objective.getProperty(propertyName, playerID) : "";
    }

}
