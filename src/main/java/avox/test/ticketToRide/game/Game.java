package avox.test.ticketToRide.game;

import avox.test.ticketToRide.game.gameHandler.GameHandler;
import avox.test.ticketToRide.game.player.GamePlayer;
import avox.test.ticketToRide.guis.GuiTools;
import avox.test.ticketToRide.guis.PlayerGuiManager;
import avox.test.ticketToRide.utils.BillboardManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;

import java.util.*;

import static avox.test.ticketToRide.TicketToRide.playerStateManager;

public class Game {
    public GameMap gameMap;
    public Arena arena;

    public boolean started = false;
    public GameHandler gameHandler;

    public Player gameOwner;
    public LinkedHashMap<Player, GamePlayer> gamePlayers = new LinkedHashMap<>();
    public ArrayList<Player> invites = new ArrayList<>();

    public TextDisplay infoText;
    private int infoTextStep = 1; // 1: not enough players, 2: start info

    public MapColor[] cardBoard = new MapColor[5];

    public Game(Player gameOwner, Arena arena, GameMap gameMap) {
        this.gameOwner = gameOwner;
        this.arena = arena;
        this.gameMap = gameMap;
        addPlayer(gameOwner);

        infoText = new BillboardManager().spawnLine(arena.world, arena.mapStartPosition.clone().add((double) arena.tileX / 2, 2, (double) arena.tileY / 2), Component.text("Not enough players to start!", NamedTextColor.RED), 2, -1, false, true);

        replaceFullBoard(true);
    }

    public void addPlayer(Player player) {
        playerStateManager.savePlayer(player);
        gamePlayers.put(player, new GamePlayer(this, player));

        player.getInventory().clear();
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setExp(0);
        player.setGameMode(GameMode.ADVENTURE);

        GameManager.activePlayers.add(player);
        player.teleport(arena.spawnPosition);

        if (infoTextStep == 1 && gamePlayers.size() >= 2) {
            infoTextStep = 2;
            infoText.text(Component.text("Start the game using /t2r start!", NamedTextColor.GREEN));
        }
    }

    public void leaveGame(Player player) {
        playerStateManager.restorePlayer(player);
        GamePlayer gamePlayer = gamePlayers.get(player);
        gamePlayers.remove(player);
        PlayerGuiManager.removeGui(player);
        GameManager.activePlayers.remove(player);
        if (gamePlayer.marker != null) {
            gamePlayer.marker.remove();
        }

        broadcast("§e" + player.getName() + " §cleft the game!");
        if (player == gameOwner) {
            broadcast("§cThe host left the game!");
        }
        player.sendMessage("§cYou left the game!");

        if (!started && gamePlayers.size() <= 2 && infoTextStep != 1) {
            infoTextStep = 1;
            infoText.text(Component.text("Not enough players to start!", NamedTextColor.RED));
        }

        if (gamePlayers.isEmpty() || (started && gamePlayers.size() < 2)) {
            GameManager.deleteGame(this);
            return;
        }

        if (player == gameOwner) {
            GameManager.changeOwner(this, gamePlayers.keySet().stream().toList().get(new Random().nextInt(0, gamePlayers.size())));
            broadcast("§e" + gameOwner.getName() + "§7 is now the host of this game!");
        }
    }

    public void broadcast(String message) {
        for (Player player : gamePlayers.keySet()) {
            player.sendMessage(message);
        }
    }

    public void broadcast(Component message) {
        for (Player player : gamePlayers.keySet()) {
            player.sendMessage(message);
        }
    }

    public void broadcastTitle(Component title, Component subTitle, Player excludePlayer) {
        for (Player player : gamePlayers.keySet()) {
            if (!player.equals(excludePlayer)) {
                player.showTitle(Title.title(title, subTitle));
            }
        }
    }

    public void newBoardCard(int i, boolean silent) {
        ArrayList<MapColor> colors = gameMap.getAllColors();
        Random rand = new Random();

        cardBoard[i] = colors.get(rand.nextInt(colors.size()));
        if (ensureValidBoard(colors, rand) && !silent) {
            broadcast(GuiTools.getGray("Card Board contained 3 or more train cards! Fully replaced!"));
        }
    }

    public void replaceFullBoard(boolean silent) {
        ArrayList<MapColor> colors = gameMap.getAllColors();
        Random rand = new Random();

        for (int j = 0; j < cardBoard.length; j++) {
            cardBoard[j] = colors.get(rand.nextInt(colors.size()));
        }

        if (ensureValidBoard(colors, rand) && !silent) {
            broadcast(GuiTools.getGray("Card Board contained 3 or more train cards! Fully replaced!"));
        }
    }

    private boolean ensureValidBoard(ArrayList<MapColor> colors, Random rand) {
        boolean replaced = false;
        while (countWildcards() >= 3) {
            for (int j = 0; j < cardBoard.length; j++) {
                cardBoard[j] = colors.get(rand.nextInt(colors.size()));
            }
            replaced = true;
        }
        return replaced;
    }

    private int countWildcards() {
        return (int) Arrays.stream(cardBoard)
                .filter(card -> card.equals(gameMap.wildCard))
                .count();
    }

}
