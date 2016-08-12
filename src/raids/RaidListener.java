package raids;

import com.zeshanaslam.crimering.Main;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class RaidListener implements Listener {

    private final Main plugin;

    public RaidListener(Main plugin) {
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

        String invName = ChatColor.stripColor(event.getInventory().getName());

        if (!invName.startsWith("Raid: ")) {
            return;
        }
        event.setCancelled(true);

        invName = invName.replace("Raid: ", "");

        if (event.getSlot() == 1) {
            if (Main.instance.raidManager.raids.containsKey(player)) {
                player.sendMessage(ChatColor.RED + "You are already in a queue for a raid!");
                return;
            }

            Main.instance.raidManager.startRaid(player, Main.instance.raidManager.raidnames.get(invName));
        }

        if (event.getSlot() == 6) {
            Main.instance.raidManager.cancelRaid(player);
            player.sendMessage(ChatColor.RED + "Raid has been canceled!");
        }

        if (event.getSlot() == 17) {
            player.closeInventory();
        }
    }
}
