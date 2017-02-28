package events;

import com.shampaggon.crackshot.events.WeaponShootEvent;
import com.zeshanaslam.crimering.Main;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import perks.arrest.CopUtil;

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

        for (Entity entity : getNearbyCitizens(player.getLocation(), 15)) {
            MythicMob mob = Main.instance.mythicAPI.getMythicMobInstance(entity).getType();

            String skill = "citizenToFlee" + mob.getInternalName().replace("Citizen", "");
            Main.instance.mythicAPI.castSkill(entity, skill, player, player.getLocation(), eTargets, null, 1.0F);
        }
    }

    @EventHandler
    public void onWeaponShootCop(WeaponShootEvent event) {
        Player player = event.getPlayer();

        if (new CopUtil().isCop(player)) {
            return;
        }

        for (Entity entity : getNearbyCops(player.getLocation(), 15)) {
            plugin.actionDefaults.addMMThreat(entity, player, 10000);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        Entity entity1 = event.getDamager();

        if(entity1 instanceof Arrow){
            Arrow arrow = (Arrow) event.getDamager();

            entity1 = (Entity) arrow.getShooter();
        }

        if (entity1 instanceof Player) {
            Player player = (Player) entity1;

            if (new CopUtil().isCop(player)) {
                return;
            }
        }

        if (Main.instance.mythicAPI.isMythicMob(entity)) {
            if (Main.instance.mythicAPI.getMythicMobInstance(entity).getType().getInternalName().startsWith("Cop")) {
                return;
            }

            if (!Main.instance.mythicAPI.getMythicMobInstance(entity).getType().getInternalName().startsWith("Citizen")) {
                if (!(entity instanceof Player)) {
                    return;
                }
            }
        }

        for (Entity cops : getNearbyCops(entity.getLocation(), 15)) {
            plugin.actionDefaults.addMMThreat(cops, (LivingEntity) entity1, 10000);
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

    private List<Entity> getNearbyCops(Location location, int range) {
        List<Entity> entities = new ArrayList<>();

        for (Entity entity : location.getWorld().getEntities()) {
            if (entity.getLocation().distance(location) > range) {
                continue;
            }

            if (Main.instance.mythicAPI.isMythicMob(entity)) {
                if (Main.instance.mythicAPI.getMythicMobInstance(entity).getType().getInternalName().startsWith("Cop")) {
                    entities.add(entity);
                }
            }
        }
        return entities;
    }
}
