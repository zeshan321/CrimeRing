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

    public ActionRegions(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEnter(RegionEnterEvent event) {
        Player player = event.getPlayer();

        if (Main.instance.scriptsManager.contains(event.getRegion().getId())) {
            ScriptObject scriptObject = Main.instance.scriptsManager.getObject(event.getRegion().getId());

            try {
                ScriptEngineManager factory = new ScriptEngineManager();
                ScriptEngine engine = factory.getEngineByName("JavaScript");
                CompiledScript compiledScript = scriptObject.script;

                // Objects
                Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
                bindings.put("player", player);
                bindings.put("event", event);
                bindings.put("regionType", "enter");
                bindings.put("CR", new ActionDefaults());

                compiledScript.eval(bindings);
            } catch (ScriptException e) {
                e.printStackTrace();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLeave(RegionLeaveEvent event) {
        Player player = event.getPlayer();

        if (Main.instance.scriptsManager.contains(event.getRegion().getId())) {
            ScriptObject scriptObject = Main.instance.scriptsManager.getObject(event.getRegion().getId());

            try {
                ScriptEngineManager factory = new ScriptEngineManager();
                ScriptEngine engine = factory.getEngineByName("JavaScript");
                CompiledScript compiledScript = scriptObject.script;

                // Objects
                Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
                bindings.put("player", player);
                bindings.put("event", event);
                bindings.put("regionType", "leave");
                bindings.put("CR", new ActionDefaults());

                compiledScript.eval(bindings);
            } catch (ScriptException e) {
                e.printStackTrace();
            }
        }
    }
}
