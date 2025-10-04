package avox.test.ticketToRide.game.gameHandler;

import avox.test.ticketToRide.game.core.DestinationCard;
import avox.test.ticketToRide.game.core.MapColor;
import avox.test.ticketToRide.game.core.Route;
import avox.test.ticketToRide.game.core.game.Game;
import avox.test.ticketToRide.game.core.game.GamePlayer;
import avox.test.ticketToRide.guis.ActionManager;
import avox.test.ticketToRide.guis.game.move.PlaceRouteGui;
import avox.test.ticketToRide.listener.PlayerGuiManager;
import avox.test.ticketToRide.guis.game.move.PickCardGui;
import avox.test.ticketToRide.util.GuiTools;
import avox.test.ticketToRide.util.board.MarkerManager;
import avox.test.ticketToRide.util.board.TrainManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class MoveManager {
    private final GameHandler gameHandler;
    public Move currentMove;
    private GamePlayer lastPlayer;

    public static class Move {
        public GamePlayer player;

        // Pick card variables
        public ArrayList<PickCardGui.CardSelection> pickedColorCards;
        public int capacity;

        // Pick destination card variables
        public List<DestinationCard> selectedDestinationCards;

        public Component actionMessage;
        public Runnable onFinish;

        // Place route card variables
        public boolean placeModeSelected = false;

        public Move(GamePlayer player) {
            this.player = player;

            pickedColorCards = new ArrayList<>();
            capacity = 2;
        }
    }

    public MoveManager(GameHandler gameHandler) {
        this.gameHandler = gameHandler;
    }

    public void startMove(Game game, GamePlayer player) {
        currentMove = new Move(player);
        boolean cancelGame = player.equals(lastPlayer);

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

            if (cancelGame) {
                gameHandler.gameFinished();
            } else {
                List<Player> players = new ArrayList<>(game.gamePlayers.keySet());
                int currentIndex = players.indexOf(player.player);
                int nextIndex = (currentIndex + 1) % players.size();
                GamePlayer next = game.gamePlayers.get(players.get(nextIndex));
                startMove(game, next);
            }
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

    public void pickRoutes(GamePlayer player) {
        currentMove.onFinish = () -> {
            currentMove.actionMessage = GuiTools.colorize(player.player.getName() + " picked up " + currentMove.selectedDestinationCards.size() + " cards!", NamedTextColor.GREEN);
            player.player.sendMessage(GuiTools.colorize("Turn finished! " + currentMove.selectedDestinationCards.size() + " cards selected!", NamedTextColor.GREEN));
        };

        player.player.sendMessage(GuiTools.colorize("You selected pick up destination cards! You must save at least one, but you can save more if you'd like!", NamedTextColor.GREEN));
        gameHandler.destinationHandler.chooseDestinationCards(player.game, player, 1);
    }

    public void placeRoute(GamePlayer player) {
        GameHandler.HotbarAction actionAction = player.hotbarAction;
        player.hotbarAction = new GameHandler.HotbarAction(GuiTools.getYellow("Return"), () -> {
            currentMove.placeModeSelected = false;
            player.hotbarAction = actionAction;
            gameHandler.setHotbar(player);
        }, 8, null);
        gameHandler.setHotbar(player);
        currentMove.placeModeSelected = true;
        player.player.closeInventory();
    }

    public void routeClicked(Route route, Player user) {
        if (!user.equals(currentMove.player.player) || !currentMove.placeModeSelected) return;
        GamePlayer player = currentMove.player;
        if (player.trains >= route.length) {
            MapColor wildCard = gameHandler.game.gameMap.wildCard;
            int wildCards = player.cards.get(wildCard);
            ArrayList<RoutePlacement> options = new ArrayList<>();

            if (!route.color.equals(wildCard)) {
                RoutePlacement colorOption = canAffordRoute(route, route.color, player.cards.get(route.color), wildCards);
                if (colorOption != null) {
                    options.add(colorOption);
                } else {
                    RoutePlacement wildcardOption = canAffordRoute(route, wildCard, wildCards, wildCards);
                    if (wildcardOption != null) options.add(wildcardOption);
                }
            } else {
                for (Map.Entry<MapColor, Integer> entry : player.cards.entrySet()) {
                    if (entry.getValue() <= 0) continue;
                    RoutePlacement placement = canAffordRoute(route, entry.getKey(), entry.getValue(), wildCards);
                    if (placement != null) options.add(placement);
                }
                if (options.stream().map(a -> a.color).toList().contains(wildCard) && options.size() > 1) {
                    options.removeIf(option -> option.color.equals(wildCard));
                }
            }

            if (!options.isEmpty()) {
                player.player.openInventory(new PlaceRouteGui(gameHandler.game.gameMap, player, options).gui);
            } else {
                player.player.sendMessage("§cYou can't afford that!");
            }
        } else {
            player.player.sendMessage("§cYou can't afford that! You don't have enough trains left!");
        }
    }

    private RoutePlacement canAffordRoute(Route route, MapColor color, int cards, int wildCards) {
        int total = route.tiles.size();
        int boats = (int) route.tiles.stream().filter(t -> t.type == Route.TileType.BOAT).count();
        int normal = total - boats;

        if (!color.equals(gameHandler.game.gameMap.wildCard) && cards <= 0) return null;

        if (color.equals(gameHandler.game.gameMap.wildCard)) {
            if (cards < total) return null;
            return new RoutePlacement(route, color, cards, total, 0);
        }

        if (wildCards < boats) return null;

        int neededWilds = boats;

        if (cards < normal) {
            int extraWilds = normal - cards;
            if (wildCards - boats < extraWilds) return null;
            neededWilds += extraWilds;
        }

        if (neededWilds == total) return null;

        return new RoutePlacement(route, color, cards, Math.min(cards, normal), neededWilds);
    }

    public void placeRouteOnBoard(Game game, GamePlayer player, RoutePlacement card) {
        player.player.closeInventory();
        gameHandler.clearHotbar(player.player);

        player.cards.put(card.color, player.cards.get(card.color) - card.cardsUsed);
        player.cards.put(game.gameMap.wildCard, player.cards.get(game.gameMap.wildCard) - card.wildCardsUsed);

        for (Route.Tile tile : card.route.tiles) {
            TrainManager.spawnSmallTrainCar(game.arena, game.gameMap, tile, player.markerData.material);
        }

        int points = game.gameMap.routePoints.get(card.route.length - 1);
        player.points += points;
        player.trains -= card.route.length;
        MarkerManager.reposition(game, player, player.points);

        String builtMessage;
        if (card.route.point_a.city() && card.route.point_b.city()) {
            builtMessage = "built a route from " + card.route.point_a.name() + " to " + card.route.point_b.name();
        } else if (card.route.point_a.city() || card.route.point_b.city()) {
            String cityName = card.route.point_a.city() ? card.route.point_a.name() : card.route.point_b.name();
            builtMessage = "built a route connecting to " + cityName;
        } else {
            builtMessage = "built a route";
        }

        currentMove.actionMessage = Component.text(
                player.player.getName() + " " + builtMessage +
                        " (" + card.route.color.name.toLowerCase() + ") for " + points + " points. " +
                        "The player now have " + player.trains + " trains left.",
                NamedTextColor.GREEN
        );

        player.player.sendMessage("§aRoute successfully placed! Awarded with " + points + " points! You now have " + player.trains + " trains left!");
        if (lastPlayer == null && player.trains < 3) {
            lastPlayer = player;
            gameHandler.timerManager.delay = 60;
            game.broadcast(Component.text(player.player.getName() + " now have less than 2 trains! Each player get one more go!", NamedTextColor.GOLD));
            game.broadcastTitle(GuiTools.colorize("Last Round!", NamedTextColor.GOLD), GuiTools.getGray(player.player.getName() + "only have " + player.trains + " trains left!"));
        }
        gameHandler.playerStateManager.get(player.player).finished = true;
    }

    public record RoutePlacement(Route route, MapColor color, int cards, int cardsUsed, int wildCardsUsed) {}

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
