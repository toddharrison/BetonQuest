package org.betonquest.betonquest.compatibility.vault;

import net.milkbowl.vault.permission.Permission;
import org.betonquest.betonquest.Instruction;
import org.betonquest.betonquest.api.QuestEvent;
import org.betonquest.betonquest.api.profiles.Profile;
import org.betonquest.betonquest.exceptions.InstructionParseException;

/**
 * Manages player's permissions
 */
@SuppressWarnings("PMD.CommentRequired")
public class PermissionEvent extends QuestEvent {

    private final String world;
    private final String permission;
    private final boolean add;
    private final boolean perm;

    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    public PermissionEvent(final Instruction instruction) throws InstructionParseException {
        super(instruction, true);
        add = "add".equalsIgnoreCase(instruction.next());
        perm = "perm".equalsIgnoreCase(instruction.next());
        permission = instruction.next();
        if (instruction.size() >= 5) {
            world = instruction.next();
        } else {
            world = null;
        }
    }

    @Override
    protected Void execute(final Profile profile) {
        final Permission vault = VaultIntegrator.getPermission();
        if (add) {
            if (perm) {
                vault.playerAdd(world, profile.getPlayer(), permission);
            } else {
                vault.playerAddGroup(world, profile.getPlayer(), permission);
            }
        } else {
            if (perm) {
                vault.playerRemove(world, profile.getPlayer(), permission);
            } else {
                vault.playerRemoveGroup(world, profile.getPlayer(), permission);
            }
        }
        return null;
    }
}
