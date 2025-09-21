package avox.test.ticketToRide.commands;

import avox.test.ticketToRide.game.*;
import avox.test.ticketToRide.game.player.GamePlayer;
import avox.test.ticketToRide.guis.createGame.CreateGameGui;
import avox.test.ticketToRide.guis.general.SelectObjectGui;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static avox.test.ticketToRide.config.ArenaManager.arenas;
import static avox.test.ticketToRide.config.MapManager.maps;
import static avox.test.ticketToRide.game.GameManager.*;
import static avox.test.ticketToRide.guis.GuiTools.getGray;
import static avox.test.ticketToRide.guis.GuiTools.getYellow;

public class MainCommand {
    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("t2r")
                .then(Commands.literal("create")
                    .executes(ctx -> {
                        if (!(ctx.getSource().getExecutor() instanceof Player)) return 0;
                        Player sender = (Player) ctx.getSource().getSender();
                        if (!GameManager.activePlayers.contains(sender)) {
                            // Todo: Autoselect maps / arena if there's only one available / installed
                            SelectObjectGui<GameMap> mapGui = new SelectObjectGui<>(sender, Component.text("Choose Map"));
                            ArrayList<SelectObjectGui<GameMap>.ObjectEntry> options = new ArrayList<>(maps.stream().map(map -> mapGui.new ObjectEntry(getYellow(map.name), getGray(map.description), map.headTexture, map)).toList());
                            if (options.isEmpty()) {
                                if (sender.isOp()) {
                                    sender.sendMessage(Component.text("There are no maps installed! Please install one using <HERE>.", NamedTextColor.RED));
                                } else {
                                    sender.sendMessage(Component.text("There are no maps installed! Please contact a staff member to install one.", NamedTextColor.RED));
                                }
                                mapGui.getInventory().close();
                                return 0;
                            }

                            mapGui.init(options, option -> {
                                sender.closeInventory();
                                GameMap gameMap = option.element;
                                SelectObjectGui<BaseArena> arenaGui = new SelectObjectGui<>(sender, Component.text("Choose Arena"));

                                ArrayList<SelectObjectGui<BaseArena>.ObjectEntry> arenaOptions = new ArrayList<>(arenas.stream().filter(arena -> gameMap.tilesX == arena.tileX && gameMap.tilesY == arena.tileY).map(arena -> arenaGui.new ObjectEntry(getYellow(arena.name), getGray(arena.description), arena.texture, arena)).toList());
                                if (arenaOptions.isEmpty()) {
                                    if (sender.isOp()) {
                                        sender.sendMessage(Component.text("There are no arenas installed that works with this map! Please install one using <HERE>.", NamedTextColor.RED));
                                    } else {
                                        sender.sendMessage(Component.text("There are no arenas installed that works with this map! Please contact a staff member to install one.", NamedTextColor.RED));
                                    }
                                    mapGui.getInventory().close();
                                    return;
                                }
                                arenaGui.init(arenaOptions, arenaOption -> {
                                    sender.closeInventory();
                                    BaseArena chosenArena = arenaOption.element;

                                    sender.openInventory(new CreateGameGui(sender, Component.text("Create T2R Game"), gameMap, chosenArena).getInventory());
                                });

                                sender.openInventory(arenaGui.getInventory());
                            });

                            sender.openInventory(mapGui.getInventory());
//                            boolean succeeded = GameManager.createGame(TicketToRide.plugin, sender, map, arena);
//                            if (succeeded) {
//                                sender.sendMessage("§aCreated new T2R with map §e" + map + " §aand arena §e" + arena + "§a!");
//                            } else {
//                                sender.sendMessage("§cFailed to start game!");
//                            }
                            return 1;
                        } else {
                            sender.sendMessage("§cYou are already in a game!");
                            return 0;
                        }
                    })
                )
                .then(Commands.literal("start")
                        .executes(ctx -> {
                            Player sender = (Player) ctx.getSource().getSender();
                            Game game = getGameByOwner(sender);
                            if (game != null) {
                                if (game.started) {
                                    sender.sendMessage("§cThe game has already started!");
                                } else if (game.gamePlayers.size() < 2) {
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
                                    return invitePlayer(target, sender, true);
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
                                        if (game.gamePlayers.size() >= 5) {
                                            sender.sendMessage("§cThis game is already full!");
                                            game.invites.remove(sender);
                                        } else if (game.invites.contains(sender)) {
                                            game.invites.remove(sender);
                                            game.addPlayer(sender);
                                            game.broadcast("§e" + sender.getName() + "§a joined the game!");
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
                                            game.broadcast("§e" + sender.getName() + " §cdeclined the invitation to the game!");
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
                                Game game = activeGames.stream().filter(a -> a.gamePlayers.containsKey(sender)).toList().getFirst();
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

    public static int invitePlayer(Player target, Player sender, boolean sendToSender) {
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

            if (sendToSender) {
                sender.sendMessage("§aInvited §e" + target.getName() + "§a to the game!");
            }
            return 1;
        } else {
            sender.sendMessage("§cYou are not in charge of a game at the moment!");
        }
        return 0;
    }
}
