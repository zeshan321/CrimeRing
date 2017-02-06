package entity;

import com.zeshanaslam.crimering.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Iterator;

public class EntityListener implements Listener {

    private final Main plugin;

    public EntityListener(Main plugin) {
        this.plugin = plugin;
    }


    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (Main.instance.entityManager.entityUUID.contains(event.getEntity().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        Iterator<EntityObject> iterator = Main.instance.entityManager.entityObjectList.iterator();

        while (iterator.hasNext()) {
            EntityObject entityObject = iterator.next();

            if (entityObject.entity.getUniqueId() == event.getEntity().getUniqueId()) {
                plugin.entityManager.entityUUID.remove(entityObject.entity.getUniqueId());

                entityObject.entity.remove();

                iterator.remove();

            }
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Iterator<EntityObject> iterator = Main.instance.entityManager.entityObjectList.iterator();

        while (iterator.hasNext()) {
            EntityObject entityObject = iterator.next();

            if (entityObject.owner == event.getPlayer()) {
                plugin.entityManager.entityUUID.remove(entityObject.entity.getUniqueId());

                entityObject.entity.remove();

                iterator.remove();

            }
        }
    }
}
