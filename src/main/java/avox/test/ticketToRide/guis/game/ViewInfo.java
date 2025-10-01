package avox.test.ticketToRide.guis.game;

import avox.test.ticketToRide.game.core.DestinationCard;
import avox.test.ticketToRide.game.core.game.Game;
import avox.test.ticketToRide.game.core.MapColor;
import avox.test.ticketToRide.game.core.game.GamePlayer;
import avox.test.ticketToRide.guis.GuiAction;
import avox.test.ticketToRide.guis.game.move.PickCardGui;
import avox.test.ticketToRide.util.GuiTools;
import avox.test.ticketToRide.guis.InventoryGui;
import avox.test.ticketToRide.listener.PlayerGuiManager;
import avox.test.ticketToRide.guis.general.ScrollableRow;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ViewInfo extends InventoryGui {
    private final PlayerGuiManager.PlayerEntry oldState;

    public ViewInfo(Game game, Player user, PlayerGuiManager.PlayerEntry oldState, boolean chooseTurn) {
        super(user, chooseTurn ? 54 : 36, Component.text(chooseTurn ? "Pick an action" : "Game Info"), oldState);
        this.oldState = oldState;

        GamePlayer player = game.gamePlayers.get(user);
        int startingIndex = chooseTurn ? 18 : 0;
        if (chooseTurn) {
            initiateChooseTurn(game, player);
        }

        gui.setItem(startingIndex + 4, GuiTools.format(
                new ItemStack(player.markerData.material).add(player.trains - 1),
                GuiTools.getYellow("Trains left: ").append(GuiTools.colorize(player.trains + "/" + player.game.gameMap.startingTrains, NamedTextColor.GRAY)),
                game.gamePlayers.values().stream().filter(gamePlayer -> !gamePlayer.equals(player)).map(gamePlayer -> gamePlayer.markerData.colored.append(GuiTools.getGray(" - " + gamePlayer.trains + "/" + player.game.gameMap.startingTrains))).toList()
        ));

        int startCardBoard = startingIndex + 11;
        for (int i = 0; i < 5; i++) {
            MapColor card = game.cardBoard[i];
            gui.setItem(startCardBoard + i, GuiTools.format(new ItemStack(card.material), card.colored.decorate(TextDecoration.BOLD).append(GuiTools.getYellow(" Card")), List.of(GuiTools.getGray("The face up cards."))));
        }

        new ScrollableRow<>(actionManager, gui, startingIndex + 18, 9, new ArrayList<>(player.cards.keySet()), null) {
            @Override
            public ItemStack getSlotItem(MapColor color) {
                int cards = player.cards.get(color);
                ItemStack stack;
                if (cards == 0) {
                    stack = new ItemStack(Material.BARRIER);
                } else {
                    stack = new ItemStack(color.material).add(cards - 1);
                }
                return GuiTools.format(stack, GuiTools.getYellow("Card ").append(color.colored.decoration(TextDecoration.ITALIC, false).decorate(TextDecoration.BOLD)).append(GuiTools.getGray(" (" + cards + ")")));
            }
        };

        int startSlot = startingIndex + 27;
        ScrollableRow<DestinationCard> scrollableRow = new ScrollableRow<>(actionManager, gui, startSlot, 8, player.getDestinationCards(), GuiTools.getYellow("Empty Destination Card Slot")) {
            @Override
            public ItemStack getSlotItem(DestinationCard item) {
                return GuiTools.format(
                        new ItemStack(item.finished ? Material.LIME_CONCRETE : Material.RED_CONCRETE),
                        GuiTools.getYellow(item.pointA.name() + " - " + item.pointB.name() + " (" + item.reward + " points)")
                );
            }
        };

        actionManager.setAction(gui, GuiTools.format(GuiTools.clearCompass(new ItemStack(Material.COMPASS)), GuiTools.getYellow("Click to view cards on board")), startSlot + 8,
            GuiAction.ofClick(() -> {
                if (!scrollableRow.currentlyShown.isEmpty()) {
                    game.gameHandler.destinationHandler.viewDestinationCards(scrollableRow.currentlyShown, game, player.player);
                }
            })
        );
    }

    private void initiateChooseTurn(Game game, GamePlayer player) {
        actionManager.setAction(gui, GuiTools.format(new ItemStack(Material.GRASS_BLOCK), GuiTools.getYellow("Place Route")), 2, GuiAction.ofClick(() -> {

        }));

        actionManager.setAction(gui, GuiTools.format(new ItemStack(Material.WHITE_WOOL), GuiTools.getYellow("Take Up Cards"), List.of(GuiTools.colorize("Once selected you can't change!", NamedTextColor.RED))), 4, GuiAction.ofClick(() -> game.gameHandler.moveManager.pickCards(game, player, oldState)));

        actionManager.setAction(gui, GuiTools.format(new ItemStack(Material.NAME_TAG), GuiTools.getYellow("Take Up Routes")), 6, GuiAction.ofClick(() -> game.gameHandler.moveManager.pickRoutes()));
    }
}
