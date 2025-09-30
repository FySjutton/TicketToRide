package avox.test.ticketToRide.util;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PlayerStateManager {
    private final File dataFolder;
    private final Map<UUID, PlayerState> cachedStates = new HashMap<>();

    public PlayerStateManager(File pluginDataFolder) {
        this.dataFolder = new File(pluginDataFolder, "playerdata");
        if (!this.dataFolder.exists()) this.dataFolder.mkdirs();
    }

    public void savePlayer(Player player) {
        PlayerState state = new PlayerState(player);
        cachedStates.put(player.getUniqueId(), state);

        File file = new File(dataFolder, player.getUniqueId() + ".yml");
        state.saveToFile(file);
    }

    public void restorePlayer(Player player) {
        PlayerState state = cachedStates.remove(player.getUniqueId());
        if (state == null) {
            File file = new File(dataFolder, player.getUniqueId() + ".yml");
            if (file.exists()) {
                state = PlayerState.loadFromFile(file);
                file.delete();
            }
        }

        if (state != null) {
            state.apply(player);
        }
    }

    private static class PlayerState {
        private final ItemStack[] contents;
        private final ItemStack[] armor;
        private final double health;
        private final int food;
        private final float exp;
        private final int level;
        private final Location location;
        private final GameMode gamemode;

        PlayerState(Player player) {
            this.contents = player.getInventory().getContents();
            this.armor = player.getInventory().getArmorContents();
            this.health = player.getHealth();
            this.food = player.getFoodLevel();
            this.exp = player.getExp();
            this.level = player.getLevel();
            this.location = player.getLocation().clone();
            this.gamemode = player.getGameMode();
        }

        void apply(Player player) {
            player.getInventory().setContents(contents);
            player.getInventory().setArmorContents(armor);
            player.setHealth(Math.min(health, player.getMaxHealth()));
            player.setFoodLevel(food);
            player.setExp(exp);
            player.setLevel(level);
            player.setGameMode(gamemode);
            player.teleport(location);
        }

        void saveToFile(File file) {
            YamlConfiguration config = new YamlConfiguration();
            config.set("contents", contents);
            config.set("armor", armor);
            config.set("health", health);
            config.set("food", food);
            config.set("exp", exp);
            config.set("level", level);
            config.set("location", location);
            config.set("gamemode", gamemode.name());
            try {
                config.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        static PlayerState loadFromFile(File file) {
            YamlConfiguration config = new YamlConfiguration();
            try {
                config.load(file);
                ItemStack[] contents = ((List<ItemStack>) config.get("contents")).toArray(new ItemStack[0]);
                ItemStack[] armor = ((List<ItemStack>) config.get("armor")).toArray(new ItemStack[0]);
                double health = config.getDouble("health");
                int food = config.getInt("food");
                float exp = (float) config.getDouble("exp");
                int level = config.getInt("level");
                Location location = (Location) config.get("location");
                GameMode gamemode = GameMode.valueOf(config.getString("gamemode", "SURVIVAL"));

                return new PlayerState(contents, armor, health, food, exp, level, location, gamemode);
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }
            return null;
        }

        PlayerState(ItemStack[] contents, ItemStack[] armor, double health, int food, float exp, int level, Location location, GameMode gamemode) {
            this.contents = contents;
            this.armor = armor;
            this.health = health;
            this.food = food;
            this.exp = exp;
            this.level = level;
            this.location = location;
            this.gamemode = gamemode;
        }
    }
}
