package avox.test.ticketToRide.game.gameHandler;

import avox.test.ticketToRide.game.DestinationCard;
import avox.test.ticketToRide.game.Game;
import avox.test.ticketToRide.game.player.GamePlayer;
import avox.test.ticketToRide.guis.ActionManager;
import avox.test.ticketToRide.guis.PlayerGuiManager;
import avox.test.ticketToRide.guis.game.ViewInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class MoveManager {
    private GameHandler gameHandler;

    public MoveManager(GameHandler gameHandler) {
        this.gameHandler = gameHandler;
    }

    public void startMove(Game game, GamePlayer player) {
        gameHandler.currentTurn = player;
        game.broadcastTitle(Component.text("§a" + player.player.getName() + "'s turn!"), Component.empty(), player.player);
        game.broadcast(Component.text("§a" + player.player.getName() + "'s turn!"));
        player.player.showTitle(Title.title(Component.text("§aIt's your turn!"), Component.empty()));

        gameHandler.setDefaultHotbar(player, true);
        gameHandler.playerStateManager.put(player.player, new MoveAction(game, player.player, PlayerGuiManager.getGui(player.player).actionManager()));
        gameHandler.timerManager.startTimedAction(120, () -> {

        });
    }

    public void pickCards() {

    }

    public void placeRoute() {

    }

    public void pickRoutes() {

    }

    public class MoveAction extends GameHandler.PlayerState {
        public MoveAction(Game game, Player player, ActionManager actionManager) {
            super(game, player, actionManager, false);
        }

        @Override
        public void timeOut() {
            // TODO: Assign two random cards directly here, let placeRoute open the gui
            placeRoute();
        }
    }
}
