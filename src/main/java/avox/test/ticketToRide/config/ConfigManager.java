package avox.test.ticketToRide.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

import static avox.test.ticketToRide.config.ArenaManager.loadArenaNames;
import static avox.test.ticketToRide.config.MapManager.loadMapNames;

public class ConfigManager {
    private File mapsFolder;
    private File arenasFolder;

    public void setupConfig(JavaPlugin plugin) {
        plugin.saveDefaultConfig();

        mapsFolder = new File(plugin.getDataFolder(), "maps");
        arenasFolder = new File(plugin.getDataFolder(), "arenas");

        if (!mapsFolder.exists()) mapsFolder.mkdirs();
        if (!arenasFolder.exists()) arenasFolder.mkdirs();

        loadMapNames(mapsFolder);
        loadArenaNames(arenasFolder);
    }

    public FileConfiguration loadCustomConfig(File folder, String fileName) {
        File file = new File(folder, fileName);
        return YamlConfiguration.loadConfiguration(file);
    }

    public void saveCustomConfig(FileConfiguration config, File folder, String fileName) {
        try {
            config.save(new File(folder, fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
