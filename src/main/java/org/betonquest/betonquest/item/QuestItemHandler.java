package org.betonquest.betonquest.item;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.val;
import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.Journal;
import org.betonquest.betonquest.api.profiles.Profile;
import org.betonquest.betonquest.config.Config;
import org.betonquest.betonquest.utils.PlayerConverter;
import org.betonquest.betonquest.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.ListIterator;

/**
 * Handler for Journals.
 */
@SuppressWarnings({"PMD.GodClass", "PMD.TooManyMethods", "PMD.CommentRequired", "PMD.CyclomaticComplexity"})
public class QuestItemHandler implements Listener {
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * Registers the quest item handler as Listener.
     */
    public QuestItemHandler() {
        Bukkit.getPluginManager().registerEvents(this, BetonQuest.getInstance());
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemDrop(final PlayerDropItemEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            return;
        }
        final Profile profile = PlayerConverter.getID(event.getPlayer());
        final ItemStack item = event.getItemDrop().getItemStack();
        if (Journal.isJournal(profile, item)) {
            if (isJournalSlotLocked()) {
                event.setCancelled(true);
            } else {
                event.getItemDrop().remove();
            }
        } else if (Utils.isQuestItem(item)) {
            BetonQuest.getInstance().getPlayerData(profile).addItem(item.clone(), item.getAmount());
            event.getItemDrop().remove();
        }
    }

    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.AvoidLiteralsInIfCondition", "PMD.CognitiveComplexity", "PMD.NPathComplexity"})
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    @EventHandler(ignoreCancelled = true)
    public void onItemMove(final InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        if (event.getWhoClicked().getGameMode() == GameMode.CREATIVE) {
            return;
        }
        final Profile profile = PlayerConverter.getID((Player) event.getWhoClicked());
        ItemStack item = null;
        switch (event.getAction()) {
            case PICKUP_ALL:
            case PICKUP_HALF:
            case PICKUP_ONE:
            case PICKUP_SOME:
                if (isJournalSlotLocked() && Journal.isJournal(profile, event.getCurrentItem())) {
                    event.setCancelled(true);
                    return;
                }
                break;
            case HOTBAR_MOVE_AND_READD:
            case HOTBAR_SWAP:
                if (event.getClickedInventory().getType() == InventoryType.PLAYER) {
                    if (isJournalSlotLocked()) {
                        final ItemStack swapped;
                        if (event.getHotbarButton() == -1 && "SWAP_OFFHAND".equals(event.getClick().name())) {
                            swapped = event.getWhoClicked().getInventory().getItemInOffHand();
                        } else {
                            swapped = event.getWhoClicked().getInventory().getItem(event.getHotbarButton());
                        }
                        if (Journal.isJournal(profile, event.getCurrentItem()) || Journal.isJournal(profile, swapped)) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                } else {
                    if (event.getHotbarButton() == -1 && "SWAP_OFFHAND".equals(event.getClick().name())) {
                        item = event.getWhoClicked().getInventory().getItemInOffHand();
                    } else {
                        item = event.getWhoClicked().getInventory().getItem(event.getHotbarButton());
                    }
                }
                break;
            case MOVE_TO_OTHER_INVENTORY:
                item = event.getCurrentItem();
                break;
            case PLACE_ALL:
            case PLACE_ONE:
            case PLACE_SOME:
            case SWAP_WITH_CURSOR:
                if (isJournalSlotLocked() && Journal.isJournal(profile, event.getCurrentItem())) {
                    event.setCancelled(true);
                    return;
                }
                if (event.getClickedInventory().getType() != InventoryType.PLAYER) {
                    item = event.getCursor();
                }
                break;
            default:
                break;
        }
        if (Journal.isJournal(profile, item) || Utils.isQuestItem(item)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemDrag(final InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        if (event.getWhoClicked().getGameMode() == GameMode.CREATIVE) {
            return;
        }
        final Profile profile = PlayerConverter.getID((Player) event.getWhoClicked());
        if (Journal.isJournal(profile, event.getOldCursor()) || Utils.isQuestItem(event.getOldCursor())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onArmorStandEquip(final PlayerArmorStandManipulateEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            return;
        }
        final ItemStack item = event.getPlayerItem();
        final Profile profile = PlayerConverter.getID(event.getPlayer());
        if (item != null && (Journal.isJournal(profile, item) || Utils.isQuestItem(item))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDeath(final PlayerDeathEvent event) {
        if (event.getEntity().getGameMode() == GameMode.CREATIVE) {
            return;
        }
        final Profile profile = PlayerConverter.getID(event.getEntity());
        // check if there is data for this player; NPCs don't have data
        if (BetonQuest.getInstance().getPlayerData(profile) == null) {
            return;
        }
        // this prevents the journal from dropping on death by removing it from
        // the list of drops
        final List<ItemStack> drops = event.getDrops();
        final ListIterator<ItemStack> litr = drops.listIterator();
        while (litr.hasNext()) {
            final ItemStack stack = litr.next();
            if (Journal.isJournal(profile, stack)) {
                litr.remove();
            }
            // remove all quest items and add them to backpack
            if (Utils.isQuestItem(stack)) {
                BetonQuest.getInstance().getPlayerData(profile).addItem(stack.clone(), stack.getAmount());
                litr.remove();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onRespawn(final PlayerRespawnEvent event) {
        if (Boolean.parseBoolean(Config.getString("config.remove_items_after_respawn"))) {
            // some plugins block item dropping after death and add those
            // items after respawning, so the player doesn't loose his
            // inventory after death; this aims to force removing quest
            // items, as they have been added to the backpack already
            if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
                return;
            }
            final Inventory inv = event.getPlayer().getInventory();
            for (int i = 0; i < inv.getSize(); i++) {
                if (Utils.isQuestItem(inv.getItem(i))) {
                    inv.setItem(i, null);
                }
            }
        }
        if (Boolean.parseBoolean(Config.getString("config.journal.give_on_respawn"))) {
            BetonQuest.getInstance().getPlayerData(PlayerConverter.getID(event.getPlayer())).getJournal().addToInv();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemFrameClick(final PlayerInteractEntityEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            return;
        }
        // this prevents the journal from being placed inside of item frame
        if (event.getRightClicked() instanceof ItemFrame) {
            final ItemStack item = (event.getHand() == EquipmentSlot.HAND) ? event.getPlayer().getInventory().getItemInMainHand()
                    : event.getPlayer().getInventory().getItemInOffHand();

            final Profile profile = PlayerConverter.getID(event.getPlayer());
            if (Journal.isJournal(profile, item) || Utils.isQuestItem(item)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(final BlockPlaceEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            return;
        }

        // this prevents players from placing "quest item" blocks
        val itemInHand = event.getItemInHand();
        if (Utils.isQuestItem(itemInHand)) {

            // *** Briar ***
            if (isProtectionBlockQuestItem(itemInHand)) {
                return;
            }
            // *** Briar ***

            event.setCancelled(true);
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler(ignoreCancelled = true)
    public void onItemBreak(final PlayerItemBreakEvent event) {
        if ("false".equalsIgnoreCase(BetonQuest.getInstance().getPluginConfig().getString("quest_items_unbreakable"))) {
            return;
        }
        // prevent quest items from breaking
        if (Utils.isQuestItem(event.getBrokenItem())) {
            final ItemStack original = event.getBrokenItem();
            original.setDurability((short) 0);
            final ItemStack copy = original.clone();
            event.getPlayer().getInventory().removeItem(original);
            event.getPlayer().getInventory().addItem(copy);
        }
    }

    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    @EventHandler()
    public void onInteractEvent(final PlayerInteractEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE || event.useInteractedBlock() == Event.Result.DENY && event.useItemInHand() == Event.Result.DENY) {
            return;
        }
        final ItemStack item = event.getItem();
        if (item != null && !EnchantmentTarget.TOOL.includes(item.getType()) && Utils.isQuestItem(item) && item.getType() != Material.WRITTEN_BOOK) {

            // *** Briar ***
            if (isProtectionBlockQuestItem(item)) {
                return;
            }
            // *** Briar ***

            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucketFillEvent(final PlayerBucketFillEvent event) {
        onBucketEvent(event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucketEmptyEvent(final PlayerBucketEmptyEvent event) {
        onBucketEvent(event);
    }

    public void onBucketEvent(final PlayerBucketEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            return;
        }
        final ItemStack itemMain = event.getPlayer().getInventory().getItemInMainHand();
        final ItemStack itemOff = event.getPlayer().getInventory().getItemInOffHand();
        if (Utils.isQuestItem(itemMain) || Utils.isQuestItem(itemOff)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerSwapHandItems(final PlayerSwapHandItemsEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
            return;
        }
        final Profile profile = PlayerConverter.getID(event.getPlayer());
        if (isJournalSlotLocked() && (Journal.isJournal(profile, event.getMainHandItem()) || Journal.isJournal(profile, event.getOffHandItem()))) {
            event.setCancelled(true);
        }
    }

    private boolean isJournalSlotLocked() {
        return Boolean.parseBoolean(Config.getString("config.journal.lock_default_journal_slot"));
    }

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    // *** Briar ***
    /** Added hard-coded claim protection stone name component */
    private static final String ITEM_PROTECTION_BLOCK = "Protection Block";

    /**
     * Determine if the specified ItemStack is a claim protection block.
     * @param item The item to test.
     * @return True if the specified ItemStack is a claim protection block, false otherwise.
     */
    private boolean isProtectionBlockQuestItem(final @Nullable ItemStack item) {
        return item != null && item.toString().contains(ITEM_PROTECTION_BLOCK);
    }
    // *** Briar ***
}
