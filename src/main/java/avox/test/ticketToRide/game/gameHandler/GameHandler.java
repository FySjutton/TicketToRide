package avox.test.ticketToRide.game.gameHandler;

import avox.test.ticketToRide.game.DestinationCard;
import avox.test.ticketToRide.game.Game;
import avox.test.ticketToRide.guis.ActionManager;
import avox.test.ticketToRide.guis.GuiAction;
import avox.test.ticketToRide.guis.GuiTools;
import avox.test.ticketToRide.guis.PlayerGuiManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.List;

public class GameHandler {
    protected Game game;
    protected HashMap<Player, PlayerState> playerStateManager = new HashMap<>();

    private final DestinationHandler destinationHandler = new DestinationHandler(this);

    public GameHandler(Game game) {
        this.game = game;

        for (Player player : game.gamePlayers.keySet()) {
            player.showTitle(Title.title(Component.text("Select Destination Cards"), Component.text("Select at least 2")));
            destinationHandler.chooseDestinationCards(game, player, 2, (newCards) -> {

            });
        }
    }

    public static class PlayerState {
        public Player player;
        public ActionManager actionManager;

        public PlayerState(Player player, ActionManager actionManager) {
            this.player = player;

            PlayerGuiManager.createGui(player.getInventory(), player, actionManager, true);
        }
    }
}
