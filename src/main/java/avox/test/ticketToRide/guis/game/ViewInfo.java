package avox.test.ticketToRide.guis.game;

import avox.test.ticketToRide.game.DestinationCard;
import avox.test.ticketToRide.game.Game;
import avox.test.ticketToRide.game.MapColor;
import avox.test.ticketToRide.game.player.GamePlayer;
import avox.test.ticketToRide.guis.GuiAction;
import avox.test.ticketToRide.guis.GuiTools;
import avox.test.ticketToRide.guis.InventoryGui;
import avox.test.ticketToRide.guis.PlayerGuiManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class ViewInfo extends InventoryGui {
    private int scroll = 0;

    public ViewInfo(Game game, Player user, PlayerGuiManager.PlayerEntry oldState) {
        super(user, 54, Component.text(user.getName() + " Game Info"), oldState);

        GamePlayer player = game.gamePlayers.get(user);

        gui.setItem(4, GuiTools.format(
                new ItemStack(player.markerData.material).add(player.trains - 1),
                GuiTools.getYellow("Trains left: ").append(GuiTools.colorize(player.trains + "/" + player.game.gameMap.startingTrains, NamedTextColor.GRAY)),
                game.gamePlayers.values().stream().filter(gamePlayer -> !gamePlayer.equals(player)).map(gamePlayer -> gamePlayer.markerData.colored.append(GuiTools.getGray(" - " + gamePlayer.trains))).toList()
        ));

        int slot = 0;
        for (MapColor color : player.cards.keySet()) {
            int cards = player.cards.get(color);
            ItemStack stack;
            if (cards == 0) {
                stack = new ItemStack(Material.BARRIER);
            } else {
                stack = new ItemStack(color.material).add(cards - 1);
            }
            gui.setItem(9 + slot, GuiTools.format(stack, GuiTools.getYellow("Card ").append(color.colored.decoration(TextDecoration.ITALIC, false).decorate(TextDecoration.BOLD)).append(GuiTools.getGray(" (" + cards + ")"))));
            slot++;
        }

        updateDestinationRow(game, gui, player, 27);
    }

    private void updateDestinationRow(Game game, Inventory gui, GamePlayer player, int slot) {
        ArrayList<DestinationCard> cards = player.getDestinationCards();
        int totalCards = cards.size();

        for (int i = 0; i <= 8; i++) {
            actionManager.removeAction(slot + i);
            gui.setItem(slot + i, null);
        }

        boolean hasBack = scroll > 0;
        boolean hasForward = scroll + (8 - (hasBack ? 1 : 0)) < totalCards;

        int cardSlots = 8 - (hasBack ? 1 : 0) - (hasForward ? 1 : 0);
        int itemSlot = slot + (hasBack ? 1 : 0);

        if (hasBack) {
            actionManager.addAction(gui, GuiTools.format(new ItemStack(Material.ARROW), GuiTools.getYellow("Scroll back")), slot,
                    GuiAction.ofClick(() -> {
                        if (scroll == 2) scroll = 0;
                        else scroll--;
                        updateDestinationRow(game, gui, player, slot);
                    })
            );
        }

        if (hasForward) {
            actionManager.addAction(gui, GuiTools.format(new ItemStack(Material.ARROW), GuiTools.getYellow("Scroll forward")), slot + 7,
                    GuiAction.ofClick(() -> {
                        if (scroll == 0 && totalCards > 7) {
                            scroll = 2;
                        } else {
                            scroll++;
                        }
                        updateDestinationRow(game, gui, player, slot);
                    })
            );
        }

        for (int i = 0; i < cardSlots; i++) {
            actionManager.removeAction(itemSlot);
            int cardIndex = scroll + i;
            if (cardIndex < totalCards) {
                DestinationCard card = cards.get(cardIndex);
                gui.setItem(itemSlot, GuiTools.format(
                        new ItemStack(card.finished ? Material.LIME_CONCRETE : Material.RED_CONCRETE),
                        GuiTools.getYellow(card.pointA.name() + " - " + card.pointB.name() + " (" + card.reward + " points) " + (cardIndex + 1))
                ));
            } else {
                gui.setItem(itemSlot, GuiTools.format(new ItemStack(Material.BARRIER), GuiTools.getYellow("Empty Destination Card Slot")));
            }
            itemSlot++;
        }

        actionManager.addAction(gui, GuiTools.format(GuiTools.clearCompass(new ItemStack(Material.COMPASS)), GuiTools.getYellow("Click to view cards on board")), slot + 8,
                GuiAction.ofClick(() -> {
                    ArrayList<DestinationCard> visibleCards = new ArrayList<>();
                    for (int i = 0; i < cardSlots; i++) {
                        int idx = scroll + i;
                        if (idx < totalCards) visibleCards.add(cards.get(idx));
                    }
                    if (!visibleCards.isEmpty()) {
                        game.gameHandler.destinationHandler.viewDestinationCards(visibleCards, game, player.player);
                    }
                })
        );
    }
}
