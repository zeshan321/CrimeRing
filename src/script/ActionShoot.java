package script;

import com.shampaggon.crackshot.events.WeaponShootEvent;
import com.zeshanaslam.crimering.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import javax.script.*;

public class ActionShoot implements Listener {

    private final Main plugin;
    private final String type = "CRACKSHOT-";

    public ActionShoot(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onShoot(WeaponShootEvent event) {
        Player player = event.getPlayer();

        for (ScriptObject scriptObject : Main.instance.scriptsManager.getObjects(type + "*")) {

            try {
                ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

                // Objects
                Bindings bindings = engine.createBindings();
                bindings.put("player", player);
                bindings.put("event", event);
                bindings.put("CR", new ActionDefaults(type + "*", engine));

                ScriptContext scriptContext = engine.getContext();
                scriptContext.setBindings(bindings, ScriptContext.ENGINE_SCOPE);

                engine.eval(scriptObject.scriptData, scriptContext);
            } catch (ScriptException e) {
                System.out.println("[CR] Scripting error at: " + scriptObject.dir);
                e.printStackTrace();
            }
        }

        plugin.actionDefaults.getApplicableRegions(player).stream().filter(region -> Main.instance.scriptsManager.contains(type + region)).forEach(region -> {
            for (ScriptObject scriptObject : Main.instance.scriptsManager.getObjects(type + region)) {

                try {
                    ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

                    // Objects
                    Bindings bindings = engine.createBindings();
                    bindings.put("player", player);
                    bindings.put("event", event);
                    bindings.put("CR", new ActionDefaults(type + region, engine));

                    ScriptContext scriptContext = engine.getContext();
                    scriptContext.setBindings(bindings, ScriptContext.ENGINE_SCOPE);

                    engine.eval(scriptObject.scriptData, scriptContext);
                } catch (ScriptException e) {
                    System.out.println("[CR] Scripting error at: " + scriptObject.dir);
                    e.printStackTrace();
                }
            }
        });
    }
}
