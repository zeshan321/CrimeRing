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
    private final String typeBlock = "BLOCK-";
    private final String typeBreak = "BREAK-";

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

            // Check if block is locked
            if (Main.instance.lockManager.locks.containsKey(x + " " + y + " " + z + " " + world)) {
                String lockType = Main.instance.lockManager.locks.get(x + " " + y + " " + z + " " + world);

                if (!plugin.lockManager.unlocked.containsKey(lockType + " " + x + " " + y + " " + z + " " + world)) {
                    return;
                }
            }

            if (Main.instance.listeners.contains(player.getUniqueId(), typeBlock + x + " " + y + " " + z + " " + world)) {
                ListenerObject listenerObject = Main.instance.listeners.get(player.getUniqueId(), typeBlock + x + " " + y + " " + z + " " + world);

                Invocable invocable = (Invocable) listenerObject.engine;
                try {
                    invocable.invokeFunction(listenerObject.method, event);
                } catch (ScriptException | NoSuchMethodException e) {
                    e.printStackTrace();
                }

                return;
            }

            if (Main.instance.scriptsManager.contains(typeBlock + x + " " + y + " " + z + " " + world)) {
                ScriptObject scriptObject = Main.instance.scriptsManager.getObject(typeBlock + x + " " + y + " " + z + " " + world);

                try {
                    ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

                    // Objects
                    Bindings bindings = engine.createBindings();

                    bindings.put("player", player);
                    bindings.put("event", event);
                    bindings.put("CR", new ActionDefaults(typeBlock + x + " " + y + " " + z + " " + world, engine));
                    bindings.put("x", x);
                    bindings.put("y", y);
                    bindings.put("z", z);
                    bindings.put("world", world);
                    bindings.put("blockLocation", event.getClickedBlock().getLocation());

                    ScriptContext scriptContext = engine.getContext();
                    scriptContext.setBindings(bindings, scriptContext.ENGINE_SCOPE);

                    engine.eval(scriptObject.scriptData, scriptContext);
                } catch (ScriptException e) {
                    e.printStackTrace();
                }

                return;
            }

            String blockData = event.getClickedBlock().getTypeId() + ":" + event.getClickedBlock().getData();
            if (Main.instance.listeners.contains(player.getUniqueId(), typeBlock + blockData)) {
                ListenerObject listenerObject = Main.instance.listeners.get(player.getUniqueId(), typeBlock + blockData);

                Invocable invocable = (Invocable) listenerObject.engine;
                try {
                    invocable.invokeFunction(listenerObject.method, event);
                } catch (ScriptException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
                return;
            }

            if (Main.instance.scriptsManager.contains(typeBlock + blockData)) {
                ScriptObject scriptObject = Main.instance.scriptsManager.getObject(typeBlock + blockData);

                try {
                    ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

                    // Objects
                    Bindings bindings = engine.createBindings();
                    bindings.put("player", player);
                    bindings.put("event", event);
                    bindings.put("CR", new ActionDefaults(typeBlock + blockData, engine));
                    bindings.put("x", x);
                    bindings.put("y", y);
                    bindings.put("z", z);
                    bindings.put("world", world);
                    bindings.put("blockLocation", event.getClickedBlock().getLocation());

                    ScriptContext scriptContext = engine.getContext();
                    scriptContext.setBindings(bindings, scriptContext.ENGINE_SCOPE);

                    engine.eval(scriptObject.scriptData, scriptContext);
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

        if (Main.instance.listeners.contains(player.getUniqueId(), typeBreak + x + " " + y + " " + z + " " + world)) {
            ListenerObject listenerObject = Main.instance.listeners.get(player.getUniqueId(), typeBreak + x + " " + y + " " + z + " " + world);

            Invocable invocable = (Invocable) listenerObject.engine;
            try {
                invocable.invokeFunction(listenerObject.method, event);
            } catch (ScriptException | NoSuchMethodException e) {
                e.printStackTrace();
            }
            return;
        }

        if (Main.instance.scriptsManager.contains(typeBreak + x + " " + y + " " + z + " " + world)) {
            ScriptObject scriptObject = Main.instance.scriptsManager.getObject(typeBreak + x + " " + y + " " + z + " " + world);

            try {
                ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

                // Objects
                Bindings bindings = engine.createBindings();
                bindings.put("player", player);
                bindings.put("event", event);
                bindings.put("CR", new ActionDefaults(typeBreak + x + " " + y + " " + z + " " + world, engine));
                bindings.put("x", x);
                bindings.put("y", y);
                bindings.put("z", z);
                bindings.put("world", world);
                bindings.put("blockLocation", event.getBlock().getLocation());

                ScriptContext scriptContext = engine.getContext();
                scriptContext.setBindings(bindings, scriptContext.ENGINE_SCOPE);

                engine.eval(scriptObject.scriptData, scriptContext);
            } catch (ScriptException e) {
                e.printStackTrace();
            }

            return;
        }

        String blockData = event.getBlock().getTypeId() + ":" + event.getBlock().getData();
        if (Main.instance.listeners.contains(player.getUniqueId(), typeBreak + blockData)) {
            ListenerObject listenerObject = Main.instance.listeners.get(player.getUniqueId(), typeBreak + blockData);

            Invocable invocable = (Invocable) listenerObject.engine;
            try {
                invocable.invokeFunction(listenerObject.method, event);
            } catch (ScriptException | NoSuchMethodException e) {
                e.printStackTrace();
            }
            return;
        }

        if (Main.instance.scriptsManager.contains(typeBreak + blockData)) {
            ScriptObject scriptObject = Main.instance.scriptsManager.getObject(typeBreak + blockData);

            try {
                ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

                // Objects
                Bindings bindings = engine.createBindings();
                bindings.put("player", player);
                bindings.put("event", event);
                bindings.put("CR", new ActionDefaults(typeBreak + blockData, engine));
                bindings.put("x", x);
                bindings.put("y", y);
                bindings.put("z", z);
                bindings.put("world", world);
                bindings.put("blockLocation", event.getBlock().getLocation());

                ScriptContext scriptContext = engine.getContext();
                scriptContext.setBindings(bindings, scriptContext.ENGINE_SCOPE);

                engine.eval(scriptObject.scriptData, scriptContext);
            } catch (ScriptException e) {
                e.printStackTrace();
            }
        }
    }
}
