package script;

import com.zeshanaslam.crimering.Main;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

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

            if (Main.instance.scriptsManager.contains(name)) {
                ScriptObject scriptObject = Main.instance.scriptsManager.getObject(name);

                try {
                    ScriptEngineManager factory = new ScriptEngineManager();
                    ScriptEngine engine = factory.getEngineByName("JavaScript");
                    Compilable compilableEngine = (Compilable) engine;
                    CompiledScript compiledScript = compilableEngine.compile(scriptObject.script);

                    // Objects
                    Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
                    bindings.put("player", player);
                    bindings.put("event", event);
                    bindings.put("CR", new ActionDefaults());
                    bindings.put("mobName", name);
                    bindings.put("mobLocation", event.getEntity().getLocation());

                    compiledScript.eval(bindings);
                } catch (ScriptException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
