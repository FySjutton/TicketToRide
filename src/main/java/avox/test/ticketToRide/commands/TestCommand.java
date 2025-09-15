package avox.test.ticketToRide.commands;

import avox.test.ticketToRide.game.*;
import avox.test.ticketToRide.game.player.GamePlayer;
import avox.test.ticketToRide.utils.board.MarkerManager;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

import static avox.test.ticketToRide.game.GameManager.*;

public class TestCommand {
    public static ItemDisplay pointA;
    public static ItemDisplay pointB;

    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("test")
                .then(Commands.literal("add_point")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                            .executes(ctx -> {
                                Player sender = (Player) ctx.getSource().getSender();
                                Game game = getGameByUser(sender);
                                GamePlayer player = game.gamePlayers.get(sender);
                                player.points += IntegerArgumentType.getInteger(ctx, "amount");
                                new MarkerManager().reposition(game, player, player.points);
                                return 1;
                            })))
                .then(Commands.literal("random_city")
                        .executes(ctx -> {
                            Player sender = (Player) ctx.getSource().getSender();

                            Game game = getGameByUser(sender);
                            if (game == null) return 0;
                            GameMap map = game.gameMap;
                            Arena arena = game.arena;

                            City a = map.getRandomCity();
                            City b = map.getRandomCity();

                            System.out.println("heer");
                            RewardCalculator rewardCalculator = new RewardCalculator();
                            int reward = rewardCalculator.getReward(map, a, b);
//                            pathFinder.test(map, sender);

                            sender.sendMessage("§e§l" + reward + "§r§afor A: §e" + a.name() + "§a, B: §e" + b.name());
                            System.out.println("heer2");

                            if (pointA != null) {
                                pointA.remove();
                                pointB.remove();
                            }


                            Location spawnLocation = map.getStartLocation(arena).clone().add((double) (a.x()) / 128, 0.05, (double) (a.y()) / 128);

                            pointA = (ItemDisplay) arena.world.spawnEntity(spawnLocation, EntityType.ITEM_DISPLAY);
                            pointA.setItemStack(new ItemStack(Material.RED_CONCRETE));

                            pointA.setTransformation(new Transformation(
                                    new Vector3f(0f, 0f, 0f),
                                    new Quaternionf(),
                                    new Vector3f(0.01f, 1f, 0.01f),
                                    new Quaternionf()
                            ));

                            pointA.setBillboard(Display.Billboard.FIXED);
                            pointA.setViewRange(20);
                            pointA.setPersistent(true);

                            Location spawnLocation2 = map.getStartLocation(arena).clone().add((double) (b.x()) / 128, 0.05, (double) (b.y()) / 128);

                            pointB = (ItemDisplay) arena.world.spawnEntity(spawnLocation2, EntityType.ITEM_DISPLAY);
                            pointB.setItemStack(new ItemStack(Material.RED_CONCRETE));

                            pointB.setTransformation(new Transformation(
                                    new Vector3f(0f, 0f, 0f),
                                    new Quaternionf(),
                                    new Vector3f(0.01f, 1f, 0.01f),
                                    new Quaternionf()
                            ));

                            pointB.setBillboard(Display.Billboard.FIXED);
                            pointB.setViewRange(20);
                            pointB.setPersistent(true);
                            return 1;
                        }))
                .build();
    }

    public void register(io.papermc.paper.command.brigadier.Commands commands) {
        commands.register(build(), "T2R command", List.of());
    }
}
