package avox.test.ticketToRide.guis;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

public class PlayerGuiManager implements Listener {
    private static final Map<Player, Inventory> playerInventories = new HashMap<>();
    private static final Map<Player, Map<Integer, Runnable>> playerClickActions = new HashMap<>();

    public static void createGui(Inventory inventory, Player player, Map<Integer, Runnable> slotActions) {
        playerInventories.put(player, inventory);
        playerClickActions.put(player, slotActions);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory gui = playerInventories.get(player);

        if (gui != null && event.getInventory().equals(gui)) {
            event.setCancelled(true);

            int slot = event.getSlot();
            Map<Integer, Runnable> actions = playerClickActions.get(player);

            if (actions != null && actions.containsKey(slot)) {
                actions.get(slot).run();
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory gui = playerInventories.get(player);

        if (gui != null && event.getInventory().equals(gui)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        playerInventories.remove(player);
        playerClickActions.remove(player);
    }
}
