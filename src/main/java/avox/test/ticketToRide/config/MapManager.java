package avox.test.ticketToRide.config;

import java.io.File;
import java.util.ArrayList;

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
