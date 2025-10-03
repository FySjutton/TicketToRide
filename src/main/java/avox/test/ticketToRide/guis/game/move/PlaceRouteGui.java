package avox.test.ticketToRide.guis.game.move;

import avox.test.ticketToRide.game.core.MapColor;
import avox.test.ticketToRide.game.core.Route;
import avox.test.ticketToRide.game.core.game.Game;
import avox.test.ticketToRide.game.core.game.GameMap;
import avox.test.ticketToRide.game.core.game.GamePlayer;
import avox.test.ticketToRide.game.gameHandler.MoveManager;
import avox.test.ticketToRide.guis.GuiAction;
import avox.test.ticketToRide.guis.InventoryGui;
import avox.test.ticketToRide.guis.general.CenteredRow;
import avox.test.ticketToRide.util.GuiTools;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class PlaceRouteGui extends InventoryGui {

    public PlaceRouteGui(GameMap map, GamePlayer player, ArrayList<MoveManager.RoutePlacement> options) {
        super(player.player, 27, Component.text("Pick which color to use"));

        new CenteredRow<>(actionManager, gui, 9, 9, options) {
            @Override
            public ItemStack getSlotItem(MoveManager.RoutePlacement object) {
                Component description = GuiTools.getYellow("Use ").append(object.color().colored).append(GuiTools.getYellow(" Cards"));
                List<Component> lore = new ArrayList<>();

                if (!object.color().equals(map.wildCard) && object.wildCards() > 0) {
                    description = description.append(GuiTools.getGray(" (" + object.wildCards() + " wildcards)"));
                    lore.add(GuiTools.colorize("This will spend " + object.wildCards() + " wildcards!", NamedTextColor.RED));
                }
                lore.add(GuiTools.getGray("You have " + object.cards() + " cards of this type."));
                return GuiTools.format(new ItemStack(object.color().material), description, lore);
            }

            @Override
            public GuiAction getGuiAction(MoveManager.RoutePlacement object) {
                return null;
            }
        };

        gui.setItem(0, GuiTools.format(new ItemStack(map.wildCard.material), GuiTools.getYellow("You have " + player.cards.get(map.wildCard) + " wildcards!")));
    }
}