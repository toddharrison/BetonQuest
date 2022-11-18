package org.betonquest.betonquest.conversation;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.profiles.OnlineProfile;
import org.betonquest.betonquest.config.Config;
import org.betonquest.betonquest.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

/**
 * Base of all chat conversation outputs
 */
@SuppressWarnings("PMD.CommentRequired")
public abstract class ChatConvIO implements ConversationIO, Listener {

    protected final Conversation conv;
    protected final String name;
    protected final Player player;
    protected final Map<String, ChatColor[]> colors;
    private final String npcTextColor;
    private final double maxNpcDistance;
    protected int optionsCount;
    protected Map<Integer, String> options;
    protected String npcText;
    protected String npcName;
    protected String answerFormat;
    protected String textFormat;

    public ChatConvIO(final Conversation conv, final OnlineProfile onlineProfile) {
        this.options = new HashMap<>();
        this.conv = conv;
        this.player = onlineProfile.getPlayer();
        this.name = player.getName();
        this.colors = ConversationColors.getColors();
        StringBuilder string = new StringBuilder();
        for (final ChatColor color : colors.get("npc")) {
            string.append(color);
        }
        string.append("%npc%").append(ChatColor.RESET).append(": ");

        final StringBuilder textColorBuilder = new StringBuilder();
        for (final ChatColor color : colors.get("text")) {
            textColorBuilder.append(color);
        }
        npcTextColor = textColorBuilder.toString();

        string.append(npcTextColor);
        textFormat = string.toString();
        string = new StringBuilder();
        for (final ChatColor color : colors.get("player")) {
            string.append(color);
        }
        string.append(name).append(ChatColor.RESET).append(": ");
        for (final ChatColor color : colors.get("answer")) {
            string.append(color);
        }
        answerFormat = string.toString();
        Bukkit.getPluginManager().registerEvents(this, BetonQuest.getInstance());
        maxNpcDistance = Double.parseDouble(Config.getString("config.max_npc_distance"));
    }

    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    @EventHandler(ignoreCancelled = true)
    public void onWalkAway(final PlayerMoveEvent event) {
        // return if it's someone else
        if (!event.getPlayer().equals(player)) {
            return;
        }
        // if player passes max distance
        if (!event.getTo().getWorld().equals(conv.getLocation().getWorld()) || event.getTo()
                .distance(conv.getLocation()) > maxNpcDistance) {
            // we can stop the player or end conversation
            if (conv.isMovementBlock()) {
                moveBack(event);
            } else {
                conv.endConversation();
            }
        }
    }

    /**
     * Moves the player back a few blocks in the conversation's center
     * direction.
     *
     * @param event PlayerMoveEvent event, for extracting the necessary data
     */
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    private void moveBack(final PlayerMoveEvent event) {
        // if the player is in other world (he teleported himself), teleport him
        // back to the center of the conversation
        if (!event.getTo().getWorld().equals(conv.getLocation().getWorld()) || event.getTo()
                .distance(conv.getLocation()) > maxNpcDistance * 2) {
            event.getPlayer().teleport(conv.getLocation());
            return;
        }
        // if not, then calculate the vector
        final float yaw = event.getTo().getYaw();
        final float pitch = event.getTo().getPitch();
        Vector vector = new Vector(conv.getLocation().getX() - event.getTo().getX(),
                conv.getLocation().getY() - event.getTo().getY(), conv.getLocation().getZ() - event.getTo().getZ());
        vector = vector.multiply(1 / vector.length());
        // and teleport him back using this vector
        final Location newLocation = event.getTo().clone();
        newLocation.add(vector);
        newLocation.setPitch(pitch);
        newLocation.setYaw(yaw);
        event.getPlayer().teleport(newLocation);
        if ("true".equalsIgnoreCase(Config.getString("config.notify_pullback"))) {
            conv.sendMessage(Config.getMessage(Config.getLanguage(), "pullback"));
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onReply(final AsyncPlayerChatEvent event) {
        if (!event.getPlayer().equals(player)) {
            return;
        }
        final String message = event.getMessage().trim();
        for (final Map.Entry<Integer, String> entry : options.entrySet()) {
            final int index = entry.getKey();
            if (message.equals(Integer.toString(index))) {
                conv.sendMessage(answerFormat + entry.getValue());
                conv.passPlayerAnswer(index);
                event.setCancelled(true);
                return;
            }
        }
        // redisplay the conversation after player's message so he can see it
        new BukkitRunnable() {
            @Override
            public void run() {
                display();
            }
        }.runTask(BetonQuest.getInstance());
    }

    @Override
    public void setNpcResponse(final String npcName, final String response) {
        this.npcName = npcName;
        this.npcText = response;
    }

    @Override
    public void addPlayerOption(final String option) {
        optionsCount++;
        options.put(optionsCount, option);
    }

    @Override
    public void display() {
        if (npcText == null && options.isEmpty()) {
            end();
            return;
        }
        conv.sendMessage(Utils.replaceReset(textFormat.replace("%npc%", npcName) + npcText, npcTextColor));
    }

    @Override
    public void clear() {
        optionsCount = 0;
        options.clear();
        npcText = null;
    }

    @Override
    public void end() {
        HandlerList.unregisterAll(this);
    }

    @Override
    public void print(final String message) {
        if (message != null && message.length() > 0) {
            conv.sendMessage(message);
        }
    }
}
