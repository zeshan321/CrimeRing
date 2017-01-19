package events;

import com.zeshanaslam.crimering.Main;
import customevents.ArrowHitBlockEvent;
import net.minecraft.server.v1_11_R1.EntityArrow;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

import java.lang.reflect.Field;

public class ArrowHitBlockListener implements Listener {

    private final Main plugin;

    public ArrowHitBlockListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntityType() == EntityType.ARROW) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                try {
                    EntityArrow entityArrow = ((CraftArrow) event.getEntity()).getHandle();

                    Field fieldX = EntityArrow.class.getDeclaredField("h");
                    Field fieldY = EntityArrow.class.getDeclaredField("at");
                    Field fieldZ = EntityArrow.class.getDeclaredField("au");

                    fieldX.setAccessible(true);
                    fieldY.setAccessible(true);
                    fieldZ.setAccessible(true);

                    int x = fieldX.getInt(entityArrow);
                    int y = fieldY.getInt(entityArrow);
                    int z = fieldZ.getInt(entityArrow);

                    if ((x != -1) && (y != -1) && (z != -1)) {
                        Block block = event.getEntity().getWorld().getBlockAt(x, y, z);
                        Bukkit.getServer().getPluginManager().callEvent(new ArrowHitBlockEvent((Arrow) event.getEntity(), block, event.getEntity().getShooter()));
                    }

                } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException exception) {
                    exception.printStackTrace();
                }
            });
        }
    }
}
