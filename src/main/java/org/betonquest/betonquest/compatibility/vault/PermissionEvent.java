package org.betonquest.betonquest.compatibility.vault;

import net.milkbowl.vault.permission.Permission;
import org.betonquest.betonquest.Instruction;
import org.betonquest.betonquest.api.QuestEvent;
import org.betonquest.betonquest.api.profiles.Profile;
import org.betonquest.betonquest.exceptions.InstructionParseException;
import org.bukkit.OfflinePlayer;

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
        final OfflinePlayer player = profile.getOfflinePlayer();
        if (add) {
            if (perm) {
                vault.playerAdd(world, player, permission);
            } else {
                vault.playerAddGroup(world, player, permission);
            }
        } else {
            if (perm) {
                vault.playerRemove(world, player, permission);
            } else {
                vault.playerRemoveGroup(world, player, permission);
            }
        }
        return null;
    }
}
