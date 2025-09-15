package avox.test.ticketToRide.game;

import avox.test.ticketToRide.guis.ActionManager;
import avox.test.ticketToRide.guis.GuiAction;
import avox.test.ticketToRide.guis.PlayerGuiManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;

public class GameHandler {
    private Game game;
    private HashMap<Player, PlayerState> playerStateManager = new HashMap<>();

    public GameHandler(Game game) {
        this.game = game;

        for (Player player : game.gamePlayers.keySet()) {
            player.showTitle(Title.title(Component.text("Select Destination Cards"), Component.text("Select at least 2")));
            chooseDestinationCards(player);
        }
    }

    private void chooseDestinationCards(Player player) {
        ActionManager actionManager = new ActionManager();
        playerStateManager.put(player, new PlayerState(player, PlayerStateType.DESTINATION_TICKET, actionManager));

        PlayerInventory inventory = player.getInventory();
        setDestinationCard(0, actionManager, inventory, player);
        setDestinationCard(3, actionManager, inventory, player);
        setDestinationCard(6, actionManager, inventory, player);
    }

    private void setDestinationCard(int slot, ActionManager actionManager, Inventory inventory, Player player) {
        DestinationCard destinationCard = DestinationCard.getDestinationCard(game.gameMap);
        actionManager.setSlot(inventory, new ItemStack(Material.NAME_TAG), slot, GuiAction.ofHold(() -> {
            game.gamePlayers.get(player).setBeacon1(destinationCard.pointA.x(), destinationCard.pointA.y());
            game.gamePlayers.get(player).setBeacon2(destinationCard.pointB.x(), destinationCard.pointB.y());
        }));
    }

    public static class PlayerState {
        public Player player;
        public PlayerStateType playerState;
        public ActionManager actionManager;

        public PlayerState(Player player, PlayerStateType state, ActionManager actionManager) {
            this.player = player;
            this.playerState = state;

            PlayerGuiManager.createGui(player.getInventory(), player, actionManager, true);
        }
    }

    public enum PlayerStateType {
        DESTINATION_TICKET
    }
}
