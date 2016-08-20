package script;

import com.zeshanaslam.crimering.Main;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

import javax.script.*;

public class ActionNPC implements Listener {

    private final Main plugin;

    public ActionNPC(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();

        // Only use right hand
        if (event.getHand() == EquipmentSlot.OFF_HAND) {
            return;
        }

        if (ActionCommands.clicks.containsKey(player.getName())) {
            return;
        }

        if (!(event.getRightClicked() instanceof LivingEntity)) {
            return;
        }

        LivingEntity interacted = (LivingEntity) event.getRightClicked();
        if (interacted.getCustomName() == null) {
            return;
        }

        if (Main.instance.scriptsManager.contains("NPC-" + ChatColor.stripColor(interacted.getCustomName()))) {
            ScriptObject scriptObject = Main.instance.scriptsManager.getObject("NPC-" + ChatColor.stripColor(interacted.getCustomName()));

            try {
                ScriptEngine engine = Main.instance.scriptsManager.engine;
                CompiledScript compiledScript = scriptObject.script;

                // Objects
                Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
                bindings.put("player", player);
                bindings.put("event", event);
                bindings.put("CR", new ActionDefaults());
                bindings.put("x", interacted.getLocation().getBlockX());
                bindings.put("y", interacted.getLocation().getBlockY());
                bindings.put("z", interacted.getLocation().getBlockZ());
                bindings.put("world", interacted.getLocation().getWorld().getName());

                compiledScript.eval(bindings);
            } catch (ScriptException e) {
                e.printStackTrace();
            }
        }
    }
}
