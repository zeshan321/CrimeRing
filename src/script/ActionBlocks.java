package script;

import com.zeshanaslam.crimering.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import javax.script.*;

public class ActionBlocks implements Listener {

    private final Main plugin;

    public ActionBlocks(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // Only use right hand
        if (event.getHand() == EquipmentSlot.OFF_HAND) {
            return;
        }

        // Check if player is using action copy
        if (ActionCommands.clicks.containsKey(player.getName())) {
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) {

            int x = event.getClickedBlock().getLocation().getBlockX();
            int y = event.getClickedBlock().getLocation().getBlockY();
            int z = event.getClickedBlock().getLocation().getBlockZ();
            String world = player.getWorld().getName();

            if (Main.instance.listeners.contains(player.getUniqueId(), "BLOCK-" + x + " " + y + " " + z + " " + world)) {
                ListenerObject listenerObject = Main.instance.listeners.get(player.getUniqueId(), "BLOCK-" + x + " " + y + " " + z + " " + world);

                Invocable invocable = (Invocable) listenerObject.engine;
                try {
                    invocable.invokeFunction(listenerObject.method, event);
                } catch (ScriptException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
                return;
            }

            if (Main.instance.scriptsManager.contains(x + " " + y + " " + z + " " + world)) {
                ScriptObject scriptObject = Main.instance.scriptsManager.getObject(x + " " + y + " " + z + " " + world);

                try {
                    ScriptEngine engine = Main.instance.scriptsManager.engine;
                    CompiledScript compiledScript = scriptObject.script;

                    // Objects
                    Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
                    bindings.put("player", player);
                    bindings.put("event", event);
                    bindings.put("CR", new ActionDefaults(x + " " + y + " " + z + " " + world, engine));
                    bindings.put("x", x);
                    bindings.put("y", y);
                    bindings.put("z", z);
                    bindings.put("world", world);

                    compiledScript.eval(bindings);
                } catch (ScriptException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        int x = event.getBlock().getLocation().getBlockX();
        int y = event.getBlock().getLocation().getBlockY();
        int z = event.getBlock().getLocation().getBlockZ();
        String world = player.getWorld().getName();
        String blockName = event.getBlock().getType().toString();

        if (Main.instance.listeners.contains(player.getUniqueId(), "BREAK-" + x + " " + y + " " + z + " " + world)) {
            ListenerObject listenerObject = Main.instance.listeners.get(player.getUniqueId(), "BREAK-" + x + " " + y + " " + z + " " + world);

            Invocable invocable = (Invocable) listenerObject.engine;
            try {
                invocable.invokeFunction(listenerObject.method, event);
            } catch (ScriptException | NoSuchMethodException e) {
                e.printStackTrace();
            }
            return;
        }

        if (Main.instance.scriptsManager.contains(x + " " + y + " " + z + " " + world)) {
            ScriptObject scriptObject = Main.instance.scriptsManager.getObject(x + " " + y + " " + z + " " + world);

            try {
                ScriptEngine engine = Main.instance.scriptsManager.engine;
                CompiledScript compiledScript = scriptObject.script;

                // Objects
                Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
                bindings.put("player", player);
                bindings.put("event", event);
                bindings.put("CR", new ActionDefaults(x + " " + y + " " + z + " " + world, engine));
                bindings.put("x", x);
                bindings.put("y", y);
                bindings.put("z", z);
                bindings.put("world", world);

                compiledScript.eval(bindings);
            } catch (ScriptException e) {
                e.printStackTrace();
            }
        }

        if (Main.instance.listeners.contains(player.getUniqueId(), "BREAK-" + blockName)) {
            ListenerObject listenerObject = Main.instance.listeners.get(player.getUniqueId(), "BREAK-" + blockName);

            Invocable invocable = (Invocable) listenerObject.engine;
            try {
                invocable.invokeFunction(listenerObject.method, event);
            } catch (ScriptException | NoSuchMethodException e) {
                e.printStackTrace();
            }
            return;
        }

        if (Main.instance.scriptsManager.contains(blockName)) {
            ScriptObject scriptObject = Main.instance.scriptsManager.getObject(blockName);

            try {
                ScriptEngine engine = Main.instance.scriptsManager.engine;
                CompiledScript compiledScript = scriptObject.script;

                // Objects
                Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
                bindings.put("player", player);
                bindings.put("event", event);
                bindings.put("CR", new ActionDefaults(blockName, engine));
                bindings.put("x", x);
                bindings.put("y", y);
                bindings.put("z", z);
                bindings.put("world", world);

                compiledScript.eval(bindings);
            } catch (ScriptException e) {
                e.printStackTrace();
            }
        }
    }
}
