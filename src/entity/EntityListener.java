package entity;

import com.zeshanaslam.crimering.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
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
    public void onJoin(PlayerJoinEvent event) {
        Main.instance.entityManager.entityObjectList.stream().filter(entityObject -> entityObject.hidden).forEach(entityObject -> Main.instance.entityManager.entityHider.hideEntity(event.getPlayer(), entityObject.entity));
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Iterator<EntityObject> iterator = Main.instance.entityManager.entityObjectList.iterator();

        while (iterator.hasNext()) {
            EntityObject entityObject = iterator.next();

            if (entityObject.owner == event.getPlayer()) {
                iterator.remove();
                Main.instance.entityManager.entityUUID.remove(entityObject.entity.getUniqueId());

                entityObject.entity.remove();
            }
        }
    }
}
