package avox.test.ticketToRide.guis;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

public class InventoryGui {
    public final Inventory gui;
    public final ActionManager actionManager = new ActionManager();

    public InventoryGui(Player player, int size, Component name, PlayerGuiManager.PlayerEntry onClose) {
        gui = Bukkit.createInventory(null, size, name);
        PlayerGuiManager.createGui(gui, player, actionManager, false, onClose);
    }

    public Inventory getInventory() {
        return gui;
    }
}
