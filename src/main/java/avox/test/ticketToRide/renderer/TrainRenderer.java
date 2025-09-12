package avox.test.ticketToRide.renderer;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class TrainRenderer {
    public void spawnSmallTrainCar(World world, Location loc) {
        BlockDisplay blockDisplay = (BlockDisplay) world.spawnEntity(loc, EntityType.BLOCK_DISPLAY);

        // Sätt blocket till RED_CONCRETE
        blockDisplay.setBlock(Material.RED_CONCRETE.createBlockData());

        // Skala ner blocket (0.2 bred, 0.05 hög, 0.2 djup)
        blockDisplay.setTransformation(new Transformation(
                new Vector3f(0f, 0f, 0f),                        // translation (ingen förskjutning)
                new Quaternionf().rotateXYZ(0, 0, 0),           // rotation i radianer (justera efter behov)
                new Vector3f(0.2f, 0.05f, 0.2f),                // skalning
                new Quaternionf()                                // leftRotation, kan vara identity
        ));

        // Justera andra inställningar efter behov
        blockDisplay.setBillboard(Display.Billboard.FIXED); // blocket är fixerad, rotera ej mot spelare
        blockDisplay.setViewRange(64);
        blockDisplay.setPersistent(true);
    }
}
