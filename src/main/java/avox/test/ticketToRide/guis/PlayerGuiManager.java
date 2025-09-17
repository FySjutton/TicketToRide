package avox.test.ticketToRide.guis;

import org.bukkit.entity.Player;
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
import java.util.Map;
import java.util.ArrayList;

public class PlayerGuiManager implements Listener {
    private static final Map<Player, GuiInventory> playerInventories = new HashMap<>();
    private static final Map<Player, Map<Integer, ArrayList<GuiAction>>> playerActions = new HashMap<>();

    public static void createGui(Inventory inventory, Player player, ActionManager slotActions, boolean hotbarOnly) {
        playerInventories.put(player, new GuiInventory(inventory, hotbarOnly));
        playerActions.put(player, slotActions.actions);
    }

    public static class GuiInventory {
        public Inventory inventory;
        public boolean hotbarOnly;

        public GuiInventory(Inventory inventory, boolean hotbarOnly) {
            this.inventory = inventory;
            this.hotbarOnly = hotbarOnly;
        }
    }

    // --- Helper methods ---
    private boolean isRegistered(Player player) {
        return playerInventories.containsKey(player);
    }

    private void runSlotActions(Player player, int slot, boolean runHoldAction) {
        Map<Integer, ArrayList<GuiAction>> actions = playerActions.get(player);
        if (actions != null && actions.containsKey(slot)) {
            for (GuiAction action : actions.get(slot)) {
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

        Inventory gui = playerInventories.get(player).inventory;
        if (!event.getInventory().equals(gui)) return;

        event.setCancelled(true);

        int slot = event.getSlot();

        runSlotActions(player, slot, false);
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!isRegistered(player)) return;

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = event.getItem();
            if (item != null && event.getHand() != null) {
                int slot;
                if (event.getHand() == EquipmentSlot.HAND) {
                    slot = player.getInventory().getHeldItemSlot();
                } else {
                    slot = 40;
                }
                runSlotActions(player, slot, false);
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!isRegistered(player)) return;
        Inventory gui = playerInventories.get(player).inventory;

        if (event.getInventory().equals(gui)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (!isRegistered(player)) return;

        if (playerInventories.get(player).hotbarOnly) {
            playerInventories.remove(player);
            playerActions.remove(player);
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