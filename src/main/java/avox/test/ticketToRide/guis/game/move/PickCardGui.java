package avox.test.ticketToRide.guis.game.move;

import avox.test.ticketToRide.game.Game;
import avox.test.ticketToRide.game.MapColor;
import avox.test.ticketToRide.game.player.GamePlayer;
import avox.test.ticketToRide.guis.GuiAction;
import avox.test.ticketToRide.guis.GuiTools;
import avox.test.ticketToRide.guis.InventoryGui;
import avox.test.ticketToRide.guis.PlayerGuiManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class PickCardGui extends InventoryGui {
    private final HashMap<MapColor, Integer> picked = new HashMap<>();
    private final MapColor[] board;
    private int capacity = 2;

    private Component finalMessage;

    public PickCardGui(Game game, GamePlayer player, PlayerGuiManager.PlayerEntry onClose) {
        super(player.player, 27, Component.text("Pick two cards"), onClose);
        board = game.cardBoard.clone();

        updateGui(game, player);
    }

    private void updateGui(Game game, GamePlayer player) {
        ArrayList<MapColor> cardTypes = game.gameMap.getAllColors();

        List<MapColor> pickedCards = picked.entrySet().stream().flatMap(e -> Collections.nCopies(e.getValue(), e.getKey()).stream()).toList();

        if (!pickedCards.isEmpty()) {
            MapColor card = pickedCards.getFirst();
            gui.setItem(3, GuiTools.format(new ItemStack(card.material), card.colored.append(GuiTools.getYellow(" Card"))));
        } else {
            gui.setItem(3, GuiTools.format(new ItemStack(Material.BARRIER), GuiTools.getYellow("Choose a card below!")));
        }

        if (pickedCards.size() == 2) {
            MapColor card = pickedCards.getFirst();
            gui.setItem(5, GuiTools.format(new ItemStack(card.material), card.colored.append(GuiTools.getYellow(" Card"))));
        } else {
            gui.setItem(5, GuiTools.format(new ItemStack(Material.BARRIER), GuiTools.getYellow("Choose a card below!")));
        }

        actionManager.addAction(gui, GuiTools.format(new ItemStack(Material.WHITE_DYE), GuiTools.getYellow("Pick random card")), 10, GuiAction.ofClick(() -> {
            System.out.println("Here!");
            Random rand = new Random();
            pick(game, player, cardTypes.get(rand.nextInt(0, cardTypes.size())), 1);
        }));

        for (int i = 0; i < 5; i++) {
            MapColor color = board[i];
            int finalI = i;
            actionManager.addAction(gui, getBoardItem(game, color), 12 + i, GuiAction.ofClick(() -> {
                if (pick(game, player, color, color.equals(game.gameMap.wildCard) ? 2 : 1)) {
                    Component message = game.newBoardCard(finalI, board, true);
                    if (finalMessage == null && message != null) {
                        finalMessage = message;
                    }
                }
            }));
        }
    }

    private ItemStack getBoardItem(Game game, MapColor color) {
        List<Component> lore = new ArrayList<>();
        if (game.gameMap.wildCard.equals(color)) {
            if (capacity >= 2) {
                lore.add(GuiTools.colorize("Picking this card costs two slots!", NamedTextColor.RED));
            } else {
                lore.add(GuiTools.colorize("This card costs two slots, you only have 1 left!", NamedTextColor.RED));
            }
        } else {
            lore.add(GuiTools.colorize("Click to select!", NamedTextColor.GRAY));
        }
        return GuiTools.format(new ItemStack(color.material), color.colored.append(GuiTools.getYellow(" Card")), lore);
    }

    private boolean pick(Game game, GamePlayer player, MapColor card, int space) {
        System.out.println("Here");
        if ((capacity - space) >= 0) {
            System.out.println("Here1");

            capacity =- space;
            picked.merge(card, 1, Integer::sum);
            if (capacity == 0) {
                System.out.println("Here2");
                game.gameHandler.playerStateManager.get(player.player).finished = true;
                game.cardBoard = board.clone();
                for (Map.Entry<MapColor, Integer> playerCard : picked.entrySet()) {
                    player.cards.merge(playerCard.getKey(), playerCard.getValue(), Integer::sum);
                }
                game.gameHandler.sendNewCardMessage(player.player, picked, "You got:");
                if (finalMessage != null) {
                    game.broadcast(finalMessage);
                }
                player.player.closeInventory();
            }
            updateGui(game, player);
            return true;
        } else {
            player.player.sendMessage("§cYou can't pick this card!");
        }
        return false;
    }
}
