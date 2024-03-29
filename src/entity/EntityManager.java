package entity;

import com.zeshanaslam.crimering.Main;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import javax.script.ScriptEngine;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class EntityManager {

    public List<EntityObject> entityObjectList = new ArrayList<>();
    public List<UUID> entityUUID = new ArrayList<>();
    public EntityHider entityHider;

    public EntityManager() {
        entityHider = new EntityHider(Main.instance);
    }

    public void clear() {
        entityObjectList.clear();
        entityUUID.clear();
    }

    public void createEntityWithSkin(Player player, String type, String name, String skin, boolean hidden, String world, int x, int y, int z, float yaw, float pitch) {
        if (getEntityObject(player, name) != null) {
            EntityObject entityObject = getEntityObject(player, name);
            entityUUID.remove(entityObject.entity.getUniqueId());
            entityObject.entity.remove();
            entityObjectList.remove(entityObject);
        }

        Entity entity = Bukkit.getWorld(world).spawnEntity(new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch), EntityType.valueOf(type));
        entity.setCustomName(name);

        // Disguise
        PlayerDisguise disguise = new PlayerDisguise(name);
        disguise.getWatcher().setCustomNameVisible(true);
        disguise.getWatcher().setCustomName(name);
        disguise.getWatcher().setSkin(skin);

        DisguiseAPI.disguiseToAll(entity, disguise);

        if (hidden) {
            entityHider.hideEntity(entity, player);
        }

        // Remove AI
        //new EntityMethods().addCustomNBT((LivingEntity) entity, "NoAI", 1);

        // Remove sound
        new EntityMethods().addCustomNBT((LivingEntity) entity, "Silent", 1);

        // Entity flags
        ((LivingEntity) entity).setCollidable(false);
        ((LivingEntity) entity).setCanPickupItems(false);

        // Store data
        entityObjectList.add(new EntityObject(player, entity, hidden, entity.getUniqueId()));
        entityUUID.add(entity.getUniqueId());
    }

    public LivingEntity getEntity(Player player, String name) {
        for (EntityObject entityObject : entityObjectList) {
            if (entityObject.owner == player && ChatColor.stripColor(entityObject.entity.getCustomName()).equals(name)) {
                return (LivingEntity) entityObject.entity;
            }
        }
        return null;
    }

    public EntityObject getEntityObject(Player player, String name) {
        for (EntityObject entityObject : entityObjectList) {
            if (entityObject.owner == player && ChatColor.stripColor(name).equals(ChatColor.stripColor(entityObject.entity.getCustomName()))) {
                return entityObject;
            }
        }
        return null;
    }

    public void killEntity(Player player, String name) {
        EntityObject entityObject = getEntityObject(player, ChatColor.stripColor(name));

        if (entityObjectList.contains(entityObject)) {
            entityObjectList.remove(entityObject);

            if (entityObject.entity != null) {
                entityObject.entity.remove();
            }
        }
    }

    public void navigate(final Player player, final ScriptEngine engine, final LivingEntity entity, final String script, final int x, final int y, final int z) {
        EntityObject entityObject = getEntityObject(player, ChatColor.stripColor(entity.getCustomName()));

        new BukkitRunnable() {
            public void run() {
                if (entity.isDead() || !player.isOnline()) {
                    killEntity(player, entity.getCustomName());
                    this.cancel();
                    return;
                }

                // If players out of range teleport back to them
                /*if (player.getLocation().distance(entity.getLocation()) > 15) {
                    entity.teleport(player.getLocation());

                    // Start navigation again
                    new EntityMethods().handlePathfinders(new Location(entity.getWorld(), x, y, z), entity, 1.9);
                }*/

                // Trigger script when the entity reaches the location
                if (entity.getLocation().distance(new Location(entity.getWorld(), x, y, z)) <= 1) {
                    new EntityMethods().runScript(player, engine, entity, script);
                    this.cancel();
                    return;
                }

                if (entity.getLocation().getBlockX() == entityObject.lastX && entity.getLocation().getBlockY() == entityObject.lastY && entity.getLocation().getBlockZ() == entityObject.lastZ) {
                    entity.teleport(new Location(entity.getWorld(), entityObject.lastX, entityObject.lastY, entityObject.lastZ));
                    new EntityMethods().handlePathfinders(new Location(entity.getWorld(), x, y, z), entity, 1.9);
                }

                entityObject.lastX = entity.getLocation().getBlockX();
                entityObject.lastY = entity.getLocation().getBlockY();
                entityObject.lastZ = entity.getLocation().getBlockZ();
            }
        }.runTaskTimer(Main.instance, 0L, 40L);
    }
}
