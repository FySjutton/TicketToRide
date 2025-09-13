package avox.test.ticketToRide.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class PlayerGuiManager implements Listener {
    private static final Map<Player, Inventory> playerInventories = new HashMap<>();

    public static void createGui(Inventory inventory, Player player) {
        playerInventories.put(player, inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory gui = playerInventories.get(player);

        if (gui != null && event.getInventory().equals(gui)) {
            event.setCancelled(true);

            if (event.getSlot() == 4) {
                player.sendMessage("Du klickade på diamanten!");
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
    }
}
