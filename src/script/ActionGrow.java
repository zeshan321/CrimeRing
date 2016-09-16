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
        Block newBlock = event.getNewState().getBlock();

        BlockState oldState = old.getState();
        BlockState newState = event.getNewState();

        String data = typeGrow + oldState.getTypeId() + ":" + oldState.getRawData() + " " + newState.getTypeId() + ":" + newState.getRawData();
        if (Main.instance.scriptsManager.contains(data)) {
            ScriptObject scriptObject = Main.instance.scriptsManager.getObject(data);

            try {
                ScriptEngine engine = Main.instance.scriptsManager.engine;
                CompiledScript compiledScript = scriptObject.script;

                // Objects
                Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
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

                compiledScript.eval(bindings);
            } catch (ScriptException e) {
                e.printStackTrace();
            }
        }
    }
}
