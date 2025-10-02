package avox.test.ticketToRide.game.gameHandler;

import avox.test.ticketToRide.game.core.DestinationCard;
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
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class MoveManager {
    private final GameHandler gameHandler;
    public Move currentMove;

    public static class Move {
        public GamePlayer player;

        // Pick card variables
        public ArrayList<PickCardGui.CardSelection> pickedColorCards;
        public int capacity;
        public Component finalMessage;

        // Pick destination card variables
        public List<DestinationCard> selectedDestinationCards;

        public Component actionMessage;
        public Runnable onFinish;

        public Move(GamePlayer player) {
            this.player = player;

            pickedColorCards = new ArrayList<>();
            capacity = 2;
            finalMessage = null;
        }
    }

    public MoveManager(GameHandler gameHandler) {
        this.gameHandler = gameHandler;
    }

    public void startMove(Game game, GamePlayer player) {
        currentMove = new Move(player);

        game.broadcastTitle(Component.text(player.player.getName() + "'s turn!", NamedTextColor.GREEN), Component.empty(), player.player);
        game.broadcast(Component.text(player.player.getName() + "'s turn!", NamedTextColor.GREEN));
        player.player.showTitle(Title.title(Component.text("It's your turn!", NamedTextColor.GREEN), Component.empty()));

        player.hotbarAction = new GameHandler.HotbarAction(GuiTools.getYellow("Choose Action"), () -> gameHandler.startViewInfo(player.player), 4, null);

        gameHandler.setHotbar(player);
        gameHandler.playerStateManager.put(player.player, new MoveAction(game, player, PlayerGuiManager.getGui(player.player).actionManager));

        gameHandler.timerManager.startTimedAction(120, () -> {
            gameHandler.setDefaultHotbar(player);
            if (currentMove.onFinish != null) {
                currentMove.onFinish.run();
            }
            gameHandler.setHotbar(player);
            game.broadcast(currentMove.actionMessage, player.player);

            List<Player> players = new ArrayList<>(game.gamePlayers.keySet());
            int currentIndex = players.indexOf(player.player);
            int nextIndex = (currentIndex + 1) % players.size();
            GamePlayer next = game.gamePlayers.get(players.get(nextIndex));
            startMove(game, next);
        });
    }

    public void pickCards(GamePlayer player) {
        currentMove.onFinish = () -> {
            Map<MapColor, Integer> frontCards = currentMove.pickedColorCards.stream().filter(PickCardGui.CardSelection::fromUpface).collect(Collectors.toMap(PickCardGui.CardSelection::card, s -> 1, Integer::sum));
            int amountFrontUp = currentMove.pickedColorCards.stream().filter(PickCardGui.CardSelection::fromUpface).toList().size();
            if (currentMove.pickedColorCards.size() != amountFrontUp) {
                frontCards.put(new MapColor("Secret"), currentMove.pickedColorCards.size() - amountFrontUp);
            }
            currentMove.actionMessage = gameHandler.newCardMessage(frontCards, player.player.getName() + " picked up the following cards:");
        };
        player.hotbarAction.compassAction = () -> {
            player.player.closeInventory();
            PickCardGui pickCardGui = new PickCardGui(player.game, player);
            player.player.openInventory(pickCardGui.gui);
        };
        player.hotbarAction.compassAction.run();
    }

    public void placeRoute() {

    }

    public void pickRoutes(GamePlayer player) {
        currentMove.onFinish = () -> {
            currentMove.actionMessage = GuiTools.colorize(player.player.getName() + " picked up " + currentMove.selectedDestinationCards.size() + " cards!", NamedTextColor.GREEN);
            player.player.sendMessage(GuiTools.colorize("Turn finished! " + currentMove.selectedDestinationCards.size() + " cards selected!", NamedTextColor.GREEN));
        };

        player.player.sendMessage(GuiTools.colorize("You selected pick up destination cards! You must save at least one, but you can save more if you'd like!", NamedTextColor.GREEN));
        gameHandler.destinationHandler.chooseDestinationCards(player.game, player, 1);
    }

    public class MoveAction extends GameHandler.PlayerState {
        public MoveAction(Game game, GamePlayer player, ActionManager actionManager) {
            super(game, player, actionManager, false);
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
