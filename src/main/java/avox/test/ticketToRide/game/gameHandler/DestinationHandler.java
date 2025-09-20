package avox.test.ticketToRide.game.gameHandler;

import avox.test.ticketToRide.game.DestinationCard;
import avox.test.ticketToRide.game.Game;
import avox.test.ticketToRide.game.player.GamePlayer;
import avox.test.ticketToRide.guis.ActionManager;
import avox.test.ticketToRide.guis.GuiAction;
import avox.test.ticketToRide.guis.GuiTools;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class DestinationHandler {
    private final GameHandler gameHandler;

    public DestinationHandler(GameHandler gameHandler) {
        this.gameHandler = gameHandler;
    }

    public void chooseDestinationCards(Game game, Player player, int minimumAccepts, Consumer<List<DestinationCard>> finishedAction) {
        ActionManager actionManager = new ActionManager();
        DestinationSelectionAction state = new DestinationSelectionAction(player, actionManager);
        gameHandler.playerStateManager.put(player, state);

        PlayerInventory inventory = player.getInventory();
        inventory.setHeldItemSlot(0);

        setDestinationCard(game, state, player, inventory, 1, minimumAccepts);
        setDestinationCard(game, state, player, inventory, 3, minimumAccepts);
        setDestinationCard(game, state, player, inventory, 5, minimumAccepts);

        for (int slot : List.of(0, 7, 8)) {
            actionManager.addAction(slot, GuiAction.ofHold(() -> game.gamePlayers.get(player).clearBeacons()));
        }

        actionManager.addAction(
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
                        finishedAction.accept(state.toggles.keySet().stream().filter(state.toggles::get).toList());
                    }
                })
        );
    }

    private void setDestinationCard(Game game, DestinationSelectionAction state, Player player, Inventory inventory, int slot, int minimumAccepts) {
        DestinationCard card = DestinationCard.getDestinationCard(game.gameMap);
        int toggleSlot = slot + 1;

        state.cardSlots.put(slot, card);
        state.cardSlots.put(toggleSlot, card);
//        state.toggles.put(card, false);

        ItemStack mainItem = GuiTools.format(
                new ItemStack(Material.NAME_TAG),
                GuiTools.getYellow(card.pointA.name() + " - " + card.pointB.name() + " (" + card.reward + " points)")
        );
        state.actionManager.addAction(inventory, mainItem, slot, GuiAction.ofHold(() -> beaconHolder(state.player, toggleSlot, card)));
        state.actionManager.addAction(slot, GuiAction.ofClick(() -> toggleCard(state, inventory, player, toggleSlot, card, minimumAccepts)));

        ItemStack toggleItem = GuiTools.format(
                new ItemStack(Material.YELLOW_CONCRETE),
                GuiTools.getYellow("Click to toggle")
        );
        state.actionManager.addAction(inventory, toggleItem, toggleSlot, GuiAction.ofClick(() -> toggleCard(state, inventory, player, toggleSlot, card, minimumAccepts)));
        state.actionManager.addAction(toggleSlot, GuiAction.ofHold(() -> beaconHolder(state.player, slot, card)));
    }

    private void toggleCard(DestinationSelectionAction state, Inventory inventory, Player player, int slot, DestinationCard card, int minimumAccepts) {
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
        if (state.toggles.size() == 3) {
            int amountAccepted = state.toggles.values().stream().filter(i -> i).toList().size();
            if (amountAccepted < minimumAccepts) {
                state.error = "§cYou must accept at least " + minimumAccepts + "!";
                inventory.setItem(8, GuiTools.format(finishBtn.withType(Material.RED_CONCRETE), GuiTools.colorize("Finished!", NamedTextColor.RED)));
            } else {
                state.error = "";
                inventory.setItem(8, GuiTools.format(finishBtn.withType(Material.LIME_CONCRETE), GuiTools.colorize("Finished!", NamedTextColor.GREEN)));
            }
        } else {
            state.error = "§cYou must toggle all!";
            inventory.setItem(8, GuiTools.format(finishBtn.withType(Material.RED_CONCRETE), GuiTools.colorize("Finished!", NamedTextColor.RED)));
        }
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
        public final ActionManager actionManager;
        public String error = "§cYou must toggle all!";

        public DestinationSelectionAction(Player player, ActionManager actionManager) {
            super(player, actionManager);
            this.actionManager = actionManager;
        }
    }
}
