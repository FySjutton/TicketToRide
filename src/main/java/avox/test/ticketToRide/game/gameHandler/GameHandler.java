package avox.test.ticketToRide.game.gameHandler;

import avox.test.ticketToRide.TicketToRide;
import avox.test.ticketToRide.game.Countdown;
import avox.test.ticketToRide.game.RewardCalculator;
import avox.test.ticketToRide.game.core.Route;
import avox.test.ticketToRide.game.core.game.Game;
import avox.test.ticketToRide.game.core.MapColor;
import avox.test.ticketToRide.game.core.game.GamePlayer;
import avox.test.ticketToRide.guis.ActionManager;
import avox.test.ticketToRide.guis.GuiAction;
import avox.test.ticketToRide.util.GuiTools;
import avox.test.ticketToRide.listener.PlayerGuiManager;
import avox.test.ticketToRide.guis.game.ViewInfo;
import avox.test.ticketToRide.util.LeaderboardMessage;
import avox.test.ticketToRide.util.Waiter;
import avox.test.ticketToRide.util.board.MarkerManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.units.qual.A;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class GameHandler {
    public Game game;
    public HashMap<Player, PlayerState> playerStateManager = new HashMap<>();

    public final DestinationHandler destinationHandler = new DestinationHandler(this);
    public final MoveManager moveManager = new MoveManager(this);
    public final TimerManager timerManager = new TimerManager(this);

    public GameHandler(Game game) {
        this.game = game;
        Random rand = new Random();
        new Countdown(TicketToRide.plugin, 10, (seconds) -> game.broadcastTitle(Component.text(seconds, Countdown.getColor(seconds)), Component.text("Game starting in...", NamedTextColor.YELLOW), 1.5f), () -> {
            // Start by letting all players choose at least 2 cards
            game.broadcastTitle(Component.text("Select Destination Cards", NamedTextColor.YELLOW), Component.text("Select at least 2", NamedTextColor.GRAY));
            game.broadcast(Component.text("Select destination cards! You must keep at least 2.", NamedTextColor.YELLOW));
            for (GamePlayer player : game.gamePlayers.values()) {
                destinationHandler.chooseDestinationCards(game, player, 2);
            }
            timerManager.startTimedAction(120, () -> {
                game.broadcast("§aAll players have finished choosing their routes.");
                giveStartingCards(rand);

                // Initiate the move cycle
                moveManager.startMove(game, game.gamePlayers.values().stream().toList().get(rand.nextInt(0, game.gamePlayers.size())));
            });
        }).start();
    }

    public void gameFinished() {
        playerStateManager.clear();
        closeInventories();
        PlayerGuiManager.clear();
        game.broadcastTitle(GuiTools.colorize("Game over!", NamedTextColor.GREEN), Component.empty(), 2.5f);
        new Waiter(TicketToRide.plugin).waitSeconds(2, () -> {
            HashMap<GamePlayer, Integer> routeLengths = new HashMap<>();
            for (GamePlayer player : game.gamePlayers.values()) {
                routeLengths.put(player, RewardCalculator.getLongestContinuousRoute(player));
            }
            int longestRoute = Collections.max(routeLengths.values());
            List<GamePlayer> longestWinners = routeLengths.keySet().stream().filter(player -> routeLengths.get(player) == longestRoute).toList();
            for (GamePlayer player : longestWinners) {
                player.points += 10;
                MarkerManager.reposition(game, player, player.points);
            }

            String message;
            String titleMessage;
            if (longestWinners.size() == 1) {
                GamePlayer winner = longestWinners.getFirst();
                message = winner.player.getName() + " received 10 bonus points for having the longest continuous route!";
                titleMessage = winner.player.getName();
            } else {
                String names = longestWinners.stream()
                        .map(p -> p.player.getName())
                        .collect(Collectors.joining(", "));
                message = names + " each received 10 bonus points for tying for the longest continuous route!";
                titleMessage = longestWinners.size() + " players";
            }
            game.broadcast(Component.text(message, NamedTextColor.GREEN));
            game.broadcastTitle(Component.text(titleMessage + " wins the...", NamedTextColor.GREEN), Component.text("Longest continuous route price!", NamedTextColor.YELLOW), 3.5f);
        }).waitSeconds(3, () -> game.broadcastTitle(Component.text("The winner is...", NamedTextColor.YELLOW), Component.empty(), 5)).waitSeconds( 4, () -> {
            int mostPoints = Collections.max(game.gamePlayers.values().stream().map(p -> p.points).toList());
            List<GamePlayer> winners = game.gamePlayers.values().stream().filter(player -> player.points == mostPoints).toList();
            String message;
            if (winners.size() == 1) {
                GamePlayer winner = winners.getFirst();
                message = winner.player.getName() + " won the game!";
            } else {
                String names = winners.stream()
                        .map(p -> p.player.getName())
                        .collect(Collectors.joining(", "));
                message = names + " tied first place!";
            }
            game.broadcastTitle(Component.text(message, NamedTextColor.GREEN), Component.text("With a total of " + mostPoints + " points!", NamedTextColor.YELLOW), 5);
            Component leaderboard = LeaderboardMessage.buildLeaderboard("Game Over - Result:", new ArrayList<>(game.gamePlayers.values().stream().map(player -> new LeaderboardMessage.Entry(player.player.getName(), player.points)).toList()));
            game.broadcast(leaderboard);
            for (GamePlayer player : game.gamePlayers.values()) {
                player.hotbarAction = new HotbarAction(Component.text("Leave Game", NamedTextColor.RED), () -> game.leaveGame(player.player), 4, null);
                setHotbar(player);
            }
        });
    }

    private void giveStartingCards(Random rand) {
        List<MapColor> cards = game.gameMap.getAllColors();

        for (GamePlayer player : game.gamePlayers.values()) {
            HashMap<MapColor, Integer> startingCards = new HashMap<>();
            for (int i = 0; i < 40; i++) { // TODO: Remove this testing line (change to 4)
                MapColor card = cards.get(rand.nextInt(cards.size()));
                startingCards.merge(card, 1, Integer::sum);
                player.cards.merge(card, 1, Integer::sum);
            }

            newCardMessage(player.player, startingCards, "You received 4 starting cards:");
        }
    }

    public Component newCardMessage(Map<MapColor, Integer> cards, String title) {
        return newCardMessage(null, cards, title);
    }

    public Component newCardMessage(Player player, Map<MapColor, Integer> cards, String title) {
        Component message = Component.text(title + "\n", NamedTextColor.YELLOW, TextDecoration.BOLD);
        for (Map.Entry<MapColor, Integer> entry : cards.entrySet()) {
            message = message.append(Component.text(entry.getValue() + "x ", NamedTextColor.GRAY).decoration(TextDecoration.BOLD, false).append(entry.getKey().colored).append(Component.text(" card\n", NamedTextColor.GRAY)));
        }
        if (player != null) {
            player.sendMessage(message);
        }
        return message;
    }

    public void setHotbar(GamePlayer player) {
        Player user = player.player;
        clearHotbar(user);
        ActionManager actionManager = new ActionManager();
        PlayerGuiManager.PlayerEntry entry = PlayerGuiManager.createGui(user.getInventory(), user, actionManager, true);

        actionManager.setAction(user.getInventory(), GuiTools.format(GuiTools.clearCompass(new ItemStack(Material.COMPASS)), player.hotbarAction.compassName), player.hotbarAction.compassSlot, GuiAction.ofClick(() -> player.hotbarAction.compassAction.run()));
        if (player.hotbarAction.entry != null) {
            player.hotbarAction.entry.accept(entry);
        }
    }

    public void setDefaultHotbar(GamePlayer player) {
        player.hotbarAction = new HotbarAction(GuiTools.getYellow("Show Info"), () -> startViewInfo(player.player), 8, null);
    }

    public void startViewInfo(Player player) {
        ViewInfo viewInfo = new ViewInfo(game, player, moveManager.currentMove != null && player.equals(moveManager.currentMove.player.player));
        player.openInventory(viewInfo.gui);
    }

    public void clearHotbar(Player player) {
        for (int i = 0; i < 9; i++) {
            player.getInventory().setItem(i, null);
        }
    }

    public void closeInventories() {
        for (Player player : game.gamePlayers.keySet()) {
            player.closeInventory();
        }
    }

    public static class HotbarAction {
        public Component compassName;
        public Runnable compassAction;
        public int compassSlot;
        public Consumer<PlayerGuiManager.PlayerEntry> entry;

        public HotbarAction(Component compassName, Runnable compassAction, int compassSlot, Consumer<PlayerGuiManager.PlayerEntry> entry) {
            this.compassName = compassName;
            this.compassAction = compassAction;
            this.compassSlot = compassSlot;
            this.entry = entry;
        }
    }

    public static abstract class PlayerState {
        public Game game;
        public GamePlayer player;
        public ActionManager actionManager;
        public boolean finished = false;

        public PlayerState(Game game, GamePlayer player, ActionManager actionManager, boolean createGui) {
            this.game = game;
            this.player = player;
            this.actionManager = actionManager;

            if (createGui) {
                PlayerGuiManager.createGui(player.player.getInventory(), player.player, actionManager, true);
            }
        }

        public abstract void timeOut();
    }
}
