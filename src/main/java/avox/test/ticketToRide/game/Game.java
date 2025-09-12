package avox.test.ticketToRide.game;

import avox.test.ticketToRide.game.player.GamePlayer;
import avox.test.ticketToRide.utils.BillboardManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.Random;

import static avox.test.ticketToRide.TicketToRide.playerStateManager;

public class Game {
    public GameMap gameMap;
    public Arena arena;

    public boolean started = false;
    public Player gameOwner;
    public ArrayList<Player> members = new ArrayList<>();
    public ArrayList<GamePlayer> players = new ArrayList<>();
    public ArrayList<Player> invites = new ArrayList<>();


    private TextDisplay infoText;
    private int infoTextStep = 1; // 1: not enough players, 2: start info

    public Location topLeft; // The top left position of the playing board
    public int tilesX = 8;

    public Game(Player gameOwner, Arena arena, GameMap gameMap, Location topLeft) {
        this.gameOwner = gameOwner;
        this.arena = arena;
        this.topLeft = topLeft;
        this.gameMap = gameMap;
        addPlayer(gameOwner);

        infoText = new BillboardManager().spawnLine(arena.world, new Location(arena.world, 0, 100, -8.5), Component.text("Not enough players to start!", NamedTextColor.RED), 2, -1, false, true);
    }

    public void addPlayer(Player player) {
        playerStateManager.savePlayer(player);
        players.add(new GamePlayer(this, player));
        members.add(player);

        player.getInventory().clear();
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setExp(0);
        player.setGameMode(GameMode.ADVENTURE);

        GameManager.activePlayers.add(player);
        player.teleport(arena.spawnPosition);

        if (infoTextStep == 1 && members.size() >= 2) {
            infoTextStep = 2;
            infoText.text(Component.text("Start the game using /t2r start!", NamedTextColor.GREEN));
        }
    }

    public void leaveGame(Player player) {
        playerStateManager.restorePlayer(player);
        GamePlayer gamePlayer = players.stream().filter(p -> p.player == player).toList().getFirst();
        players.remove(gamePlayer);
        GameManager.activePlayers.remove(player);
        members.remove(player);
        if (gamePlayer.marker != null) {
            gamePlayer.marker.remove();
        }

        for (GamePlayer user : players) {
            user.player.sendMessage("§e" + player.getName() + " §cleft the game!");
            if (player == gameOwner) {
                user.player.sendMessage("§cThe host left the game!");
            }
        }
        player.sendMessage("§cYou left the game!");

        if (!started && members.size() <= 2 && infoTextStep != 1) {
            infoTextStep = 1;
            infoText.text(Component.text("Not enough players to start!", NamedTextColor.RED));
        }

        if (members.isEmpty() || (started && members.size() < 2)) {
            GameManager.deleteGame(this);
            return;
        }

        if (player == gameOwner) {
            GameManager.changeOwner(this, members.get(new Random().nextInt(0, members.size())));
            for (Player user : members) {
                user.sendMessage("§e" + gameOwner.getName() + "§7 is now the host of this game!");
            }
        }
    }
}
