package avox.test.ticketToRide.game.gameHandler;

import avox.test.ticketToRide.game.core.game.Game;
import avox.test.ticketToRide.game.core.game.GamePlayer;
import avox.test.ticketToRide.guis.ActionManager;
import avox.test.ticketToRide.listener.PlayerGuiManager;
import avox.test.ticketToRide.guis.game.move.PickCardGui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;

public class MoveManager {
    private final GameHandler gameHandler;

    public MoveManager(GameHandler gameHandler) {
        this.gameHandler = gameHandler;
    }

    public void startMove(Game game, GamePlayer player) {
        gameHandler.currentTurn = player;
        game.broadcastTitle(Component.text(player.player.getName() + "'s turn!", NamedTextColor.GREEN), Component.empty(), player.player);
        game.broadcast(Component.text(player.player.getName() + "'s turn!", NamedTextColor.GREEN));
        player.player.showTitle(Title.title(Component.text("It's your turn!", NamedTextColor.GREEN), Component.empty()));

        gameHandler.setSelectActionHotbar(player);
        gameHandler.playerStateManager.put(player.player, new MoveAction(game, player.player, PlayerGuiManager.getGui(player.player).actionManager()));
        gameHandler.timerManager.startTimedAction(120, () -> {

        });
    }

    public void pickCards(GamePlayer player) {
        PickCardGui pickCardGui = new PickCardGui(gameHandler.game, player, PlayerGuiManager.getGui(player.player));
        player.player.openInventory(pickCardGui.gui);
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
            player.closeInventory();
            // TODO: Assign two random cards directly here, let placeRoute open the gui
            placeRoute();
        }
    }
}
