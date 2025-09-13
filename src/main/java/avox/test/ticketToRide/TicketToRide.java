package avox.test.ticketToRide;

import avox.test.ticketToRide.commands.MainCommand;
import avox.test.ticketToRide.commands.TestCommand;
import avox.test.ticketToRide.config.ArenaManager;
import avox.test.ticketToRide.config.ConfigManager;
import avox.test.ticketToRide.listener.ClickListener;
import avox.test.ticketToRide.listener.GameRestrictionListener;
import avox.test.ticketToRide.listener.PlayerHandlerListener;
import avox.test.ticketToRide.config.MapManager;
import avox.test.ticketToRide.utils.PlayerStateManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class TicketToRide extends JavaPlugin {
    public static JavaPlugin plugin;
    public static PlayerStateManager playerStateManager;

    @Override
    public void onEnable() {
        plugin = this;
        playerStateManager = new PlayerStateManager(getDataFolder());
//        try {
//            File templatesFolder = new File(getDataFolder(), "maps/templates");
//            if (!templatesFolder.exists()) {
//                new ResourceExtractor(this).extractTemplates();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        ConfigManager configManager = new ConfigManager();
        configManager.setupConfig(plugin);

        ArenaManager.cleanOldArenaWorlds();
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> new MainCommand().register(commands.registrar()));
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> new TestCommand().register(commands.registrar()));

        getServer().getPluginManager().registerEvents(new ClickListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerHandlerListener(plugin), this);
        getServer().getPluginManager().registerEvents(new GameRestrictionListener(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static JsonObject loadJson(String resourcePath) {
        InputStream stream = TicketToRide.class.getResourceAsStream(resourcePath);
        if (stream == null) {
            throw new IllegalArgumentException("Resource not found: " + resourcePath);
        }
        try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return new Gson().fromJson(reader, JsonObject.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load JSON resource: " + resourcePath, e);
        }
    }
}
