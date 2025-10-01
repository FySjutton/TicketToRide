package avox.test.ticketToRide.guis.game.move;

import avox.test.ticketToRide.game.core.game.Game;
import avox.test.ticketToRide.game.core.MapColor;
import avox.test.ticketToRide.game.core.game.GamePlayer;
import avox.test.ticketToRide.game.gameHandler.MoveManager;
import avox.test.ticketToRide.guis.GuiAction;
import avox.test.ticketToRide.util.GuiTools;
import avox.test.ticketToRide.guis.InventoryGui;
import avox.test.ticketToRide.listener.PlayerGuiManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class PickCardGui extends InventoryGui {
    private final MoveManager.Move currentMove;

    public PickCardGui(Game game, GamePlayer player, PlayerGuiManager.PlayerEntry onClose) {
        super(player.player, 27, Component.text("Pick two cards"), onClose);
        currentMove = game.gameHandler.moveManager.currentMove;

        updateGui(game, player);
    }

    private void updateGui(Game game, GamePlayer player) {
        ArrayList<MapColor> cardTypes = game.gameMap.getAllColors();

        if (!currentMove.picked.isEmpty()) {
            MapColor card = currentMove.picked.getFirst().card();
            gui.setItem(3, GuiTools.format(new ItemStack(card.material), card.colored.append(GuiTools.getYellow(" Card"))));
        } else {
            gui.setItem(3, GuiTools.format(new ItemStack(Material.BARRIER), GuiTools.getYellow("Choose a card below!")));
        }

        // As the gui is closed instantly after selecting the second card, nothing except a barrier is needed
        gui.setItem(5, GuiTools.format(new ItemStack(Material.BARRIER), GuiTools.getYellow("Choose a card below!")));

        actionManager.setAction(gui, GuiTools.format(new ItemStack(Material.WHITE_DYE), GuiTools.getYellow("Pick random card")), 10, GuiAction.ofClick(() -> {
            Random rand = new Random();
            pick(game, currentMove, player, new CardSelection(cardTypes.get(rand.nextInt(0, cardTypes.size())), false), 1);
            updateGui(game, player);
        }));

        for (int i = 0; i < 5; i++) {
            MapColor color = game.cardBoard[i];
            int finalI = i;
            actionManager.setAction(gui, getBoardItem(game, color), 12 + i, GuiAction.ofClick(() -> {
                if (pick(game, currentMove, player, new CardSelection(color, true), color.equals(game.gameMap.wildCard) ? 2 : 1)) {
                    Component message = game.newBoardCard(finalI, true);
                    if (currentMove.finalMessage == null && message != null) {
                        currentMove.finalMessage = message;
                    }
                    updateGui(game, player);
                }
            }));
        }
    }

    private ItemStack getBoardItem(Game game, MapColor color) {
        List<Component> lore = new ArrayList<>();
        if (game.gameMap.wildCard.equals(color)) {
            if (currentMove.capacity >= 2) {
                lore.add(GuiTools.colorize("Picking this card costs two slots!", NamedTextColor.RED));
            } else {
                lore.add(GuiTools.colorize("This card costs two slots, you only have 1 left!", NamedTextColor.RED));
            }
        } else {
            lore.add(GuiTools.colorize("Click to select!", NamedTextColor.GRAY));
        }
        return GuiTools.format(new ItemStack(color.material), color.colored.append(GuiTools.getYellow(" Card")), lore);
    }

    public static boolean pick(Game game, MoveManager.Move currentMove, GamePlayer player, CardSelection card, int space) {
        if ((currentMove.capacity - space) >= 0) {
            currentMove.capacity -= space;
            currentMove.picked.add(card);
            if (currentMove.capacity == 0) {
                game.gameHandler.playerStateManager.get(player.player).finished = true;
                for (CardSelection playerCard : currentMove.picked) {
                    player.cards.merge(playerCard.card, 1, Integer::sum);
                }
                game.gameHandler.newCardMessage(player.player, currentMove.picked.stream().collect(Collectors.toMap(s -> s.card, s -> 1, Integer::sum)), "You got:");
                if (currentMove.finalMessage != null) {
                    game.broadcast(currentMove.finalMessage);
                }
                player.player.closeInventory();
            }
            return true;
        } else {
            player.player.sendMessage("§cYou can't pick this card!");
        }
        return false;
    }

    public record CardSelection(MapColor card, boolean fromUpface) {}
}
