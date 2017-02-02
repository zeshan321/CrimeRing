package entity;

import net.minecraft.server.v1_11_R1.EntityInsentient;
import net.minecraft.server.v1_11_R1.EntityLiving;
import net.minecraft.server.v1_11_R1.NBTTagCompound;
import net.minecraft.server.v1_11_R1.PathfinderGoalFloat;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.util.Set;

import static io.lumine.xikage.mythicmobs.utils.MythicUtil.getPrivateField;
import static me.libraryaddict.disguise.utilities.ReflectionManager.getNmsClass;

public class EntityMethods {

    public LivingEntity addCustomNBT(LivingEntity entity, String key, Object value) {
        if (entity == null) return null;

        Entity bukkitEntity = entity;
        net.minecraft.server.v1_11_R1.Entity nmsEntity = ((CraftEntity) bukkitEntity).getHandle();
        NBTTagCompound tag = new NBTTagCompound();

        // Writes the entity's NBT data to tag
        nmsEntity.c(tag);

        // Add custom NBT
        if (value instanceof Integer) {
            tag.setInt(key, (int) value);
        }

        if (value instanceof String) {
            tag.setString(key, (String) value);
        }

        // Write tag back
        ((EntityLiving) nmsEntity).a(tag);

        return entity;
    }

    public void handlePathfinders(Location loc, org.bukkit.entity.Entity e, double speed) {
        try {
            Object nms_entity = ((CraftEntity) e).getHandle();
            if ((nms_entity instanceof EntityInsentient)) {
                net.minecraft.server.v1_11_R1.Entity creature = (net.minecraft.server.v1_11_R1.Entity) nms_entity;
                Set goalB = (Set) getPrivateField("b", getNmsClass("PathfinderGoalSelector"), ((EntityInsentient) creature).goalSelector);
                goalB.clear();
                Set goalC = (Set) getPrivateField("c", getNmsClass("PathfinderGoalSelector"), ((EntityInsentient) creature).goalSelector);
                goalC.clear();
                Set targetB = (Set) getPrivateField("b", getNmsClass("PathfinderGoalSelector"), ((EntityInsentient) creature).targetSelector);
                targetB.clear();
                Set targetC = (Set) getPrivateField("c", getNmsClass("PathfinderGoalSelector"), ((EntityInsentient) creature).targetSelector);
                targetC.clear();
                ((EntityInsentient) creature).goalSelector.a(0, new PathfinderGoalFloat((EntityInsentient) creature));
                ((EntityInsentient) creature).goalSelector.a(2, new PathFinderGoalWalkToLoc((EntityInsentient) creature, loc, speed));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void runScript(Player player, ScriptEngine engine, LivingEntity entity, String script) {
        if (script.equalsIgnoreCase("none")) {
            return;
        }

        Invocable invocable = (Invocable) engine;
        try {
            invocable.invokeFunction(script);
        } catch (ScriptException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
}
