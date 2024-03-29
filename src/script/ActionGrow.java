package script;

import com.zeshanaslam.crimering.Main;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;

import javax.script.*;

import static sun.audio.AudioPlayer.player;

public class ActionGrow implements Listener {

    private final Main plugin;
    private final String typeGrow = "GROW-";

    public ActionGrow(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onGrow(BlockGrowEvent event) {
        Block old = event.getBlock();

        BlockState oldState = old.getState();
        BlockState newState = event.getNewState();

        String data = typeGrow + oldState.getTypeId() + ":" + oldState.getRawData() + " " + newState.getTypeId() + ":" + newState.getRawData();
        if (plugin.scriptsManager.contains(data)) {
            for (ScriptObject scriptObject : Main.instance.scriptsManager.getObjects(data)) {

                try {
                    ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

                    // Objects
                    Bindings bindings = engine.createBindings();
                    bindings.put("player", player);
                    bindings.put("event", event);
                    bindings.put("CR", new ActionDefaults(data, engine));
                    bindings.put("oldLocation", oldState.getLocation());
                    bindings.put("newLocation", newState.getLocation());
                    bindings.put("oldType", oldState.getType().toString());
                    bindings.put("newType", newState.getType().toString());
                    bindings.put("oldX", oldState.getLocation().getX());
                    bindings.put("oldY", oldState.getLocation().getY());
                    bindings.put("oldZ", oldState.getLocation().getZ());
                    bindings.put("newX", newState.getLocation().getX());
                    bindings.put("newY", newState.getLocation().getY());
                    bindings.put("newZ", newState.getLocation().getZ());
                    bindings.put("world", newState.getLocation().getWorld().getName());

                    ScriptContext scriptContext = engine.getContext();
                    scriptContext.setBindings(bindings, ScriptContext.ENGINE_SCOPE);

                    engine.eval(scriptObject.scriptData, scriptContext);
                } catch (ScriptException e) {
                    System.out.println("[CR] Scripting error at: " + scriptObject.dir);
                    e.printStackTrace();
                }
            }
        }
    }
}
