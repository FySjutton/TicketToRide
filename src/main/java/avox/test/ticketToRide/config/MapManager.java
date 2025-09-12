package avox.test.ticketToRide.config;

import avox.test.ticketToRide.TicketToRide;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;

public class MapManager {
    public static ArrayList<String> mapNames = new ArrayList<>();

    public static void loadMapNames(File mapsFolder) {
        mapNames.clear();
        File[] folders = mapsFolder.listFiles(File::isDirectory);
        if (folders != null) {
            for (File folder : folders) {
                mapNames.add(folder.getName());
            }
        }
    }


}
