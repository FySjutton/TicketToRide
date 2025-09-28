package avox.test.ticketToRide.game.gameHandler;

import avox.test.ticketToRide.game.Game;
import avox.test.ticketToRide.game.player.GamePlayer;
import net.kyori.adventure.text.Component;

public class MoveManager {
    private GameHandler gameHandler;

    public MoveManager(GameHandler gameHandler) {
        this.gameHandler = gameHandler;
    }

    public void startMove(Game game, GamePlayer player) {
        game.broadcastTitle(Component.text("§a" + player.player.getName() + "'s turn!"), Component.empty(), player.player);
    }
}
