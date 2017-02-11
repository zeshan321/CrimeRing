package damage;

import com.zeshanaslam.crimering.Main;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class DamageListener implements Listener {

    private final Main plugin;

    public DamageListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getDamager();

            if (arrow.getShooter() instanceof Entity) {
                Entity shooter = (Entity) arrow.getShooter();

                if (shooter.getCustomName() != null && plugin.damageManager.damages.containsKey(shooter.getCustomName())) {
                    if (!event.isCancelled()) {
                        event.setDamage(plugin.damageManager.damages.get(shooter.getCustomName()));
                    }
                }
            }
        }
    }
}
