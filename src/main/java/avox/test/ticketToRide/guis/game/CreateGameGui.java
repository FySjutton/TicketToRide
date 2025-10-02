package avox.test.ticketToRide.guis.game;

import avox.test.ticketToRide.TicketToRide;
import avox.test.ticketToRide.game.core.arena.BaseArena;
import avox.test.ticketToRide.game.GameManager;
import avox.test.ticketToRide.game.core.game.GameMap;
import avox.test.ticketToRide.guis.GuiAction;
import avox.test.ticketToRide.util.GuiTools;
import avox.test.ticketToRide.guis.InventoryGui;
import avox.test.ticketToRide.listener.PlayerGuiManager;
import avox.test.ticketToRide.guis.general.SelectObjectGui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static avox.test.ticketToRide.commands.MainCommand.invitePlayer;
import static avox.test.ticketToRide.util.GuiTools.*;

public class CreateGameGui extends InventoryGui {
    private final ArrayList<Player> opponents = new ArrayList<>();
    private ItemStack createGame;
    private String error = "You must invite some players first!";
    
    public CreateGameGui(Player player, Component name, GameMap gameMap, BaseArena arena) {
        super(player, 27, name);

        gui.setItem(11, GuiTools.format(
                createHead(gameMap.headTexture),
                GuiTools.colorize(Component.text("Map: ", Style.style(TextDecoration.BOLD)).append(Component.text(gameMap.name).decoration(TextDecoration.BOLD, false)), NamedTextColor.YELLOW),
                List.of(GuiTools.colorize(Component.text(gameMap.description), NamedTextColor.GRAY))
        ));

        gui.setItem(13, GuiTools.format(
                createHead(arena.texture),
                GuiTools.colorize(Component.text("Arena: ", Style.style(TextDecoration.BOLD)).append(Component.text(arena.name).decoration(TextDecoration.BOLD, false)), NamedTextColor.YELLOW),
                List.of(GuiTools.colorize(Component.text(arena.description), NamedTextColor.GRAY))
        ));

        ItemStack opponentButton = GuiTools.format(
                new ItemStack(Material.PLAYER_HEAD),
                getYellow("Opponents"),
                List.of(getGray("Click to select opponents!"))
        );
        actionManager.setAction(gui, opponentButton, 15, GuiAction.ofClick(() -> chooseOpponents(player)));

        updateCreateButton();
        actionManager.setAction(26, GuiAction.ofClick(() -> {
            if (createGame.getType() == Material.RED_CONCRETE) {
                player.sendMessage(Component.text(error, NamedTextColor.RED));
            } else {
                player.closeInventory();
                GameManager.createGame(TicketToRide.plugin, player, gameMap, arena);
                player.sendMessage("§aGame successfully created!");

                for (Player opponent : opponents) {
                    invitePlayer(opponent, player, false);
                }
            }
        }));
    }
    
    private void chooseOpponents(Player player) {
        player.closeInventory();
        SelectObjectGui<Opponent> opponentScreen = new SelectObjectGui<>(player, Component.text("Choose opponents to invite"));
        Component selectedText = GuiTools.colorize("Selected!", NamedTextColor.GREEN);
        Component unselectedText = GuiTools.colorize("Click to invite!", NamedTextColor.GRAY);

        ArrayList<SelectObjectGui<Opponent>.ObjectEntry> entries = new ArrayList<>(Bukkit.getOnlinePlayers().stream().filter(user -> !Objects.equals(user.getPlayer(), player)).map(user -> opponentScreen.new ObjectEntry(
                getYellow(user.getName()),
                opponents.contains(user) ? selectedText : unselectedText,
                user.getPlayerProfile().getTextures().getSkin().toString(),
                new Opponent(user),
                opponents.contains(user),
                GameManager.activePlayers.contains(user),
                GameManager.activePlayers.contains(user) ? colorize("Already in a game!", NamedTextColor.RED) : null
        )).toList());

        opponentScreen.initMultiselect(
                entries,
                (obj) -> obj.description = obj.selected ? selectedText : unselectedText ,
                (result) -> {
                    player.closeInventory();
                    player.openInventory(gui);
                    PlayerGuiManager.createGui(gui, player, actionManager, false);
                    opponents.clear();
                    opponents.addAll(result.stream().map(obj -> obj.element.player).toList());
                    if (opponents.isEmpty()) {
                        error = "You must invite some players first!";
                    } else {
                        error = "";
                    }
                    updateCreateButton();
                }
        );

        player.openInventory(opponentScreen.getInventory());
    }

    private void updateCreateButton() {
        createGame = GuiTools.format(
                new ItemStack(error.isEmpty() ? Material.GREEN_CONCRETE : Material.RED_CONCRETE),
                GuiTools.colorize(Component.text("Create Game"), error.isEmpty() ? NamedTextColor.GREEN : NamedTextColor.RED),
                List.of(GuiTools.colorize(Component.text(error), NamedTextColor.GRAY))
        );
        gui.setItem(26, createGame);
    }

    public static class Opponent {
        public Player player;
        
        public Opponent(Player player) {
            this.player = player;
        }
    }
}
