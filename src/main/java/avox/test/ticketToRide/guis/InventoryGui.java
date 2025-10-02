package avox.test.ticketToRide.guis;

import avox.test.ticketToRide.game.core.game.GamePlayer;
import avox.test.ticketToRide.listener.PlayerGuiManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class InventoryGui {
    public final Inventory gui;
    public final ActionManager actionManager = new ActionManager();

    public InventoryGui(Player player, int size, Component name) {
        gui = Bukkit.createInventory(null, size, name);
        PlayerGuiManager.createGui(gui, player, actionManager, false);
    }

    public Inventory getInventory() {
        return gui;
    }
}
