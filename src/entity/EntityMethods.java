package entity;

import com.zeshanaslam.crimering.Main;
import net.minecraft.server.v1_10_R1.EntityInsentient;
import net.minecraft.server.v1_10_R1.EntityLiving;
import net.minecraft.server.v1_10_R1.NBTTagCompound;
import net.minecraft.server.v1_10_R1.PathfinderGoalFloat;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import script.ActionDefaults;
import script.ScriptObject;

import javax.script.*;
import java.util.Set;

import static me.libraryaddict.disguise.utilities.ReflectionManager.getNmsClass;
import static net.elseland.xikage.MythicLib.Util.MythicUtil.getPrivateField;

public class EntityMethods {

    public LivingEntity addCustomNBT(LivingEntity entity, String key, Object value) {
        if (entity == null) return null;

        Entity bukkitEntity = entity;
        net.minecraft.server.v1_10_R1.Entity nmsEntity = ((CraftEntity) bukkitEntity).getHandle();
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
                net.minecraft.server.v1_10_R1.Entity creature = (net.minecraft.server.v1_10_R1.Entity) nms_entity;
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

    public void runScript(Player player, LivingEntity entity, String script) {
        if (Main.instance.scriptsManager.contains(script)) {
            ScriptObject scriptObject = Main.instance.scriptsManager.getObject(script);

            try {
                ScriptEngine engine = Main.instance.scriptsManager.engine;
                CompiledScript compiledScript = scriptObject.script;

                // Objects
                Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
                bindings.put("player", player);
                bindings.put("CR", new ActionDefaults());
                bindings.put("x", entity.getLocation().getBlockX());
                bindings.put("y", entity.getLocation().getBlockY());
                bindings.put("z", entity.getLocation().getBlockZ());
                bindings.put("world", entity.getLocation().getWorld().getName());

                compiledScript.eval(bindings);
            } catch (ScriptException e) {
                e.printStackTrace();
            }
        }
    }
}
