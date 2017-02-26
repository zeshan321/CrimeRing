package events;

import com.zeshanaslam.crimering.Main;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;

import java.util.ArrayList;
import java.util.List;

public class Generators implements Listener {

    private final Main plugin;
    private final List<BlockFace> faceList;

    public Generators(Main plugin) {
        this.plugin = plugin;

        faceList = new ArrayList<>();
        faceList.add(BlockFace.UP);
        faceList.add(BlockFace.NORTH);
        faceList.add(BlockFace.EAST);
        faceList.add(BlockFace.SOUTH);
        faceList.add(BlockFace.WEST);
    }

    @EventHandler
    public void onBlockFall(EntityChangeBlockEvent event) {
        if (event.getEntityType() == EntityType.FALLING_BLOCK) {
            int radius = 1;
            Block block = event.getBlock();

            for (int x = -(radius); x <= radius; x++) {
                for (int y = -(radius); y <= radius; y++) {
                    for (int z = -(radius); z <= radius; z++) {
                        if (block.getRelative(x, y, z).getType().name().toLowerCase().contains("prismarine")) {
                            if (block.getLocation().distance(block.getRelative(x, y, z).getLocation()) == 0.0) {
                                continue;
                            }

                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();

        if (block.getType().name().toLowerCase().contains("prismarine")) {
            for (BlockFace blockFace : faceList) {
                if (block.getRelative(blockFace).getType() != Material.AIR) {
                    event.setCancelled(true);
                }
            }
        } else {
            for (BlockFace blockFace : faceList) {
                if (block.getRelative(blockFace).getType().name().toLowerCase().contains("prismarine")) {
                    event.setCancelled(true);
                }
            }

            if (block.getRelative(BlockFace.DOWN).getType().name().toLowerCase().contains("prismarine")) {
                event.setCancelled(true);
            }
        }
    }
}
