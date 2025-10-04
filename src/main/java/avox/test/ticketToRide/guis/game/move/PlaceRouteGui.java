package avox.test.ticketToRide.guis.game.move;

import avox.test.ticketToRide.game.core.game.GameMap;
import avox.test.ticketToRide.game.core.game.GamePlayer;
import avox.test.ticketToRide.game.gameHandler.MoveManager;
import avox.test.ticketToRide.guis.GuiAction;
import avox.test.ticketToRide.guis.InventoryGui;
import avox.test.ticketToRide.guis.general.ScrollableRow;
import avox.test.ticketToRide.util.GuiTools;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class PlaceRouteGui extends InventoryGui {
    public PlaceRouteGui(GameMap map, GamePlayer player, ArrayList<MoveManager.RoutePlacement> options) {
        super(player.player, 27, Component.text("Pick which color to use"));

        new ScrollableRow<>(actionManager, gui, 9, 9, options, null, true) {
            @Override
            public ItemStack getSlotItem(MoveManager.RoutePlacement object) {
                Component description = GuiTools.getYellow("Use ").append(object.color().colored).append(GuiTools.getYellow(" Cards"));
                List<Component> lore = new ArrayList<>();

                if (!object.color().equals(map.wildCard) && object.wildCardsUsed() > 0) {
                    description = description.append(GuiTools.getGray(" (" + object.wildCardsUsed() + " wild" + GuiTools.getCardsText(object.wildCardsUsed()) + ")"));
                }
                lore.add(GuiTools.colorize("This will spend " + object.cardsUsed() + " " + object.color().name.toLowerCase() + " " + GuiTools.getCardsText(object.cardsUsed()) + (object.wildCardsUsed() > 0 ? (" and " + object.wildCardsUsed() + " wild" + GuiTools.getCardsText(object.wildCardsUsed())) : "") + "!", NamedTextColor.GRAY));
                lore.add(GuiTools.getGray("You have " + object.cards() + " " + object.color().name.toLowerCase() + " " + GuiTools.getCardsText(object.cards()) + "!"));
                return GuiTools.format(new ItemStack(object.color().material), description, lore);
            }

            @Override
            public GuiAction getGuiAction(MoveManager.RoutePlacement object) {
                return GuiAction.ofClick(() -> player.game.gameHandler.moveManager.placeRouteOnBoard(player.game, player, object));
            }
        };

        gui.setItem(0, GuiTools.format(new ItemStack(map.wildCard.material), GuiTools.getYellow("You have " + player.cards.get(map.wildCard) + " wildcards!")));
    }
}