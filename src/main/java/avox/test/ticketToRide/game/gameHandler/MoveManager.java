package avox.test.ticketToRide.game.gameHandler;

import avox.test.ticketToRide.game.core.MapColor;
import avox.test.ticketToRide.game.core.game.Game;
import avox.test.ticketToRide.game.core.game.GamePlayer;
import avox.test.ticketToRide.guis.ActionManager;
import avox.test.ticketToRide.listener.PlayerGuiManager;
import avox.test.ticketToRide.guis.game.move.PickCardGui;
import avox.test.ticketToRide.util.GuiTools;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import java.util.AbstractMap.SimpleEntry;

import java.util.*;
import java.util.stream.Collectors;

public class MoveManager {
    private final GameHandler gameHandler;
    public Move currentMove;

    public static class Move {
        public GamePlayer player;

        // Pick card variables
        public ArrayList<PickCardGui.CardSelection> picked;
        public int capacity;
        public Component finalMessage;

        public Component actionMessage;
        public Runnable onFinish;

        public Move(Game game, GamePlayer player) {
            this.player = player;

            picked = new ArrayList<>();
            capacity = 2;
            finalMessage = null;
        }
    }

    public MoveManager(GameHandler gameHandler) {
        this.gameHandler = gameHandler;
    }

    public void startMove(Game game, GamePlayer player) {
        currentMove = new Move(game, player);

        game.broadcastTitle(Component.text(player.player.getName() + "'s turn!", NamedTextColor.GREEN), Component.empty(), player.player);
        game.broadcast(Component.text(player.player.getName() + "'s turn!", NamedTextColor.GREEN));
        player.player.showTitle(Title.title(Component.text("It's your turn!", NamedTextColor.GREEN), Component.empty()));

        gameHandler.setSelectActionHotbar(player);
        gameHandler.playerStateManager.put(player.player, new MoveAction(game, player, PlayerGuiManager.getGui(player.player).actionManager));

        gameHandler.timerManager.startTimedAction(120, () -> {
            player.overwriteAction = null;
            if (currentMove.onFinish != null) {
                currentMove.onFinish.run();
            }
            gameHandler.setDefaultHotbar(player, false);
            game.broadcast(currentMove.actionMessage, player.player);

            List<Player> players = new ArrayList<>(game.gamePlayers.keySet());
            int currentIndex = players.indexOf(player.player);
            int nextIndex = (currentIndex + 1) % players.size();
            GamePlayer next = game.gamePlayers.get(players.get(nextIndex));
            startMove(game, next);
        });
    }

    public void pickCards(Game game, GamePlayer player, PlayerGuiManager.PlayerEntry oldState) {
        currentMove.onFinish = () -> {
            Map<MapColor, Integer> frontCards = currentMove.picked.stream().filter(PickCardGui.CardSelection::fromUpface).collect(Collectors.toMap(PickCardGui.CardSelection::card, s -> 1, Integer::sum));
            int amountFrontUp = currentMove.picked.stream().filter(PickCardGui.CardSelection::fromUpface).toList().size();
            if (currentMove.picked.size() != amountFrontUp) {
                frontCards.put(new MapColor("Secret"), currentMove.picked.size() - amountFrontUp);
            }
            currentMove.actionMessage = gameHandler.newCardMessage(frontCards, player.player.getName() + " picked up the following cards:");
        };
        player.overwriteAction = () -> {
            PlayerGuiManager.removeOnClose(player.player);
            player.player.closeInventory();
            PickCardGui pickCardGui = new PickCardGui(player.game, player, oldState);
            player.player.openInventory(pickCardGui.gui);
        };
        player.overwriteAction.run();
    }

    public void placeRoute() {

    }

    public void pickRoutes() {

    }

    public class MoveAction extends GameHandler.PlayerState {
        private final GamePlayer player;

        public MoveAction(Game game, GamePlayer player, ActionManager actionManager) {
            super(game, player.player, actionManager, false);
            this.player = player;
        }

        @Override
        public void timeOut() {
            Random rand = new Random();
            ArrayList<MapColor> colors = game.gameMap.getAllColors();
            currentMove.actionMessage = Component.text(player.player.getName() + " ran out of time!", NamedTextColor.RED);
            player.player.sendMessage("§cYou ran out of time! You will automatically receive two cards.");
            int cards = currentMove.capacity;
            for (int i = 0; i < cards; i++) {
                PickCardGui.pick(game, currentMove, player, new PickCardGui.CardSelection(colors.get(rand.nextInt(0, colors.size())), true), 1);
            }
        }
    }
}
