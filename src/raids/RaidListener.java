package raids;

import com.shampaggon.crackshot.CSUtility;
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
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import utils.ItemUtils;

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
            if (plugin.raidManager.isInRaid(player)) {
                player.sendMessage(ChatColor.RED + "You are already in a queue for a raid!");
                return;
            }

            PartyAPI partyAPI = new PartyAPI();
            PartyObject partyObject = partyAPI.getParty(player);

            if (partyObject == null) {
                RaidObject raidObject = new RaidObject(plugin.raidManager.raidNames.get(invName));
                raidObject.members.add(player);

                plugin.raidManager.raids.add(raidObject);
                plugin.raidManager.startRaid(raidObject.id, plugin.raidManager.raidNames.get(invName));
            } else {
                RaidObject raidObject = new RaidObject(plugin.raidManager.raidNames.get(invName));
                raidObject.members.addAll(partyObject.getMembers());

                plugin.raidManager.raids.add(raidObject);
                plugin.raidManager.startRaid(raidObject.id, plugin.raidManager.raidNames.get(invName));
            }

        }

        if (event.getSlot() == 6) {
            plugin.raidManager.cancelRaid(player);
            player.sendMessage(ChatColor.RED + "Raid has been canceled!");
        }

        if (event.getSlot() == 17) {
            player.closeInventory();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeathRaid(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        PartyAPI partyAPI = new PartyAPI();
        PartyObject party = partyAPI.getParty(player);

        if (party == null) {
            if (plugin.raidManager.isInRaid(player)) {
                FileHandler fileHandler = new FileHandler("plugins/CrimeRing/raids/" + plugin.raidManager.getRaid(player).raidID + ".yml");
                event.setRespawnLocation(new Location(Bukkit.getWorld(fileHandler.getString("info.world")), fileHandler.getInteger("info.x"), fileHandler.getInteger("info.y"), fileHandler.getInteger("info.z"), fileHandler.getInteger("info.yaw"), fileHandler.getInteger("info.pitch")));
            }
        } else {
            if (plugin.raidManager.isInRaid(player)) {
                FileHandler fileHandler = new FileHandler("plugins/CrimeRing/raids/" + plugin.raidManager.getRaid(player).raidID + ".yml");
                event.setRespawnLocation(new Location(Bukkit.getWorld(fileHandler.getString("info.world")), fileHandler.getInteger("info.x"), fileHandler.getInteger("info.y"), fileHandler.getInteger("info.z"), fileHandler.getInteger("info.yaw"), fileHandler.getInteger("info.pitch")));
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (plugin.raidManager.isInRaid(player)) {
            if (player.getLocation().getWorld().getName().equals("RaidWorld")) {
                return;
            }

            RaidObject raidObject = plugin.raidManager.getRaid(player);

            for (Player players : raidObject.members) {
                raidObject.removeMember(players, true);
            }

            plugin.raidManager.sendMessage(raidObject, ChatColor.RED + "The raid has been canceled because " + player.getName() + " died while in queue!");
        }
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        if (event.getFrom().getName().equals("RaidWorld")) {
            new ItemUtils().clearRaidItems(player);

            if (plugin.actionDefaults.hasLootbag(player)) {
                plugin.actionDefaults.removeLootbag(player);
            }

            plugin.actionDefaults.removeObjective(player);
        }
    }

    // Loot bag pickup
    @EventHandler
    public void onPickup(PlayerPickupItemEvent event) {
        ItemStack itemStack = event.getItem().getItemStack();
        Player player = event.getPlayer();

        if (itemStack.getTypeId() == 293 && itemStack.getDurability() == 497) {
            if (plugin.actionDefaults.hasLootbag(player)) {
                event.setCancelled(true);
            } else {
                event.setCancelled(true);

                ItemStack cursor = player.getItemOnCursor();
                if (cursor != null) {
                    if (cursor.getTypeId() == 293 && cursor.getDurability() == 497) {
                        return;
                    }
                }

                event.getItem().remove();
                plugin.actionDefaults.giveLootbag(player);
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        if (!plugin.actionDefaults.hasLootbag(player)) {
            plugin.actionDefaults.removePotionEffect(player, "SLOW");
        }
    }

    @EventHandler
    public void onCLickLoot(InventoryClickEvent event) {
        if (event.getClickedInventory() != null && event.getClickedInventory().getName() != null && event.getClickedInventory().getName().endsWith("'s body")) {
            Player player = (Player) event.getWhoClicked();
            ItemStack item = event.getCurrentItem();

            if (item == null) {
                return;
            }

            if (item.getTypeId() == 293 && item.getDurability() == 497) {
                if (item.getItemMeta() != null && item.getItemMeta().getDisplayName() != null && item.getItemMeta().hasLore()) {
                    for (String lore : item.getItemMeta().getLore()) {
                        lore = ChatColor.stripColor(lore);

                        if (lore.startsWith("Click to receive $")) {
                            return;
                        }
                    }

                    if (plugin.actionDefaults.hasLootbag(player)) {
                        event.setCancelled(true);
                        player.sendMessage(ChatColor.RED + "You can only carry one loot bag at a time!");
                    } else {
                        plugin.actionDefaults.giveLootbag(player);
                        player.setItemOnCursor(null);
                    }
                } else {
                    if (plugin.actionDefaults.hasLootbag(player)) {
                        event.setCancelled(true);
                        player.sendMessage(ChatColor.RED + "You can only carry one loot bag at a time!");
                    } else {
                        event.setCancelled(true);
                        event.getClickedInventory().setItem(event.getSlot(), null);
                        plugin.actionDefaults.giveLootbag(player);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onTeleportOut(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        if (event.getFrom().getName().equalsIgnoreCase("raidworld")) {
            RaidObject raidObject = plugin.raidManager.getRaid(player);

            if (raidObject != null) {
                raidObject.removeMember(player);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        RaidObject raidObject = plugin.raidManager.getRaid(player);

        if (raidObject != null) {
            raidObject.removeMember(player);
        }
    }
}