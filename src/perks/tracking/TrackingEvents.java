package perks.tracking;

import com.zeshanaslam.crimering.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.UUID;

public class TrackingEvents implements Listener {

    private final Main plugin;
    private final int cooldownTime = 1200;
    private final String permission = "CR.perk.tracking";

    public TrackingEvents(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        // Only use right hand
        if (event.getHand() == EquipmentSlot.OFF_HAND) {
            return;
        }

        Player player = event.getPlayer();
        if (event.getItem() == null) {
            return;
        }

        ItemStack itemStack = event.getItem();
        if (itemStack.getType() == Material.COMPASS && plugin.cooldownManager.perksUtil.hasPermission(player, permission)) {
            ArrayList<ItemStack> list = new ArrayList<>();
            list.add(plugin.actionDefaults.createItemStackWithMeta(166, 1, 0, ChatColor.AQUA + "Cancel", ChatColor.GRAY + "Click to cancel tracking."));

            for (Player players : Bukkit.getOnlinePlayers()) {
                if (player.getUniqueId() == players.getUniqueId()) continue;

                if (players.getWorld().getName().equalsIgnoreCase("world")) {
                    ItemStack head = plugin.actionDefaults.getPlayerHead(players.getName());
                    head = plugin.actionDefaults.setItemStackMeta(head, ChatColor.AQUA + players.getName(), ChatColor.GRAY + "Click to track " + ChatColor.RED + players.getName() + ChatColor.GRAY + ".");

                    list.add(head);
                }
            }

            plugin.actionDefaults.openPageInv(player, ChatColor.RED + "Tracking", list);
        }
    }

    @EventHandler
    public void onItemClick(InventoryClickEvent event) {
        if (event.getClickedInventory() != null && event.getClickedInventory().getName() != null && ChatColor.stripColor(event.getClickedInventory().getName()).startsWith("Tracking")) {
            ItemStack item = event.getCurrentItem();
            if ((item == null) || (item.getItemMeta() == null) || (item.getItemMeta().getDisplayName() == null)) {
                return;
            }

            Player player = (Player) event.getWhoClicked();
            player.closeInventory();

            if (item.getTypeId() == 166) {
                if (plugin.trackingManager.tracking.containsKey(player.getUniqueId())) {
                    UUID uuid = plugin.trackingManager.tracking.get(player.getUniqueId()).tracking;
                    if (Bukkit.getPlayer(uuid) != null) {
                        Player tracking = Bukkit.getPlayer(uuid);
                        if (tracking.isOnline()) {
                            plugin.entityGlowHelper.removeGlow(tracking);
                        }
                    }

                    player.sendMessage(ChatColor.GRAY + "No longer tracking " + ChatColor.RED + plugin.trackingManager.tracking.get(player.getUniqueId()).name + ChatColor.GRAY + ".");
                    plugin.trackingManager.tracking.remove(player.getUniqueId());
                    player.setCompassTarget(new Location(player.getWorld(), -295, 63, -77, (float) 155.504, (float) 0.75));
                } else {
                    player.sendMessage(ChatColor.GRAY + "You are not currently tracking anyone!");
                }
                return;
            }

            if (plugin.trackingManager.tracking.containsKey(player.getUniqueId())) {
                player.sendMessage(ChatColor.GRAY + "You are currently tracking " + ChatColor.RED + plugin.trackingManager.tracking.get(player.getUniqueId()).name + ChatColor.GRAY + "!");
                return;
            }

            if (plugin.cooldownManager.isInCooldown(player, permission) == 0) {
                plugin.cooldownManager.addCooldown(player, permission, cooldownTime);

                Player tracking = Bukkit.getPlayer(ChatColor.stripColor(item.getItemMeta().getDisplayName()));
                plugin.trackingManager.tracking.put(player.getUniqueId(), new TrackObject(tracking.getUniqueId(), tracking.getName(), tracking.getLocation()));
                plugin.entityGlowHelper.addGlow(player, tracking);

                player.sendMessage(ChatColor.GRAY + "Now tracking " + ChatColor.RED + plugin.trackingManager.tracking.get(player.getUniqueId()).name + ChatColor.GRAY + ".");

                int base = 250;
                if (Main.instance.cooldownManager.perksUtil.hasPermission(player, "CR.perk.tracking.1")) {
                    base = 225;
                }

                if (Main.instance.cooldownManager.perksUtil.hasPermission(player, "CR.perk.tracking.2")) {
                    base = 200;
                }

                if (Main.instance.cooldownManager.perksUtil.hasPermission(player, "CR.perk.tracking.3")) {
                    base = 175;
                }

                player.setCompassTarget(Main.instance.actionDefaults.getRandomLocations(tracking.getLocation(), base, 1, true).get(0));
            } else {
                long seconds = plugin.cooldownManager.isInCooldown(player, permission);
                player.sendMessage(ChatColor.GRAY + "You can use tracking again in " + ChatColor.RED + seconds + ChatColor.GRAY + " seconds!");
            }
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        plugin.trackingManager.tracking.remove(player.getUniqueId());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        player.setCompassTarget(new Location(player.getWorld(), -295, 63, -77, (float) 155.504, (float) 0.75));
    }
}
