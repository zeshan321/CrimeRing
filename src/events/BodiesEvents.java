package events;

import com.zeshanaslam.crimering.Main;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import utils.HiddenStringUtils;

import java.util.HashMap;

public class BodiesEvents implements Listener {

    public static HashMap<Location, BodyObject> bodies = new HashMap<>();
    private final Main plugin;
    private HashMap<Player, Location> temp = new HashMap<>();

    public BodiesEvents(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity().getPlayer();
        Entity killer = event.getEntity().getKiller();

        ItemStack[] items = event.getDrops().toArray(new ItemStack[0]);

        boolean empty = true;
        for (ItemStack item : items) {
            if (item != null && item.getAmount() >= 1) {
                empty = false;
                break;
            }
        }

        if (plugin.bountyManager.hasBounty(player.getUniqueId())) {
            empty = false;
        }

        if (!empty) {
            Block block = getBodyLocation(player.getLocation());
            block.setType(Material.CAKE_BLOCK);

            Inventory inventory = Bukkit.createInventory(null, 54, ChatColor.RED + player.getName() + "'s body");
            inventory.setContents(event.getDrops().toArray(new ItemStack[0]));

            // Hook into bounties
            if (plugin.bountyManager.hasBounty(player.getUniqueId()) && killer instanceof Player) {
                Player killerPlayer = (Player) killer;

                if (killerPlayer.hasPermission("CR.perk.bounty")) {
                    ItemStack itemStack = plugin.actionDefaults.getPlayerHead(player.getName());
                    itemStack = plugin.actionDefaults.setItemStackMeta(itemStack, ChatColor.RED + "Bounty: " + player.getName(),
                            ChatColor.GRAY + "Right click to claim bounty.\n" + HiddenStringUtils.encodeString(player.getUniqueId().toString()));

                    inventory.addItem(itemStack);
                }
            }

            bodies.put(block.getLocation(), new BodyObject(player, killer, System.currentTimeMillis(), block.getLocation(), inventory));

            event.getDrops().clear();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getClickedBlock() == null) {
            return;
        }

        if (event.getClickedBlock().getType() == Material.CAKE_BLOCK) {

            Location loc = event.getClickedBlock().getLocation();
            if (bodies.containsKey(loc)) {

                if (bodies.get(loc).owner.getUniqueId().equals(player.getUniqueId()) || bodies.get(loc).killer != null && bodies.get(loc).killer.getUniqueId().equals(player.getUniqueId())) {
                    temp.put(player, loc);
                    player.openInventory(bodies.get(loc).inventory);
                } else {
                    //300
                    long secondsLeft = ((bodies.get(loc).time / 1000) + 300) - (System.currentTimeMillis() / 1000);
                    if (secondsLeft > 0) {
                        player.sendMessage(ChatColor.GOLD + bodies.get(loc).owner.getName() + "'s body will be lootable in " + ChatColor.RED + secondsLeft + " seconds " + ChatColor.GOLD + "for you.");
                    } else {
                        temp.put(player, loc);
                        player.openInventory(bodies.get(loc).inventory);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemClick(InventoryClickEvent event) {
        if (event.getInventory().getType() != InventoryType.CHEST) {
            return;
        }

        String invName = ChatColor.stripColor(event.getView().getTopInventory().getTitle());
        if (!invName.endsWith("'s body")) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        if (!temp.containsKey(player)) {
            return;
        }

        if (!bodies.containsKey(temp.get(player))) {
            temp.remove(player);
        }

        boolean empty = true;
        for (ItemStack item : event.getInventory().getContents()) {
            if (item != null && item.getAmount() >= 1) {
                empty = false;
                break;
            }
        }

        BodyObject bodyObject = bodies.get(temp.get(player));

        if (empty && bodyObject != null) {
            bodyObject.loc.getWorld().getBlockAt(bodyObject.loc).setType(Material.AIR);

            bodies.remove(bodyObject.loc);
            temp.remove(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().getType() != InventoryType.CHEST) {
            return;
        }

        String invName = ChatColor.stripColor(event.getInventory().getTitle());
        if (!invName.endsWith("'s body")) {
            return;
        }

        Player player = (Player) event.getPlayer();

        if (!temp.containsKey(player)) {
            return;
        }

        if (!bodies.containsKey(temp.get(player))) {
            temp.remove(player);
        }

        boolean empty = true;
        for (ItemStack item : event.getInventory().getContents()) {
            if (item != null && item.getAmount() >= 1) {
                empty = false;
                break;
            }
        }

        BodyObject bodyObject = bodies.get(temp.get(player));

        if (empty && bodyObject != null) {
            bodyObject.loc.getWorld().getBlockAt(bodyObject.loc).setType(Material.AIR);

            bodies.remove(bodyObject.loc);
            temp.remove(player);
        }
    }

    private Block getBodyLocation(Location playerLocation) {
        int x = playerLocation.getBlockX();
        int y = playerLocation.getBlockY();
        int z = playerLocation.getBlockZ();
        World world = playerLocation.getWorld();

        while (y != 0) {
            if (world.getBlockAt(new Location(world, x, y, z)).getType() != Material.AIR) {
                break;
            }
            y--;
        }

        return world.getBlockAt(new Location(world, x, y + 1, z));
    }
}
