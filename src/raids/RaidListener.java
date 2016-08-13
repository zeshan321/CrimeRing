package raids;

import com.zeshanaslam.crimering.FileHandler;
import com.zeshanaslam.crimering.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeathRaid(PlayerDeathEvent event) {
        Player player = event.getEntity();

        PartyAPI partyAPI = new PartyAPI();
        PartyObject party = partyAPI.getParty(player);

        if (party == null) {
            if (plugin.raidManager.raids.containsKey(player)) {
                FileHandler fileHandler = new FileHandler("plugins/CrimeRing/raids/" + plugin.raidManager.raids.get(player) + ".yml");
                player.teleport(new Location(Bukkit.getWorld(fileHandler.getString("info.world")), fileHandler.getInteger("info.x"), fileHandler.getInteger("info.y"), fileHandler.getInteger("info.z"), fileHandler.getInteger("info.yaw"), fileHandler.getInteger("info.pitch")));
            }
        } else {
            if (plugin.raidManager.raids.containsKey(party.getOwner())) {
                FileHandler fileHandler = new FileHandler("plugins/CrimeRing/raids/" + plugin.raidManager.raids.get(party.getOwner()) + ".yml");
                player.teleport(new Location(Bukkit.getWorld(fileHandler.getString("info.world")), fileHandler.getInteger("info.x"), fileHandler.getInteger("info.y"), fileHandler.getInteger("info.z"), fileHandler.getInteger("info.yaw"), fileHandler.getInteger("info.pitch")));
            }
        }
    }
}
