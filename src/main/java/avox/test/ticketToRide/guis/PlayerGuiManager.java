package avox.test.ticketToRide.guis;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

public class PlayerGuiManager implements Listener {
    private static final Map<Player, GuiInventory> playerInventories = new HashMap<>();
    private static final Map<Player, Map<Integer, GuiAction>> playerActions = new HashMap<>();

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

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!isRegistered(player)) return;
        Inventory gui = playerInventories.get(player).inventory;

        if (event.getInventory().equals(gui)) {
            event.setCancelled(true);

            int slot = event.getSlot();
            Map<Integer, GuiAction> actions = playerActions.get(player);

            if (actions != null && actions.containsKey(slot)) {
                GuiAction action = actions.get(slot);
                if (action.clickAction != null) {
                    actions.get(slot).clickAction.run();
                }
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (isRegistered(player)) {
            Inventory gui = playerInventories.get(player).inventory;

            if (event.getInventory().equals(gui)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (isRegistered(player)) {
            if (playerInventories.get(player).hotbarOnly) {
                playerInventories.remove(player);
                playerActions.remove(player);
            }
        }

    }

    @EventHandler
    public void onHotbarChange(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        if (!isRegistered(player)) return;
        Map<Integer, GuiAction> gui = playerActions.get(player);
        if (gui.containsKey(event.getNewSlot())) {
            GuiAction action = gui.get(event.getNewSlot());
            if (action.holdAction != null) {
                action.holdAction.run();
            }
        }
    }

    private boolean isRegistered(Player player) {
        return playerInventories.containsKey(player);
    }
}
