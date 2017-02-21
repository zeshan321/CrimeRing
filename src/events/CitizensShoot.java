package events;

import com.shampaggon.crackshot.events.WeaponShootEvent;
import com.zeshanaslam.crimering.Main;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CitizensShoot implements Listener {

    private final Main plugin;

    public CitizensShoot(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onWeaponShoot(WeaponShootEvent event) {
        Player player = event.getPlayer();

        Collection<Entity> eTargets = new ArrayList<>();
        eTargets.add(player);

        for (Entity entity : getNearbyCitizens(player.getLocation(), 10)) {
            MythicMob mob = Main.instance.mythicAPI.getMythicMobInstance(entity).getType();

            String skill = "citizenToFlee" + mob.getInternalName().replace("Citizen", "");
            Main.instance.mythicAPI.castSkill(entity, skill, player, player.getLocation(), eTargets, null, 1.0F);
        }
    }

    private List<Entity> getNearbyCitizens(Location location, int range) {
        List<Entity> entities = new ArrayList<>();

        for (Entity entity : location.getWorld().getEntities()) {
            if (entity.getLocation().distance(location) > range) {
                continue;
            }

            if (Main.instance.mythicAPI.isMythicMob(entity)) {
                if (Main.instance.mythicAPI.getMythicMobInstance(entity).getType().getInternalName().startsWith("Citizen")) {
                    entities.add(entity);
                }
            }
        }
        return entities;
    }

}
