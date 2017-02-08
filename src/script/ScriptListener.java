package script;

import com.zeshanaslam.crimering.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class ScriptListener implements Listener {

    private final Main plugin;

    public ScriptListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (plugin.flag.contains(event.getPlayer().getUniqueId().toString() + "-" + "stopmove")) {

            Location from = event.getFrom();
            if (from.getZ() != event.getTo().getZ() && from.getX() != event.getTo().getX()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Clear flags
        Iterator<String> flagIter = plugin.flag.iterator();
        while (flagIter.hasNext()) {
            String flag = flagIter.next();

            if (flag.startsWith("DEATH-" + player.getUniqueId().toString()) || flag.startsWith(player.getUniqueId().toString())) {
                flagIter.remove();
            }
        }

        // Clear listeners
        Iterator<UUID> listenerIter = Main.instance.listeners.rowKeySet().iterator();
        while (listenerIter.hasNext()) {
            if (listenerIter.next() == player.getUniqueId()) {
                listenerIter.remove();
            }
        }

        // Clear tasks
        Iterator iterator = plugin.playerTasks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry pair = (Map.Entry) iterator.next();

            int ID = (int) pair.getKey();
            UUID uuid = (UUID) pair.getValue();

            if (uuid.equals(player.getUniqueId())) {
                Bukkit.getServer().getScheduler().cancelTask(ID);

                iterator.remove();
            }
        }
    }

    // Clear run function later on death
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        Iterator iterator = plugin.playerTasks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry pair = (Map.Entry) iterator.next();

            int ID = (int) pair.getKey();
            UUID uuid = (UUID) pair.getValue();

            if (uuid.equals(player.getUniqueId())) {
                Bukkit.getServer().getScheduler().cancelTask(ID);

                iterator.remove();
            }
        }

        Iterator<String> flagIter = plugin.flag.iterator();
        while (flagIter.hasNext()) {
            String flag = flagIter.next();

            if (flag.startsWith("DEATH-" + player.getUniqueId().toString())) {
                flagIter.remove();
            }
        }
    }

    // Clear run function later on death
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();

        Iterator<String> flagIter = plugin.globalFlags.keySet().iterator();
        while (flagIter.hasNext()) {
            String key = flagIter.next();

            if (key.startsWith("DEATH-" + entity.getUniqueId().toString())) {
                flagIter.remove();
            }
        }
    }

    // Deny using recipe viewer inventories
    @EventHandler(priority = EventPriority.MONITOR)
    public void onClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null || event.getClickedInventory().getTitle() == null) {
            return;
        }

        if (event.getClickedInventory().getTitle().contains("Recipe:")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onTradeClick(InventoryClickEvent event) {
        if (event.getClickedInventory() != null && event.getClickedInventory().getType() == InventoryType.MERCHANT) {
            Player player = (Player) event.getWhoClicked();
            ItemStack item = event.getCurrentItem();

            if (item == null || item.getItemMeta() == null || item.getItemMeta().getDisplayName() == null) {
                return;
            }

            int amount = 0;

            if (item.getTypeId() == 293 && item.getDurability() == 497) {
                for (String lore : item.getItemMeta().getLore()) {
                    lore = ChatColor.stripColor(lore);

                    if (lore.startsWith("Click to receive $")) {
                        amount = Integer.parseInt(lore.replace("Click to receive $", ""));
                        break;
                    }
                }
            }

            if (amount > 0) {

                int finalAmount = amount;
                new BukkitRunnable() {
                    public void run() {
                        ItemStack cursor = player.getItemOnCursor();
                        if (cursor != null && cursor.getTypeId() == 293 && cursor.getDurability() == 497) {
                            player.setItemOnCursor(null);
                            plugin.actionDefaults.addInvBills(player, finalAmount);
                        }

                        for (ItemStack itemStack : player.getInventory().getContents()) {
                            if (itemStack == null) continue;

                            if (itemStack.getTypeId() == 293 && itemStack.getDurability() == 497) {
                                plugin.actionDefaults.addInvBills(player, finalAmount);
                                player.getInventory().remove(itemStack);
                            }
                        }
                    }
                }.runTaskLater(plugin, 5L);
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemDrop().getItemStack();

        if ((item == null) || (item.getItemMeta() == null) || (item.getItemMeta().getDisplayName() == null)) {
            return;
        }

        int amount = 0;

        if (item.getTypeId() == 293 && item.getDurability() == 497) {
            for (String lore : item.getItemMeta().getLore()) {
                lore = ChatColor.stripColor(lore);

                if (lore.startsWith("Click to receive $")) {
                    amount = Integer.parseInt(lore.replace("Click to receive $", ""));
                    break;
                }
            }
        }

        if (amount > 0) {
            plugin.actionDefaults.addInvBills(player, amount);
            event.getItemDrop().remove();
        }
    }
}
