package script;

import com.zeshanaslam.crimering.Main;
import customevents.LockpickEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import javax.script.*;

public class ActionLockpick implements Listener {

    private final Main plugin;
    private final String type = "LOCKPICK-";

    public ActionLockpick(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onTrade(LockpickEvent event) {
        Player player = event.getPlayer();

        if (Main.instance.listeners.contains(player.getUniqueId(), type + event.getLockType())) {
            ListenerObject listenerObject = Main.instance.listeners.get(player.getUniqueId(), type + event.getLockType());

            Invocable invocable = (Invocable) listenerObject.engine;
            try {
                invocable.invokeFunction(listenerObject.method, event);
            } catch (ScriptException | NoSuchMethodException e) {
                e.printStackTrace();
            }

            return;
        }

        if (Main.instance.scriptsManager.contains(type + event.getLockType())) {
            for (ScriptObject scriptObject : Main.instance.scriptsManager.getObjects(type + event.getLockType())) {

                try {
                    ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

                    // Objects
                    Bindings bindings = engine.createBindings();
                    bindings.put("player", player);
                    bindings.put("event", event);
                    bindings.put("CR", new ActionDefaults(type + event.getLockType(), engine));

                    ScriptContext scriptContext = engine.getContext();
                    scriptContext.setBindings(bindings, scriptContext.ENGINE_SCOPE);

                    engine.eval(scriptObject.scriptData, scriptContext);
                } catch (ScriptException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
