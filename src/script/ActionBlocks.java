package script;

import com.zeshanaslam.crimering.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import javax.script.*;

public class ActionBlocks implements Listener {

    private final Main plugin;

    public ActionBlocks(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // Only use right hand
        if (event.getHand() == EquipmentSlot.OFF_HAND) {
            return;
        }

        // Check if player is using action copy
        if (ActionCommands.clicks.containsKey(player.getName())) {
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) {

            int x = event.getClickedBlock().getLocation().getBlockX();
            int y = event.getClickedBlock().getLocation().getBlockY();
            int z = event.getClickedBlock().getLocation().getBlockZ();
            String world = player.getWorld().getName();

            if (Main.instance.scriptsManager.contains(x + " " + y + " " + z + " " + world)) {
                ScriptObject scriptObject = Main.instance.scriptsManager.getObject(x + " " + y + " " + z + " " + world);

                try {
                    ScriptEngineManager factory = new ScriptEngineManager();
                    ScriptEngine engine = factory.getEngineByName("JavaScript");
                    CompiledScript compiledScript = scriptObject.script;

                    // Objects
                    Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
                    bindings.put("player", player);
                    bindings.put("event", event);
                    bindings.put("CR", new ActionDefaults());

                    compiledScript.eval(bindings);
                } catch (ScriptException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
