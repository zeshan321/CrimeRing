package script;

import com.zeshanaslam.crimering.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import javax.script.*;

public class ActionInteract implements Listener {

    private final Main plugin;
    private final String typeInteract = "INTERACT-";

    public ActionInteract(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        // Only use right hand
        if (event.getHand() == EquipmentSlot.OFF_HAND) {
            return;
        }

        Player player = event.getPlayer();

        if (event.getItem() == null) {
            return;
        }

        ItemStack itemStack = event.getItem();

        int material = itemStack.getTypeId();
        short data = itemStack.getDurability();

        if (Main.instance.listeners.contains(player.getUniqueId(), typeInteract + material + ":" + data)) {
            ListenerObject listenerObject = Main.instance.listeners.get(player.getUniqueId(), typeInteract + material + ":" + data);

            Invocable invocable = (Invocable) listenerObject.engine;
            try {
                invocable.invokeFunction(listenerObject.method, event);
            } catch (ScriptException | NoSuchMethodException e) {
                e.printStackTrace();
            }
            return;
        }

        if (Main.instance.scriptsManager.contains(typeInteract + material + ":" + data)) {
            ScriptObject scriptObject = Main.instance.scriptsManager.getObject(typeInteract + material + ":" + data);

            try {
                ScriptEngine engine = Main.instance.scriptsManager.engine;
                CompiledScript compiledScript = scriptObject.script;

                // Objects
                Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
                bindings.put("player", player);
                bindings.put("event", event);
                bindings.put("CR", new ActionDefaults(typeInteract + material + ":" + data, engine));
                bindings.put("id", material);
                bindings.put("data", data);
                bindings.put("clickType", clickType(event.getAction().toString()));

                compiledScript.eval(bindings);
            } catch (ScriptException e) {
                e.printStackTrace();
            }
        }
    }

    private String clickType(String interact) {
        String type = "";

        if (interact.contains("RIGHT")) {
            type = "RIGHT";
        }

        if (interact.contains("LEFT")) {
            type = "LEFT";
        }

        return type;
    }
}