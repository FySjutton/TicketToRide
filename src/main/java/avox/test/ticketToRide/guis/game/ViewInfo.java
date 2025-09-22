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
import org.bukkit.inventory.ItemStack;

public class ViewInfo extends InventoryGui {
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
            gui.setItem(9 + slot, GuiTools.format(stack, GuiTools.getYellow("Card ").append(color.colored.decoration(TextDecoration.ITALIC, false).decorate(TextDecoration.BOLD))));
            slot++;
        }

        slot = 0;
        for (int i : new int[]{27, 28, 29, 30, 31, 32, 33, 34, 36, 37, 38, 39, 40, 41, 42, 43}) {
            DestinationCard card = (slot < player.destinationCards.size()) ? player.destinationCards.get(slot) : null;
            if (card != null) {
                gui.setItem(i, GuiTools.format(
                        new ItemStack(card.finished ? Material.LIME_CONCRETE : Material.RED_CONCRETE),
                        GuiTools.getYellow(card.pointA.name() + " - " + card.pointB.name() + " (" + card.reward + " points)")
                ));
            } else {
                gui.setItem(i, GuiTools.format(new ItemStack(Material.BARRIER), GuiTools.getYellow("Empty Destination Card Slot")));
            }
            slot++;
        }

        addDestinationAction(35, 0, 8, game, player);
        addDestinationAction(44, 8, 16, game, player);
    }

    private void addDestinationAction(int slot, int from, int to, Game game, GamePlayer player) {
        actionManager.addAction(gui, GuiTools.format(GuiTools.clearCompass(new ItemStack(Material.COMPASS)), GuiTools.getYellow("Click to view cards on board")), slot,
                GuiAction.ofClick(() -> {
                    if (player.destinationCards.size() > from) {
                        game.gameHandler.destinationHandler.viewDestinationCards(
                                player.destinationCards.subList(from, Math.min(to, player.destinationCards.size())),
                                game, player.player
                        );
                    } else {
                        player.player.sendMessage("§cThere are no destination cards in this row!");
                    }
                }));
    }
}
