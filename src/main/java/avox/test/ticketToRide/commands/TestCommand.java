package avox.test.ticketToRide.commands;

import avox.test.ticketToRide.game.Game;
import avox.test.ticketToRide.game.GameManager;
import avox.test.ticketToRide.game.player.GamePlayer;
import avox.test.ticketToRide.utils.board.MarkerManager;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static avox.test.ticketToRide.game.GameManager.*;

public class TestCommand {
    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("test")
                .then(Commands.literal("add_point")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                            .executes(ctx -> {
                                Player sender = (Player) ctx.getSource().getSender();
                                Game game = getGameByUser(sender);
                                GamePlayer player = game.players.stream().filter(p -> p.player == sender).toList().getFirst();
                                player.points += IntegerArgumentType.getInteger(ctx, "amount");
                                new MarkerManager().reposition(game, player, player.points);
                                return 1;
                            })))
                .build();
    }

    public void register(io.papermc.paper.command.brigadier.Commands commands) {
        commands.register(build(), "T2R command", List.of());
    }
}
