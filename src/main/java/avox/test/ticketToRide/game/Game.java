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
import java.util.HashMap;
import java.util.Random;

import static avox.test.ticketToRide.TicketToRide.playerStateManager;

public class Game {
    public GameMap gameMap;
    public Arena arena;

    public boolean started = false;
    public GameHandler gameHandler;

    public Player gameOwner;
    public HashMap<Player, GamePlayer> gamePlayers = new HashMap<>();
    public ArrayList<Player> invites = new ArrayList<>();

    private TextDisplay infoText;
    private int infoTextStep = 1; // 1: not enough players, 2: start info

    public Game(Player gameOwner, Arena arena, GameMap gameMap) {
        this.gameOwner = gameOwner;
        this.arena = arena;
        this.gameMap = gameMap;
        addPlayer(gameOwner);

        infoText = new BillboardManager().spawnLine(arena.world, arena.mapStartPosition.clone().add((double) arena.tileX / 2, 2, (double) arena.tileY / 2), Component.text("Not enough players to start!", NamedTextColor.RED), 2, -1, false, true);
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
        GameManager.activePlayers.remove(player);
        if (gamePlayer.marker != null) {
            gamePlayer.marker.remove();
        }

        for (Player user : gamePlayers.keySet()) {
            user.sendMessage("§e" + player.getName() + " §cleft the game!");
            if (player == gameOwner) {
                user.sendMessage("§cThe host left the game!");
            }
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
            for (Player user : gamePlayers.keySet()) {
                user.sendMessage("§e" + gameOwner.getName() + "§7 is now the host of this game!");
            }
        }
    }
}
