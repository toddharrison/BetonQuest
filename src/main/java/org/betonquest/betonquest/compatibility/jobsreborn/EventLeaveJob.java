package org.betonquest.betonquest.compatibility.jobsreborn;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.container.Job;
import org.betonquest.betonquest.Instruction;
import org.betonquest.betonquest.api.QuestEvent;
import org.betonquest.betonquest.api.profiles.Profile;
import org.betonquest.betonquest.exceptions.InstructionParseException;

@SuppressWarnings("PMD.CommentRequired")
public class EventLeaveJob extends QuestEvent {
    private final String sJobName;

    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    public EventLeaveJob(final Instruction instructions) throws InstructionParseException {
        super(instructions, true);

        if (instructions.size() < 2) {
            throw new InstructionParseException("Not enough arguments");
        }
        for (final Job job : Jobs.getJobs()) {
            if (job.getName().equalsIgnoreCase(instructions.getPart(1))) {
                sJobName = job.getName();
                return;
            }
        }
        throw new InstructionParseException("Jobs Reborn job " + instructions.getPart(1) + " does not exist");
    }

    @Override
    protected Void execute(final Profile profile) {
        for (final Job job : Jobs.getJobs()) {
            if (job.getName().equalsIgnoreCase(sJobName)) {
                Jobs.getPlayerManager().getJobsPlayer(profile.getPlayerUUID()).leaveJob(job);
                return null;
            }
        }
        return null;
    }
}
