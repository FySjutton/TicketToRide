package avox.test.ticketToRide.utils;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.file.*;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ResourceExtractor {
    private final JavaPlugin plugin;

    public ResourceExtractor(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void extractTemplates() throws IOException {
        File pluginFolder = plugin.getDataFolder();
        File templatesFolder = new File(pluginFolder, "maps/templates");

        if (!templatesFolder.exists()) {
            templatesFolder.mkdirs();
        }

        String pathInJar = "maps/templates/";

        String jarPath = plugin.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        try (JarFile jar = new JarFile(new File(jarPath))) {
            Enumeration<JarEntry> entries = jar.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();

                if (name.startsWith(pathInJar)) {
                    String relativePath = name.substring(pathInJar.length());

                    if (relativePath.isEmpty()) continue;

                    File outFile = new File(templatesFolder, relativePath);

                    if (entry.isDirectory()) {
                        outFile.mkdirs();
                    } else {
                        try (InputStream in = jar.getInputStream(entry)) {
                            Files.copy(in, outFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                }
            }
        }
    }
}
