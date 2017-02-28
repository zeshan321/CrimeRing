package script;

import com.zeshanaslam.crimering.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import javax.script.*;

public class ActionDeath implements Listener {

    private final Main plugin;
    private final String typeEntity = "DEATH_ENTITY-";
    private final String typePlayer = "DEATH_PLAYER-";

    public ActionDeath(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMobDeath(EntityDeathEvent event) {
        if (!(event.getEntity().getKiller() instanceof Player)) {
            return;
        }

        Player player = event.getEntity().getKiller();

        if (event.getEntity().getCustomName() == null) {
            if (Main.instance.listeners.contains(player.getUniqueId(), typeEntity + event.getEntity().getType().toString())) {
                ListenerObject listenerObject = Main.instance.listeners.get(player.getUniqueId(), typeEntity + event.getEntity().getType().toString());

                Invocable invocable = (Invocable) listenerObject.engine;
                try {
                    invocable.invokeFunction(listenerObject.method, event);
                } catch (ScriptException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
                return;
            }

            if (Main.instance.scriptsManager.contains(typeEntity + event.getEntity().getType().toString())) {
                for (ScriptObject scriptObject : Main.instance.scriptsManager.getObjects(typeEntity + event.getEntity().getType().toString())) {

                    try {
                        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

                        // Objects
                        Bindings bindings = engine.createBindings();
                        bindings.put("player", player);
                        bindings.put("event", event);
                        bindings.put("CR", new ActionDefaults(typeEntity + event.getEntity().getType().toString(), engine));
                        bindings.put("mobName", event.getEntity().getCustomName());
                        bindings.put("X", event.getEntity().getLocation().getX());
                        bindings.put("Y", event.getEntity().getLocation().getY());
                        bindings.put("Z", event.getEntity().getLocation().getZ());
                        bindings.put("world", event.getEntity().getLocation().getWorld().getName());

                        ScriptContext scriptContext = engine.getContext();
                        scriptContext.setBindings(bindings, ScriptContext.ENGINE_SCOPE);

                        engine.eval(scriptObject.scriptData, scriptContext);
                    } catch (ScriptException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            if (Main.instance.listeners.contains(player.getUniqueId(), typeEntity + event.getEntity().getCustomName())) {
                ListenerObject listenerObject = Main.instance.listeners.get(player.getUniqueId(), typeEntity + event.getEntity().getCustomName());

                Invocable invocable = (Invocable) listenerObject.engine;
                try {
                    invocable.invokeFunction(listenerObject.method, event);
                } catch (ScriptException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
                return;
            }

            if (Main.instance.scriptsManager.contains(typeEntity + event.getEntity().getCustomName())) {
                for (ScriptObject scriptObject : Main.instance.scriptsManager.getObjects(typeEntity + event.getEntity().getCustomName())) {
                    try {
                        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

                        // Objects
                        Bindings bindings = engine.createBindings();
                        bindings.put("player", player);
                        bindings.put("event", event);
                        bindings.put("CR", new ActionDefaults(typeEntity + event.getEntity().getCustomName(), engine));
                        bindings.put("mobName", event.getEntity().getCustomName());
                        bindings.put("X", event.getEntity().getLocation().getX());
                        bindings.put("Y", event.getEntity().getLocation().getY());
                        bindings.put("Z", event.getEntity().getLocation().getZ());
                        bindings.put("world", event.getEntity().getLocation().getWorld().getName());

                        ScriptContext scriptContext = engine.getContext();
                        scriptContext.setBindings(bindings, ScriptContext.ENGINE_SCOPE);

                        engine.eval(scriptObject.scriptData, scriptContext);
                    } catch (ScriptException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = event.getEntity();

            if (Main.instance.listeners.contains(player.getUniqueId(), typePlayer + player.getName())) {
                ListenerObject listenerObject = Main.instance.listeners.get(player.getUniqueId(), typePlayer + player.getName());

                Invocable invocable = (Invocable) listenerObject.engine;
                try {
                    invocable.invokeFunction(listenerObject.method, event);
                } catch (ScriptException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
                return;
            }

            if (Main.instance.scriptsManager.contains(typePlayer + player.getName())) {
                for (ScriptObject scriptObject : Main.instance.scriptsManager.getObjects(typePlayer + player.getName())) {

                    try {
                        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

                        // Objects
                        Bindings bindings = engine.createBindings();
                        bindings.put("player", player);
                        bindings.put("event", event);
                        bindings.put("CR", new ActionDefaults(typePlayer + player.getName(), engine));
                        bindings.put("killer", player.getKiller());
                        bindings.put("X", player.getLocation().getX());
                        bindings.put("Y", player.getLocation().getY());
                        bindings.put("Z", player.getLocation().getZ());
                        bindings.put("world", player.getLocation().getWorld().getName());

                        ScriptContext scriptContext = engine.getContext();
                        scriptContext.setBindings(bindings, ScriptContext.ENGINE_SCOPE);

                        engine.eval(scriptObject.scriptData, scriptContext);
                    } catch (ScriptException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
