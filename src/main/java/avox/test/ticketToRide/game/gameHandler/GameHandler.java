package avox.test.ticketToRide.game.gameHandler;

import avox.test.ticketToRide.game.DestinationCard;
import avox.test.ticketToRide.game.Game;
import avox.test.ticketToRide.game.player.GamePlayer;
import avox.test.ticketToRide.guis.ActionManager;
import avox.test.ticketToRide.guis.GuiAction;
import avox.test.ticketToRide.guis.GuiTools;
import avox.test.ticketToRide.guis.PlayerGuiManager;
import avox.test.ticketToRide.guis.game.ViewInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.checkerframework.checker.units.qual.A;

import java.util.HashMap;
import java.util.List;

import static avox.test.ticketToRide.TicketToRide.plugin;

public class GameHandler {
    protected Game game;
    protected HashMap<Player, PlayerState> playerStateManager = new HashMap<>();

    private final DestinationHandler destinationHandler = new DestinationHandler(this);
    private final TimerManager timerManager = new TimerManager(this);

    public GameHandler(Game game) {
        this.game = game;

        // Start by letting all players choose at least 2 cards
        for (Player player : game.gamePlayers.keySet()) {
            player.showTitle(Title.title(Component.text("Select Destination Cards"), Component.text("Select at least 2")));
            destinationHandler.chooseDestinationCards(game, player, 2);
        }
        timerManager.startTimedAction(120, () -> {
            game.broadcast("§aAll players have finished choosing their routes.");
        });
    }

    public void setDefaultHotbar(Player player) {
        PlayerGuiManager.PlayerEntry oldEntry = PlayerGuiManager.getGui(player);
        ActionManager actionManager = new ActionManager();
        PlayerGuiManager.createGui(player.getInventory(), player, actionManager, true, null);

        actionManager.addAction(player.getInventory(), GuiTools.format(new ItemStack(Material.COMPASS), GuiTools.getYellow("Show Info")), 8, GuiAction.ofClick(() -> {
            ViewInfo viewInfo = new ViewInfo(game, player, oldEntry);
            player.openInventory(viewInfo.gui);
        }));
    }

    public static abstract class PlayerState {
        public Game game;
        public Player player;
        public ActionManager actionManager;
        public boolean finished = false;

        public PlayerState(Game game, Player player, ActionManager actionManager) {
            this.game = game;
            this.player = player;
            this.actionManager = actionManager;

            PlayerGuiManager.createGui(player.getInventory(), player, actionManager, true, null);
        }

        public abstract void timeOut();
    }
}
