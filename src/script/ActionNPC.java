package script;

import com.zeshanaslam.crimering.Main;
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
    private final String typeNPC = "NPC-";

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

        if (!Main.instance.entityManager.entityHider.canSee(player, event.getRightClicked())) {
            return;
        }

        if (Main.instance.listeners.contains(player.getUniqueId(), typeNPC + interacted.getCustomName())) {
            ListenerObject listenerObject = Main.instance.listeners.get(player.getUniqueId(), typeNPC + interacted.getCustomName());

            Invocable invocable = (Invocable) listenerObject.engine;
            try {
                invocable.invokeFunction(listenerObject.method, event);
            } catch (ScriptException | NoSuchMethodException e) {
                e.printStackTrace();
            }
            return;
        }

        String customName = interacted.getCustomName().replace("§", "&");
        if (Main.instance.scriptsManager.contains(typeNPC + customName)) {
            ScriptObject scriptObject = Main.instance.scriptsManager.getObject(typeNPC + customName);

            try {
                ScriptEngine engine = Main.instance.scriptsManager.engine;
                CompiledScript compiledScript = scriptObject.script;

                // Objects
                Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
                bindings.put("player", player);
                bindings.put("event", event);
                bindings.put("CR", new ActionDefaults(typeNPC + customName, engine));
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
