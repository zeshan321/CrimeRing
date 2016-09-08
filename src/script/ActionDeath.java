package script;

import com.zeshanaslam.crimering.Main;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import javax.script.*;

public class ActionDeath implements Listener {

    private final Main plugin;

    public ActionDeath(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(EntityDeathEvent event) {
        if (event.getEntity().getCustomName() == null) {
            return;
        }

        if (event.getEntity().getKiller() instanceof Player) {
            String name = ChatColor.stripColor(event.getEntity().getCustomName());
            Player player = event.getEntity().getKiller();

            if (Main.instance.listeners.contains(player.getUniqueId(), "DEATH_ENTITY-" + event.getEntity().getCustomName())) {
                ListenerObject listenerObject = Main.instance.listeners.get(player.getUniqueId(), "DEATH_ENTITY-" + event.getEntity().getCustomName());

                Invocable invocable = (Invocable) listenerObject.engine;
                try {
                    invocable.invokeFunction(listenerObject.method, event);
                } catch (ScriptException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
                return;
            }

            if (Main.instance.scriptsManager.contains(name)) {
                ScriptObject scriptObject = Main.instance.scriptsManager.getObject(name);

                try {
                    ScriptEngine engine = Main.instance.scriptsManager.engine;
                    CompiledScript compiledScript = scriptObject.script;

                    // Objects
                    Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
                    bindings.put("player", player);
                    bindings.put("event", event);
                    bindings.put("CR", new ActionDefaults(name, engine));
                    bindings.put("mobName", name);
                    bindings.put("mobLocation", event.getEntity().getLocation());

                    compiledScript.eval(bindings);
                } catch (ScriptException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMobDeath(EntityDeathEvent event) {
        if (!(event.getEntity().getKiller() instanceof Player)) {
            return;
        }

        Player player = event.getEntity().getKiller();

        if (event.getEntity().getCustomName() == null) {
            if (Main.instance.listeners.contains(player.getUniqueId(), "DEATH_ENTITY-" + event.getEntity().getType().toString())) {
                ListenerObject listenerObject = Main.instance.listeners.get(player.getUniqueId(), "DEATH_ENTITY-" + event.getEntity().getType().toString());

                Invocable invocable = (Invocable) listenerObject.engine;
                try {
                    invocable.invokeFunction(listenerObject.method, event);
                } catch (ScriptException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
                return;
            }

            if (Main.instance.scriptsManager.contains(event.getEntity().getType().toString())) {
                ScriptObject scriptObject = Main.instance.scriptsManager.getObject(event.getEntity().getType().toString());

                try {
                    ScriptEngine engine = Main.instance.scriptsManager.engine;
                    CompiledScript compiledScript = scriptObject.script;

                    // Objects
                    Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
                    bindings.put("player", player);
                    bindings.put("event", event);
                    bindings.put("CR", new ActionDefaults(event.getEntity().getType().toString(), engine));
                    bindings.put("mobName", event.getEntity().getCustomName());
                    bindings.put("X", event.getEntity().getLocation().getX());
                    bindings.put("Y", event.getEntity().getLocation().getY());
                    bindings.put("Z", event.getEntity().getLocation().getZ());
                    bindings.put("world", event.getEntity().getLocation().getWorld().getName());

                    compiledScript.eval(bindings);
                } catch (ScriptException e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (Main.instance.listeners.contains(player.getUniqueId(), "DEATH_ENTITY-" + event.getEntity().getCustomName())) {
                ListenerObject listenerObject = Main.instance.listeners.get(player.getUniqueId(), "DEATH_ENTITY-" + event.getEntity().getCustomName());

                Invocable invocable = (Invocable) listenerObject.engine;
                try {
                    invocable.invokeFunction(listenerObject.method, event);
                } catch (ScriptException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
                return;
            }

            if (Main.instance.scriptsManager.contains(event.getEntity().getCustomName())) {
                ScriptObject scriptObject = Main.instance.scriptsManager.getObject(event.getEntity().getCustomName());

                try {
                    ScriptEngine engine = Main.instance.scriptsManager.engine;
                    CompiledScript compiledScript = scriptObject.script;

                    // Objects
                    Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
                    bindings.put("player", player);
                    bindings.put("event", event);
                    bindings.put("CR", new ActionDefaults(event.getEntity().getCustomName(), engine));
                    bindings.put("mobName", event.getEntity().getCustomName());
                    bindings.put("X", event.getEntity().getLocation().getX());
                    bindings.put("Y", event.getEntity().getLocation().getY());
                    bindings.put("Z", event.getEntity().getLocation().getZ());
                    bindings.put("world", event.getEntity().getLocation().getWorld().getName());

                    compiledScript.eval(bindings);
                } catch (ScriptException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = event.getEntity();

            if (Main.instance.listeners.contains(player.getUniqueId(), "DEATH_PLAYER-" + player.getName())) {
                ListenerObject listenerObject = Main.instance.listeners.get(player.getUniqueId(), "DEATH_PLAYER-" + player.getName());

                Invocable invocable = (Invocable) listenerObject.engine;
                try {
                    invocable.invokeFunction(listenerObject.method, event);
                } catch (ScriptException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
                return;
            }

            if (Main.instance.scriptsManager.contains(player.getName())) {
                ScriptObject scriptObject = Main.instance.scriptsManager.getObject(player.getName());

                try {
                    ScriptEngine engine = Main.instance.scriptsManager.engine;
                    CompiledScript compiledScript = scriptObject.script;

                    // Objects
                    Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
                    bindings.put("player", player);
                    bindings.put("event", event);
                    bindings.put("CR", new ActionDefaults(player.getName(), engine));
                    bindings.put("killer", player.getKiller());
                    bindings.put("X", player.getLocation().getX());
                    bindings.put("Y", player.getLocation().getY());
                    bindings.put("Z", player.getLocation().getZ());
                    bindings.put("world", player.getLocation().getWorld().getName());

                    compiledScript.eval(bindings);
                } catch (ScriptException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
