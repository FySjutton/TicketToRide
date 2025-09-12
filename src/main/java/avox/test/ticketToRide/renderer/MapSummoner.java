package avox.test.ticketToRide.renderer;

import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class MapSummoner {
    public void generateAndDisplay(World world, File imageFile, Location baseLocation, int tilesX, int tilesY) {
        int tileSize = 128;
        int totalWidth = tileSize * tilesX;
        int totalHeight = tileSize * tilesY;

        BufferedImage image;
        try {
            image = ImageIO.read(imageFile);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        BufferedImage finalImage = new BufferedImage(totalWidth, totalHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = finalImage.createGraphics();

        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, totalWidth, totalHeight);

        int paddingX = (totalWidth - image.getWidth()) / 2;   // 12
        int paddingY = (totalHeight - image.getHeight()) / 2; // 12
        g2d.drawImage(image, paddingX, paddingY, null);
        g2d.dispose();

        for (int y = 0; y < tilesY; y++) {
            for (int x = 0; x < tilesX; x++) {
                BufferedImage tile = finalImage.getSubimage(
                        x * tileSize,
                        y * tileSize,
                        tileSize,
                        tileSize
                );

                MapView mapView = Bukkit.createMap(world);
                mapView.getRenderers().clear();
                mapView.addRenderer(new StaticImageRenderer(tile));

                ItemStack mapItem = new ItemStack(Material.FILLED_MAP);
                MapMeta meta = (MapMeta) mapItem.getItemMeta();
                assert meta != null;
                meta.setMapView(mapView);
                mapItem.setItemMeta(meta);

                Location loc = baseLocation.clone().add(x, 0, y);
                ItemFrame frame = (ItemFrame) world.spawnEntity(loc, EntityType.ITEM_FRAME);
                frame.setFacingDirection(BlockFace.UP, true);
                frame.setInvisible(true);
                frame.setFixed(true);
                frame.setInvulnerable(true);
                frame.setNoPhysics(true);
                frame.setGravity(false);
                frame.setSilent(true);
                frame.setItem(mapItem);
            }
        }
    }

    private static class StaticImageRenderer extends MapRenderer {
        private final BufferedImage img;
        private boolean rendered = false;

        public StaticImageRenderer(BufferedImage img) {
            this.img = img;
        }

        @Override
        public void render(MapView view, MapCanvas canvas, Player player) {
            if (rendered) return;
            canvas.drawImage(0, 0, img);
            rendered = true;
        }
    }
}
