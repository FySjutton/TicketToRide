package avox.test.ticketToRide.listener;

import avox.test.ticketToRide.guis.ActionManager;
import avox.test.ticketToRide.guis.GuiAction;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class PlayerGuiManager implements Listener {
    private static final HashMap<Player, PlayerEntry> entries = new HashMap<>();

    public record PlayerEntry(Player player, GuiInventory inventory, ActionManager actionManager, PlayerEntry onCloseEntry) {}

    public static PlayerEntry createGui(Inventory inventory, Player player, ActionManager slotActions, boolean hotbarOnly, PlayerEntry onCloseEntry) {
        PlayerEntry entry = new PlayerEntry(player, new GuiInventory(inventory, hotbarOnly), slotActions, onCloseEntry);
        entries.put(player, entry);
        return entry;
    }

    public static PlayerEntry getGui(Player player) {
        return entries.get(player);
    }

    public static void removeGui(Player player) {
        PlayerEntry entry = entries.get(player);
        if (entry != null) {
            if (entry.onCloseEntry != null) {
                entries.put(player, entry.onCloseEntry);
            } else {
                entries.remove(player);
            }
        }
    }

    public record GuiInventory(Inventory inventory, boolean hotbarOnly) {}

    // --- Helper methods ---
    private boolean isRegistered(Player player) {
        return entries.containsKey(player);
    }

    private void runSlotActions(Player player, int slot, boolean runHoldAction) {
        Map<Integer, ArrayList<GuiAction>> actions = new HashMap<>(entries.get(player).actionManager.actions);
        if (actions.containsKey(slot)) {
            List<GuiAction> slotActions = new ArrayList<>(actions.get(slot));

            for (GuiAction action : slotActions) {
                if (runHoldAction && action.holdAction != null) {
                    action.holdAction.run();
                } else if (!runHoldAction && action.clickAction != null) {
                    action.clickAction.run();
                }
            }
        }
    }

    // --- Event handlers ---
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!isRegistered(player)) return;

        Inventory gui = entries.get(player).inventory.inventory;
        if (!event.getInventory().equals(gui)) return;

        event.setCancelled(true);

        int slot = event.getSlot();

        runSlotActions(player, slot, false);
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!isRegistered(event.getPlayer())) return;

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR) return;

        if (action == Action.RIGHT_CLICK_BLOCK) {
            if (event.useInteractedBlock() == Event.Result.DENY) return;
        }

        ItemStack item = event.getItem();
        if (item == null) return;

        int slot = event.getPlayer().getInventory().getHeldItemSlot();
        runSlotActions(event.getPlayer(), slot, false);

        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!isRegistered(player)) return;
        Inventory gui = entries.get(player).inventory.inventory;

        if (event.getInventory().equals(gui)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (!isRegistered(player)) return;

        PlayerEntry entry = entries.get(player);
        if (!entry.inventory.hotbarOnly) {
            if (entry.onCloseEntry != null) {
                entries.put(player, entry.onCloseEntry);
            } else {
                entries.remove(player);
            }
        }
    }

    @EventHandler
    public void onHotbarChange(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        if (!isRegistered(player)) return;
        runSlotActions(player, event.getNewSlot(), true);
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (!isRegistered(player)) return;
        event.setCancelled(true);
    }
}