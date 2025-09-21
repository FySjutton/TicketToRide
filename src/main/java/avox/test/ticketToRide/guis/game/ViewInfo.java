package avox.test.ticketToRide.guis.game;

import avox.test.ticketToRide.game.Game;
import avox.test.ticketToRide.game.MapColor;
import avox.test.ticketToRide.game.player.GamePlayer;
import avox.test.ticketToRide.guis.GuiTools;
import avox.test.ticketToRide.guis.InventoryGui;
import avox.test.ticketToRide.guis.PlayerGuiManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ViewInfo extends InventoryGui {
    public ViewInfo(Game game, Player user, PlayerGuiManager.PlayerEntry oldState) {
        super(user, 27, Component.text(user.getName() + " Game Info"), oldState);

        GamePlayer player = game.gamePlayers.get(user);
        int slot = 0;
        for (MapColor color : player.cards.keySet()) {
            int cards = player.cards.get(color);
            ItemStack stack;
            if (cards == 0) {
                stack = new ItemStack(Material.BARRIER);
            } else {
                stack = new ItemStack(color.material).add(cards - 1);
            }
            gui.setItem(9 + slot, GuiTools.format(stack, GuiTools.getYellow("Card ").append(color.colored.decoration(TextDecoration.ITALIC, false).decorate(TextDecoration.BOLD))));
            slot++;
        }
    }
}
