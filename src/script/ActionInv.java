package script;

import com.zeshanaslam.crimering.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import javax.script.*;

public class ActionInv implements Listener {

    private final Main plugin;
    private final String typeInv = "INVENTORY-";

    public ActionInv(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemClick(InventoryClickEvent event) {
        if (event.getInventory().getType() != InventoryType.CHEST) {
            return;
        }

        ItemStack item = event.getCurrentItem();
        if ((item == null) || (item.getItemMeta() == null) || (item.getItemMeta().getDisplayName() == null)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        if (Main.instance.listeners.contains(player.getUniqueId(), typeInv + event.getInventory().getName())) {
            event.setCancelled(true);

            ListenerObject listenerObject = Main.instance.listeners.get(player.getUniqueId(), typeInv + event.getInventory().getName());

            Invocable invocable = (Invocable) listenerObject.engine;
            try {
                invocable.invokeFunction(listenerObject.method, event);
            } catch (ScriptException | NoSuchMethodException e) {
                e.printStackTrace();
            }
            return;
        }

        String invName = event.getInventory().getName().replace("§", "&");
        if (Main.instance.scriptsManager.contains(typeInv + invName)) {
            event.setCancelled(true);

            for (ScriptObject scriptObject : Main.instance.scriptsManager.getObjects(typeInv + invName)) {

                try {
                    ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

                    // Objects
                    Bindings bindings = engine.createBindings();
                    bindings.put("player", player);
                    bindings.put("event", event);
                    bindings.put("CR", new ActionDefaults(typeInv + invName, engine));

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
