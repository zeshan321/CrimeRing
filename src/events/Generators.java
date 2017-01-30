package events;

import com.zeshanaslam.crimering.Main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class Generators implements Listener {

    private final Main plugin;

    public Generators(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockFall(EntityChangeBlockEvent event) {
        if (event.getEntityType() == EntityType.FALLING_BLOCK) {
            int radius = 1;
            Block block = event.getBlock();

            for (int x = -(radius); x <= radius; x++) {
                for (int y = -(radius); y <= radius; y++) {
                    for (int z = -(radius); z <= radius; z++) {
                        if (block.getRelative(x, y, z).getType() == Material.PRISMARINE) {
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
        int radius = 1;
        Block block = event.getBlock();
        Block newBlock = null;

        for (int x = -(radius); x <= radius; x++) {
            for (int y = -(radius); y <= radius; y++) {
                for (int z = -(radius); z <= radius; z++) {
                    if (block.getRelative(x, y, z).getType() == Material.PRISMARINE) {
                        newBlock = block.getRelative(x, y, z);
                    }
                }
            }
        }

        if (newBlock != null) {
            if (block.getLocation().distance(newBlock.getLocation()) == 0.0) {
                return;
            }

            if (block.getY() < newBlock.getY()) {
                return;
            }

            event.setCancelled(true);
        }
    }
}
