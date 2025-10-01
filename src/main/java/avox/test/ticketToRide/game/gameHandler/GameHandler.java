package avox.test.ticketToRide.game.gameHandler;

import avox.test.ticketToRide.game.core.game.Game;
import avox.test.ticketToRide.game.core.MapColor;
import avox.test.ticketToRide.game.core.game.GamePlayer;
import avox.test.ticketToRide.guis.ActionManager;
import avox.test.ticketToRide.guis.GuiAction;
import avox.test.ticketToRide.util.GuiTools;
import avox.test.ticketToRide.listener.PlayerGuiManager;
import avox.test.ticketToRide.guis.game.ViewInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.BiConsumer;

public class GameHandler {
    public Game game;
    public HashMap<Player, PlayerState> playerStateManager = new HashMap<>();

    public final DestinationHandler destinationHandler = new DestinationHandler(this);
    public final MoveManager moveManager = new MoveManager(this);
    public final TimerManager timerManager = new TimerManager(this);

    public GameHandler(Game game) {
        this.game = game;
        Random rand = new Random();

        // Start by letting all players choose at least 2 cards
        for (Player player : game.gamePlayers.keySet()) {
            player.showTitle(Title.title(Component.text("Select Destination Cards"), Component.text("Select at least 2")));
            destinationHandler.chooseDestinationCards(game, player, 2);
        }
        timerManager.startTimedAction(120, () -> {
            game.broadcast("§aAll players have finished choosing their routes.");
            giveStartingCards(rand);

            // Initiate the move cycle
            moveManager.startMove(game, game.gamePlayers.values().stream().toList().get(rand.nextInt(0, game.gamePlayers.size())));
        });
    }

    private void giveStartingCards(Random rand) {
        List<MapColor> cards = game.gameMap.getAllColors();

        for (GamePlayer player : game.gamePlayers.values()) {
            HashMap<MapColor, Integer> startingCards = new HashMap<>();
            for (int i = 0; i < 4; i++) {
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

    public void setSelectActionHotbar(GamePlayer player) {
        setDefaultHotbar(player, true, GuiTools.getYellow("Select Action"));
    }

    public void setDefaultHotbar(Player player, boolean centered) {
        setDefaultHotbar(game.gamePlayers.get(player), centered);
    }

    public void setDefaultHotbar(GamePlayer player, boolean centered) {
        setDefaultHotbar(player, centered, GuiTools.getYellow("Show Info"));
    }

    private void setDefaultHotbar(GamePlayer player, boolean centered, Component name) {
        Player user = player.player;
        clearHotbar(user);
        ActionManager actionManager = new ActionManager();
        PlayerGuiManager.PlayerEntry oldEntry = PlayerGuiManager.createGui(user.getInventory(), user, actionManager, true, null);

        actionManager.setAction(user.getInventory(), GuiTools.format(GuiTools.clearCompass(new ItemStack(Material.COMPASS)), name),  centered ? 4 : 8, GuiAction.ofClick(() -> {
            if (player.overwriteAction != null) {
                player.overwriteAction.run();
            } else {
                openViewInfo(user, oldEntry);
            }
        }));
    }

    private void openViewInfo(Player player, PlayerGuiManager.PlayerEntry oldEntry) {
        ViewInfo viewInfo = new ViewInfo(game, player, oldEntry, moveManager.currentMove != null && player.equals(moveManager.currentMove.player.player));
        player.openInventory(viewInfo.gui);
    }

    public void clearHotbar(Player player) {
        for (int i = 0; i < 9; i++) {
            player.getInventory().setItem(i, null);
        }
    }

    public static abstract class PlayerState {
        public Game game;
        public Player player;
        public ActionManager actionManager;
        public boolean finished = false;

        public PlayerState(Game game, Player player, ActionManager actionManager, boolean createGui) {
            this.game = game;
            this.player = player;
            this.actionManager = actionManager;

            if (createGui) {
                PlayerGuiManager.createGui(player.getInventory(), player, actionManager, true, null);
            }
        }

        public abstract void timeOut();
    }
}
