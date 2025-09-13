package avox.test.ticketToRide.commands;

import avox.test.ticketToRide.TicketToRide;
import avox.test.ticketToRide.config.ArenaManager;
import avox.test.ticketToRide.config.MapManager;
import avox.test.ticketToRide.game.Game;
import avox.test.ticketToRide.game.GameManager;
import avox.test.ticketToRide.game.player.GamePlayer;
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

public class MainCommand {
    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("t2r")
                .then(Commands.literal("create")
                        .then(Commands.argument("map", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    for (String map : MapManager.mapNames) {
                                        if (map.toLowerCase().startsWith(builder.getRemainingLowerCase())) {
                                            builder.suggest(map);
                                        }
                                    }
                                    return builder.buildFuture();
                                })
                                .then(Commands.argument("arena", StringArgumentType.word())
                                        .suggests((ctx, builder) -> {
                                            for (String arena : ArenaManager.arenaNames) {
                                                if (arena.toLowerCase().startsWith(builder.getRemainingLowerCase())) {
                                                    builder.suggest(arena);
                                                }
                                            }
                                            return builder.buildFuture();
                                        })
                                        .executes(ctx -> {
                                            if (!(ctx.getSource().getExecutor() instanceof Player)) return 0;
                                            Player sender = (Player) ctx.getSource().getSender();
                                            String map = StringArgumentType.getString(ctx, "map");
                                            String arena = StringArgumentType.getString(ctx, "arena");

                                            if (!(MapManager.mapNames.contains(map) && ArenaManager.arenaNames.contains(arena))) {
                                                sender.sendMessage("§cThis is not a valid map or arena!");
                                                return 0;
                                            }

                                            if (!GameManager.activePlayers.contains(sender)) {
                                                boolean succeeded = GameManager.createGame(TicketToRide.plugin, sender, map, arena);
                                                if (succeeded) {
                                                    sender.sendMessage("§aCreated new T2R with map §e" + map + " §aand arena §e" + arena + "§a!");
                                                } else {
                                                    sender.sendMessage("§cFailed to start game!");
                                                }
                                                return 1;
                                            } else {
                                                sender.sendMessage("§cYou are already in a game!");
                                                return 0;
                                            }
                                        })
                                )
                        )
                )
                .then(Commands.literal("start")
                        .executes(ctx -> {
                            Player sender = (Player) ctx.getSource().getSender();
                            Game game = getGameByOwner(sender);
                            if (game != null) {
                                if (game.started) {
                                    sender.sendMessage("§cThe game has already started!");
                                } else if (game.players.size() < 2) {
                                    sender.sendMessage("§cNot enough players! Must be at least two.");
                                } else {
                                    GameManager.startGame(game);
                                    return 1;
                                }
                            } else {
                                if (activePlayers.contains(sender)) {
                                    sender.sendMessage("§cYou do not have permission to do this! Ask the game host to execute this command!");
                                } else {
                                    sender.sendMessage("§cYou are not in a game! Create one first!");
                                }
                            }
                            return 0;
                        }))
                .then(Commands.literal("invite")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(this::getActivePlayers)
                                .executes(ctx -> {
                                    String targetName = StringArgumentType.getString(ctx, "player");
                                    Player target = Bukkit.getPlayerExact(targetName);
                                    Player sender = (Player) ctx.getSource().getSender();
                                    if (target == null) {
                                        sender.sendMessage("§cThat player is not online!");
                                        return 0;
                                    }
                                    Game game = getGameByOwner(sender);
                                    if (game != null) {
                                        if (GameManager.activePlayers.contains(target)) {
                                            sender.sendMessage("§cThat player is already in a game!");
                                            return 0;
                                        }

                                        game.invites.add(target);

                                        Component message = Component.text(sender.getName(), NamedTextColor.YELLOW)
                                                .append(Component.text(" invited you to a game of T2R! ", NamedTextColor.GRAY))
                                                .append(
                                                        Component.text("[Accept]", NamedTextColor.GREEN)
                                                                .clickEvent(ClickEvent.runCommand("/t2r accept " + sender.getName()))
                                                                .hoverEvent(HoverEvent.showText(Component.text("Accept the challenge!", NamedTextColor.GREEN)))
                                                )
                                                .append(Component.text(" "))
                                                .append(
                                                        Component.text("[Decline]", NamedTextColor.RED)
                                                                .clickEvent(ClickEvent.runCommand("/t2r decline " + sender.getName()))
                                                                .hoverEvent(HoverEvent.showText(Component.text("Decline the challenge!", NamedTextColor.RED)))
                                                );
                                        target.sendMessage(message);

                                        sender.sendMessage("§aInvited §e" + targetName + "§a to the game!");
                                        return 1;
                                    } else {
                                        sender.sendMessage("§cYou are not in charge of a game at the moment!");
                                    }
                                    return 0;
                                })
                        )
                )
                .then(Commands.literal("accept")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(this::getActivePlayers)
                                .executes(ctx -> {
                                    String targetName = StringArgumentType.getString(ctx, "player");
                                    Player target = Bukkit.getPlayerExact(targetName);
                                    Player sender = (Player) ctx.getSource().getSender();

                                    Game game = getGameByOwner(target);

                                    if (game != null) {
                                        if (game.players.size() >= 5) {
                                            sender.sendMessage("§cThis game is already full!");
                                            game.invites.remove(sender);
                                        } else if (game.invites.contains(sender)) {
                                            game.invites.remove(sender);
                                            game.addPlayer(sender);
                                            for (GamePlayer player : game.players) {
                                                player.player.sendMessage("§e" + sender.getName() + "§a joined the game!");
                                            }
                                            return 1;
                                        }
                                    }
                                    sender.sendMessage("§cYou have not received an invite from that player!");
                                    return 0;
                                })
                        )
                )
                .then(Commands.literal("decline")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(this::getActivePlayers)
                                .executes(ctx -> {
                                    String targetName = StringArgumentType.getString(ctx, "player");
                                    Player target = Bukkit.getPlayerExact(targetName);
                                    Player sender = (Player) ctx.getSource().getSender();

                                    Game game = getGameByOwner(target);
                                    if (game != null) {
                                        if (game.invites.contains(sender)) {
                                            game.invites.remove(sender);
                                            sender.sendMessage("&cYou declined the T2R game invitation.");
                                            for (GamePlayer player : game.players) {
                                                player.player.sendMessage("§e" + sender.getName() + " §cdeclined the invitation to the game!");
                                            }
                                            return 1;
                                        }
                                    }
                                    sender.sendMessage("§cYou have not received an invite from that player!");
                                    return 0;
                                })
                        )
                )
                .then(Commands.literal("leave")
                        .executes(ctx -> {
                            Player sender = (Player) ctx.getSource().getSender();
                            if (activePlayers.contains(sender)) {
                                Game game = activeGames.stream().filter(a -> a.members.contains(sender)).toList().getFirst();
                                game.leaveGame(sender);
                                return 1;
                            } else {
                                sender.sendMessage("§cYou are not in a game!");
                            }
                            return 0;
                        })
                )
                .build();
    }

    public void register(io.papermc.paper.command.brigadier.Commands commands) {
        commands.register(build(), "T2R command", List.of());
    }

    private CompletableFuture<Suggestions> getActivePlayers(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            builder.suggest(p.getName());
        }
        return builder.buildFuture();
    }
}
