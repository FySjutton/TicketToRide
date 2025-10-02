package avox.test.ticketToRide.game.gameHandler;

import avox.test.ticketToRide.game.core.DestinationCard;
import avox.test.ticketToRide.game.core.game.Game;
import avox.test.ticketToRide.game.core.game.GamePlayer;
import avox.test.ticketToRide.guis.ActionManager;
import avox.test.ticketToRide.guis.GuiAction;
import avox.test.ticketToRide.util.GuiTools;
import avox.test.ticketToRide.listener.PlayerGuiManager;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DestinationHandler {
    private final GameHandler gameHandler;

    public DestinationHandler(GameHandler gameHandler) {
        this.gameHandler = gameHandler;
    }

    public void viewDestinationCards(List<DestinationCard> cards, Game game, GamePlayer gamePlayer) {
        Player player = gamePlayer.player;
        player.closeInventory();

        gamePlayer.hotbarAction = new GameHandler.HotbarAction(GuiTools.getYellow("Return"), () -> {
            gameHandler.setDefaultHotbar(gamePlayer);
            gameHandler.setHotbar(gamePlayer);
        }, 8, (entry) -> {
            for (int i = 0; i < cards.size(); i++) {
                DestinationCard card = cards.get(i);
                int finalI = i;
                entry.actionManager.setAction(entry.inventory.inventory(), GuiTools.format(
                        new ItemStack(Material.NAME_TAG),
                        GuiTools.getYellow(card.pointA.name() + " - " + card.pointB.name() + " (" + card.reward + " points)")
                ), i, GuiAction.ofHold(() -> beaconHolder(player, finalI, card)));
            }

            for (int i = cards.size(); i < 9; i++) {
                entry.actionManager.addAction(i, GuiAction.ofHold(() -> game.gamePlayers.get(player).clearBeacons()));
            }
        });
        gameHandler.setHotbar(gamePlayer);
    }

    public void chooseDestinationCards(Game game, GamePlayer player, int minimumAccepts) {
        chooseDestinationCards(game, player, minimumAccepts, 3);
    }

    public void chooseDestinationCards(Game game, GamePlayer gamePlayer, int minimumAccepts, int maximumAccepts) {
        Player player = gamePlayer.player;
        player.closeInventory();
        ActionManager actionManager = new ActionManager();
        DestinationSelectionAction state = new DestinationSelectionAction(game, gamePlayer, actionManager);
        gameHandler.playerStateManager.put(player, state);

        PlayerInventory inventory = player.getInventory();
        inventory.setHeldItemSlot(0);

        setDestinationCard(game, state, inventory, 1, minimumAccepts, maximumAccepts);
        setDestinationCard(game, state, inventory, 3, minimumAccepts, maximumAccepts);
        setDestinationCard(game, state, inventory, 5, minimumAccepts, maximumAccepts);

        for (int slot : List.of(0, 7, 8)) {
            actionManager.setAction(slot, GuiAction.ofHold(() -> game.gamePlayers.get(player).clearBeacons()));
        }

        actionManager.setAction(
                inventory,
                GuiTools.format(
                    new ItemStack(Material.RED_CONCRETE),
                    GuiTools.colorize("Finished!", NamedTextColor.RED)
                ),
                8,
                GuiAction.ofClick(() -> {
                    ItemStack stack = inventory.getItem(8);
                    if (stack == null) return;
                    if (stack.getType().equals(Material.RED_CONCRETE)) {
                        player.sendMessage(state.error);
                    } else {
                        List<DestinationCard> acceptedCards = state.toggles.keySet().stream().filter(state.toggles::get).toList();
                        finished(game, gamePlayer, state, acceptedCards);
                    }
                })
        );
    }

    private void setDestinationCard(Game game, DestinationSelectionAction state, Inventory inventory, int slot, int minimumAccepts, int maximumAccepts) {
        DestinationCard card = DestinationCard.getDestinationCard(game.gameMap);
        int toggleSlot = slot + 1;

        state.cardSlots.put(slot, card);
        state.cardSlots.put(toggleSlot, card);

        ItemStack mainItem = GuiTools.format(
                new ItemStack(Material.NAME_TAG),
                GuiTools.getYellow(card.pointA.name() + " - " + card.pointB.name() + " (" + card.reward + " points)")
        );
        state.actionManager.addAction(inventory, mainItem, slot, GuiAction.ofHold(() -> beaconHolder(state.player.player, toggleSlot, card)));
        state.actionManager.addAction(slot, GuiAction.ofClick(() -> toggleCard(state, inventory, toggleSlot, card, minimumAccepts, maximumAccepts)));

        ItemStack toggleItem = GuiTools.format(
                new ItemStack(Material.YELLOW_CONCRETE),
                GuiTools.getYellow("Click to toggle")
        );
        state.actionManager.addAction(inventory, toggleItem, toggleSlot, GuiAction.ofClick(() -> toggleCard(state, inventory, toggleSlot, card, minimumAccepts, maximumAccepts)));
        state.actionManager.addAction(toggleSlot, GuiAction.ofHold(() -> beaconHolder(state.player.player, toggleSlot, card)));
    }

    private void toggleCard(DestinationSelectionAction state, Inventory inventory, int slot, DestinationCard card, int minimumAccepts, int maximumAccepts) {
        if (card == null) return;

        ItemStack stack = inventory.getItem(slot);
        if (stack == null) return;

        boolean accepted = state.toggles.getOrDefault(card, false);

        if (accepted) {
            inventory.setItem(slot, GuiTools.format(
                    stack.withType(Material.RED_CONCRETE),
                    GuiTools.colorize("Don't Accept", NamedTextColor.RED)
            ));
            state.toggles.put(card, false);
        } else {
            inventory.setItem(slot, GuiTools.format(
                    stack.withType(Material.LIME_CONCRETE),
                    GuiTools.colorize("Accept", NamedTextColor.GREEN)
            ));
            state.toggles.put(card, true);
        }

        ItemStack finishBtn = inventory.getItem(8);
        if (finishBtn == null) return;
        boolean success = false;
        if (state.toggles.size() == 3) {
            int amountAccepted = state.toggles.values().stream().filter(i -> i).toList().size();
            if (amountAccepted < minimumAccepts) {
                state.error = "§cYou must accept at least " + minimumAccepts + "!";
            } else if (amountAccepted > maximumAccepts) {
                state.error = "§cYou must accept no more than " + maximumAccepts + "!";
            } else {
                state.error = "";
                success = true;
            }
        } else {
            state.error = "§cYou must toggle all!";
        }
        inventory.setItem(8, GuiTools.format(finishBtn.withType(success ? Material.LIME_CONCRETE : Material.RED_CONCRETE), GuiTools.colorize("Finished!", success ? NamedTextColor.GREEN : NamedTextColor.RED)));
    }

    private static void finished(Game game, GamePlayer player, DestinationSelectionAction state, List<DestinationCard> acceptedCards) {
        for (int i = 0; i < 9; i++) {
            player.player.getInventory().setItem(i, null);
        }
        game.gamePlayers.get(player.player).getDestinationCards().addAll(acceptedCards);

        MoveManager moveManager = game.gameHandler.moveManager;
        if (moveManager.currentMove != null && moveManager.currentMove.player.equals(player)) {
            moveManager.currentMove.selectedDestinationCards = acceptedCards;
        }
        state.finished = true;
        game.gameHandler.setDefaultHotbar(player);
        game.gameHandler.setHotbar(player);
    }

    private void beaconHolder(Player player, int slot, DestinationCard card) {
        GamePlayer gamePlayer = gameHandler.game.gamePlayers.get(player);
        if (gamePlayer.beaconSlot != slot) {
            gamePlayer.setBeacons(
                    slot,
                    card.pointA.x() + card.pointA.width() / 2,
                    card.pointA.y() + card.pointA.height() / 2,
                    card.pointB.x() + card.pointB.width() / 2,
                    card.pointB.y() + card.pointB.height() / 2
            );
        }
    }

    public static class DestinationSelectionAction extends GameHandler.PlayerState {
        public final Map<DestinationCard, Boolean> toggles = new HashMap<>();
        public final Map<Integer, DestinationCard> cardSlots = new HashMap<>();
        public String error = "§cYou must toggle all!";

        public DestinationSelectionAction(Game game, GamePlayer player, ActionManager actionManager) {
            super(game, player, actionManager, true);
        }

        @Override
        public void timeOut() {
            finished(game, player, this, cardSlots.values().stream().distinct().toList());
        }
    }
}
