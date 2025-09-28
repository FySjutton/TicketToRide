package avox.test.ticketToRide.guis.game;

import avox.test.ticketToRide.game.DestinationCard;
import avox.test.ticketToRide.game.Game;
import avox.test.ticketToRide.game.MapColor;
import avox.test.ticketToRide.game.player.GamePlayer;
import avox.test.ticketToRide.guis.GuiAction;
import avox.test.ticketToRide.guis.GuiTools;
import avox.test.ticketToRide.guis.InventoryGui;
import avox.test.ticketToRide.guis.PlayerGuiManager;
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
    public ViewInfo(Game game, Player user, PlayerGuiManager.PlayerEntry oldState) {
        super(user, 27, Component.text(user.getName() + " Game Info"), oldState);
        GamePlayer player = game.gamePlayers.get(user);

        gui.setItem(0, GuiTools.format(
                new ItemStack(player.markerData.material).add(player.trains - 1),
                GuiTools.getYellow("Trains left: ").append(GuiTools.colorize(player.trains + "/" + player.game.gameMap.startingTrains, NamedTextColor.GRAY)),
                game.gamePlayers.values().stream().filter(gamePlayer -> !gamePlayer.equals(player)).map(gamePlayer -> gamePlayer.markerData.colored.append(GuiTools.getGray(" - " + gamePlayer.trains))).toList()
        ));

        int startCardBoard = 4;
        for (int i = 0; i < 5; i++) {
            MapColor card = game.cardBoard[i];
            gui.setItem(startCardBoard + i, GuiTools.format(new ItemStack(card.material), card.colored.decorate(TextDecoration.BOLD).append(GuiTools.getYellow(" Card")), List.of(GuiTools.getGray("The face up cards."))));
        }

        new ScrollableRow<>(actionManager, gui, 9, 9, new ArrayList<>(player.cards.keySet()), null) {
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

        int startSlot = 18;
        ScrollableRow<DestinationCard> scrollableRow = new ScrollableRow<>(actionManager, gui, startSlot, 8, player.getDestinationCards(), GuiTools.getYellow("Empty Destination Card Slot")) {
            @Override
            public ItemStack getSlotItem(DestinationCard item) {
                return GuiTools.format(
                        new ItemStack(item.finished ? Material.LIME_CONCRETE : Material.RED_CONCRETE),
                        GuiTools.getYellow(item.pointA.name() + " - " + item.pointB.name() + " (" + item.reward + " points)")
                );
            }
        };

        actionManager.addAction(gui, GuiTools.format(GuiTools.clearCompass(new ItemStack(Material.COMPASS)), GuiTools.getYellow("Click to view cards on board")), startSlot + 8,
            GuiAction.ofClick(() -> {
                if (!scrollableRow.currentlyShown.isEmpty()) {
                    game.gameHandler.destinationHandler.viewDestinationCards(scrollableRow.currentlyShown, game, player.player);
                }
            })
        );
    }
}
