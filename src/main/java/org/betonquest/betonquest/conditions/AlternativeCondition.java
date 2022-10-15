package org.betonquest.betonquest.conditions;

import lombok.CustomLog;
import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.Instruction;
import org.betonquest.betonquest.api.Condition;
import org.betonquest.betonquest.api.profiles.Profile;
import org.betonquest.betonquest.exceptions.InstructionParseException;
import org.betonquest.betonquest.id.ConditionID;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * One of specified conditions has to be true
 */
@SuppressWarnings("PMD.CommentRequired")
@CustomLog
public class AlternativeCondition extends Condition {

    private final List<ConditionID> conditionIDs;

    public AlternativeCondition(final Instruction instruction) throws InstructionParseException {
        super(instruction, false);
        conditionIDs = instruction.getList(instruction::getCondition);
    }

    @SuppressWarnings("PMD.CognitiveComplexity")
    @Override
    protected Boolean execute(final Profile profile) {
        if (Bukkit.isPrimaryThread()) {
            for (final ConditionID id : conditionIDs) {
                if (BetonQuest.condition(profile, id)) {
                    return true;
                }
            }
        } else {
            final List<CompletableFuture<Boolean>> conditions = new ArrayList<>();
            for (final ConditionID id : conditionIDs) {
                final CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(
                        () -> BetonQuest.condition(profile, id));
                conditions.add(future);
            }
            for (final CompletableFuture<Boolean> condition : conditions) {
                try {
                    if (condition.get()) {
                        return true;
                    }
                } catch (final InterruptedException | ExecutionException e) {
                    LOG.reportException(instruction.getPackage(), e);
                    return false;
                }
            }
        }
        return false;
    }
}
