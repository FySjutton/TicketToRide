package avox.test.ticketToRide.guis;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

public class InventoryGui {
    public final Inventory gui;
    public final Map<Integer, Runnable> actions = new HashMap<>();

    public InventoryGui(Player player, int size, Component name) {
        gui = Bukkit.createInventory(null, size, name);
        PlayerGuiManager.createGui(gui, player, actions);
    }

    public Inventory getInventory() {
        return gui;
    }
}
