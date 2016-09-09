package script;

import com.zeshanaslam.crimering.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import javax.script.*;

public class ActionConsume implements Listener {

    private final Main plugin;

    public ActionConsume(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();

        ItemStack itemStack = event.getItem();

        int material = itemStack.getTypeId();
        short data = itemStack.getDurability();

        if (Main.instance.listeners.contains(player.getUniqueId(), "CONSUME-" + material + " " + data)) {
            // Cancel event and remove item
            event.setCancelled(true);
            updateHand(player);

            ListenerObject listenerObject = Main.instance.listeners.get(player.getUniqueId(), "CONSUME-" + material + " " + data);

            Invocable invocable = (Invocable) listenerObject.engine;
            try {
                invocable.invokeFunction(listenerObject.method, event);
            } catch (ScriptException | NoSuchMethodException e) {
                e.printStackTrace();
            }
            return;
        }

        if (Main.instance.scriptsManager.contains(material + " " + data)) {
            // Cancel event and remove item
            event.setCancelled(true);
            updateHand(player);

            ScriptObject scriptObject = Main.instance.scriptsManager.getObject(material + " " + data);

            try {
                ScriptEngine engine = Main.instance.scriptsManager.engine;
                CompiledScript compiledScript = scriptObject.script;

                // Objects
                Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
                bindings.put("player", player);
                bindings.put("event", event);
                bindings.put("CR", new ActionDefaults(material + " " + data, engine));
                bindings.put("id", material);
                bindings.put("data", data);

                compiledScript.eval(bindings);
            } catch (ScriptException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateHand(Player player) {
        if (player.getInventory().getItemInMainHand().getAmount() > 1) {
            player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
    }
}
