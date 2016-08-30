package script;

import com.zeshanaslam.crimering.Main;
import events.PlayerEquipEvent;
import events.PlayerUnequipEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import javax.script.Invocable;
import javax.script.ScriptException;

public class ActionEquip implements Listener {

    private final Plugin plugin;

    public ActionEquip(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEquip(PlayerEquipEvent event) {
        Player player = event.getPlayer();

        if (Main.instance.listeners.contains(player.getUniqueId(), "EQUIP-" + player.getUniqueId().toString())) {
            ListenerObject listenerObject = Main.instance.listeners.get(player.getUniqueId(), "EQUIP-" + player.getUniqueId().toString());

            Invocable invocable = (Invocable) listenerObject.engine;
            try {
                invocable.invokeFunction(listenerObject.method, event);
            } catch (ScriptException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    @EventHandler
    public void onUnequip(PlayerUnequipEvent event) {
        Player player = event.getPlayer();

        if (Main.instance.listeners.contains(player.getUniqueId(), "UNEQUIP-" + player.getUniqueId().toString())) {
            ListenerObject listenerObject = Main.instance.listeners.get(player.getUniqueId(), "UNEQUIP-" + player.getUniqueId().toString());

            Invocable invocable = (Invocable) listenerObject.engine;
            try {
                invocable.invokeFunction(listenerObject.method, event);
            } catch (ScriptException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }
}
