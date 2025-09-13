package avox.test.ticketToRide.guis.createGame;

import avox.test.ticketToRide.utils.PlayerGuiManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.awt.*;

public class CreateGameGui {
    private final Inventory gui;

    private ItemStack mapSetting;
    private ItemStack arenaSetting;

    public CreateGameGui(Player player) {
        gui = Bukkit.createInventory(null, 27, "Game GUI");

        mapSetting = GuiTools.createHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTUwMTU0NzBlMjg2ZTRlZDc3YTAzODc2Y2JiZmQ3YjNkMzU4YTYwNjA2YjQ0NmQyYzRiYzhkOGU5YzM3M2VlOSJ9fX0=");
        mapSetting.editMeta(meta -> meta.customName(Component.text("Map", NamedTextColor.YELLOW).decoration(TextDecoration.BOLD, true)));
        gui.setItem(11, mapSetting);

        arenaSetting = new ItemStack(Material.RED_CONCRETE);
        gui.setItem(13, arenaSetting);

        gui.setItem(15, new ItemStack(Material.RED_CONCRETE));

        PlayerGuiManager.createGui(gui, player);
    }

    public Inventory getInventory() {
        return gui;
    }
}
