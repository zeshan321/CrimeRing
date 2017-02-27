package script;

import com.zeshanaslam.crimering.Main;
import customevents.PlayerArrestedEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import javax.script.*;

public class ActionArrested implements Listener {

    private final Plugin plugin;
    private final String type = "ARREST";

    public ActionArrested(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onArrest(PlayerArrestedEvent event) {
        Player player = event.getPlayer();

        if (Main.instance.scriptsManager.contains(type)) {
            for (ScriptObject scriptObject : Main.instance.scriptsManager.getObjects(type)) {

                try {
                    ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

                    // Objects
                    Bindings bindings = engine.createBindings();
                    bindings.put("player", player);
                    bindings.put("event", event);
                    bindings.put("cop", event.getCop());
                    bindings.put("CR", new ActionDefaults(type, engine));

                    ScriptContext scriptContext = engine.getContext();
                    scriptContext.setBindings(bindings, scriptContext.ENGINE_SCOPE);

                    engine.eval(scriptObject.scriptData, scriptContext);
                } catch (ScriptException e) {
                    System.out.println("[CR] Scripting error at: " + scriptObject.dir);
                    e.printStackTrace();
                }
            }
        }
    }
}
