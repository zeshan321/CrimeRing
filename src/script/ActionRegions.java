package script;

import com.mewin.WGRegionEvents.events.RegionEnterEvent;
import com.mewin.WGRegionEvents.events.RegionLeaveEvent;
import com.zeshanaslam.crimering.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import javax.script.*;

public class ActionRegions implements Listener {

    private final Main plugin;
    private final String typeEnter = "REGION_ENTER-";
    private final String typeLeave = "REGION_LEAVE-";

    public ActionRegions(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEnter(RegionEnterEvent event) {
        Player player = event.getPlayer();

        if (Main.instance.listeners.contains(player.getUniqueId(), typeEnter + event.getRegion().getId())) {
            ListenerObject listenerObject = Main.instance.listeners.get(player.getUniqueId(), typeEnter + event.getRegion().getId());

            Invocable invocable = (Invocable) listenerObject.engine;
            try {
                invocable.invokeFunction(listenerObject.method, event);
            } catch (ScriptException | NoSuchMethodException e) {
                e.printStackTrace();
            }
            return;
        }

        if (Main.instance.scriptsManager.contains(typeEnter + event.getRegion().getId())) {
            ScriptObject scriptObject = Main.instance.scriptsManager.getObject(typeEnter + event.getRegion().getId());

            try {
                ScriptEngine engine = Main.instance.scriptsManager.engine;
                CompiledScript compiledScript = scriptObject.script;

                // Objects
                Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
                bindings.put("player", player);
                bindings.put("event", event);
                bindings.put("regionType", "enter");
                bindings.put("CR", new ActionDefaults(typeEnter + event.getRegion().getId(), engine));

                compiledScript.eval(bindings);
            } catch (ScriptException e) {
                e.printStackTrace();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLeave(RegionLeaveEvent event) {
        Player player = event.getPlayer();

        if (Main.instance.listeners.contains(player.getUniqueId(), typeLeave + event.getRegion().getId())) {
            ListenerObject listenerObject = Main.instance.listeners.get(player.getUniqueId(), typeLeave + event.getRegion().getId());

            Invocable invocable = (Invocable) listenerObject.engine;
            try {
                invocable.invokeFunction(listenerObject.method, event);
            } catch (ScriptException | NoSuchMethodException e) {
                e.printStackTrace();
            }
            return;
        }

        if (Main.instance.scriptsManager.contains(typeLeave + event.getRegion().getId())) {
            ScriptObject scriptObject = Main.instance.scriptsManager.getObject(typeLeave + event.getRegion().getId());

            try {
                ScriptEngine engine = Main.instance.scriptsManager.engine;
                CompiledScript compiledScript = scriptObject.script;

                // Objects
                Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
                bindings.put("player", player);
                bindings.put("event", event);
                bindings.put("regionType", "leave");
                bindings.put("CR", new ActionDefaults(typeLeave + event.getRegion().getId(), engine));

                compiledScript.eval(bindings);
            } catch (ScriptException e) {
                e.printStackTrace();
            }
        }
    }
}
