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
import java.util.function.Consumer;

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
        for (GamePlayer player : game.gamePlayers.values()) {
            player.player.showTitle(Title.title(Component.text("Select Destination Cards"), Component.text("Select at least 2")));
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
