package org.betonquest.betonquest.quest.event.scoreboard;

import org.betonquest.betonquest.VariableNumber;
import org.betonquest.betonquest.api.profiles.Profile;
import org.betonquest.betonquest.api.quest.event.Event;
import org.betonquest.betonquest.exceptions.QuestRuntimeException;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

/**
 * Adds/removes/multiplies/divides scores on scoreboards.
 */
public class ScoreboardEvent implements Event {
    /**
     * The name of the objective.
     */
    private final String objective;

    /**
     * The number to modify the score by.
     */
    private final VariableNumber count;

    /**
     * The modification to apply to the score.
     */
    private final ScoreModification scoreModification;

    /**
     * Creates a new ScoreboardEvent.
     *
     * @param objective         the name of the objective
     * @param count             the number to modify the score by
     * @param scoreModification the modification to apply to the score
     */
    public ScoreboardEvent(final String objective, final VariableNumber count, final ScoreModification scoreModification) {
        this.objective = objective;
        this.count = count;
        this.scoreModification = scoreModification;
    }

    @Override
    public void execute(final Profile profile) throws QuestRuntimeException {
        final Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        final Objective obj = board.getObjective(objective);
        if (obj == null) {
            throw new QuestRuntimeException("Scoreboard objective " + objective + " does not exist!");
        }
        final Score score = obj.getScore(profile.getPlayer());
        score.setScore(scoreModification.modify(score.getScore(), count.getDouble(profile)));
    }
}
