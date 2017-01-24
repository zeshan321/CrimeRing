package locks;

import com.codisimus.plugins.phatloots.PhatLootsAPI;
import com.codisimus.plugins.phatloots.events.PrePlayerLootEvent;
import com.zeshanaslam.crimering.Main;
import customevents.LockpickEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Door;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LockListener implements Listener {

    private final Main plugin;
    private final LocksUtil locksUtil = new LocksUtil();
    private String lockPickPerm = "CR.lockpick";

    public LockListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemClick(InventoryClickEvent event) {
        if (event.getInventory().getType() != InventoryType.CHEST) {
            return;
        }

        ItemStack item = event.getCurrentItem();
        if (item == null) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        if (locksUtil.isOwner(player, item) == 2) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // Only use right hand
        if (event.getHand() == EquipmentSlot.OFF_HAND) {
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {

            if (player.getInventory().getItemInMainHand() == null) {
                return;
            }

            ItemStack itemStack = player.getInventory().getItemInMainHand();

            if (!Main.instance.lockManager.chests.containsKey(itemStack.getTypeId() + ":" + itemStack.getDurability() + "-lock")) {
                return;
            }

            if (!itemStack.hasItemMeta() || !itemStack.getItemMeta().hasLore() || ChatColor.stripColor(itemStack.getItemMeta().getLore().get(0)).contains("Owner: None")) {
                ItemMeta itemMeta = itemStack.getItemMeta();

                List<String> lore = new ArrayList<>();

                lore.add(ChatColor.GREEN + "Owner: " + player.getName());

                itemMeta.setLore(lore);

                itemStack.setItemMeta(itemMeta);

                player.getInventory().setItemInMainHand(itemStack);

                player.sendMessage(ChatColor.GREEN + "The lock has been activated!");
            } else {
                player.sendMessage(ChatColor.RED + "This lock is already activated!");
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // Only use right hand
        if (event.getHand() == EquipmentSlot.OFF_HAND) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();

        if (block.getType().toString().contains("DOOR")) {
            Door door = new Door(0, block.getData());

            if (door.isTopHalf()) {
                block = block.getRelative(BlockFace.DOWN);
            }
        }

        if (PhatLootsAPI.isPhatLootChest(block)) {
            return;
        }

        if (Main.instance.lockManager.locks.containsKey(block.getX() + " " + block.getY() + " " + block.getZ() + " " + block.getWorld().getName())) {
            String lockType = Main.instance.lockManager.locks.get(block.getX() + " " + block.getY() + " " + block.getZ() + " " + block.getWorld().getName());

            if (!plugin.lockManager.unlocked.containsKey(block.getX() + " " + block.getY() + " " + block.getZ() + " " + block.getWorld().getName())) {
                event.setCancelled(true);

                if (!player.hasPermission(lockPickPerm)) {
                    player.sendMessage(ChatColor.RED + "Looks like I need someone with lock picking skills!");
                    return;
                }

                if (!locksUtil.isPick(player.getInventory().getItemInMainHand())) {
                    player.sendMessage(ChatColor.RED + "This needs to be lock picked!");
                    return;
                }

                player.openInventory(locksUtil.loadInventory(player, block, lockType));
            }
        } else {
            // Check if chest contains a lock
            BlockState blockState = block.getState();

            if (blockState instanceof Chest) {
                if (!plugin.lockManager.unlocked.containsKey(block.getX() + " " + block.getY() + " " + block.getZ() + " " + block.getWorld().getName())) {
                    Chest chest = (Chest) blockState;

                    if (!locksUtil.isChestAllowed(player, chest.getInventory())) {
                        event.setCancelled(true);

                        if (!player.hasPermission(lockPickPerm)) {
                            player.sendMessage(ChatColor.RED + "Looks like I need someone with lock picking skills!");
                            return;
                        }

                        if (!locksUtil.isPick(player.getInventory().getItemInMainHand())) {
                            player.sendMessage(ChatColor.RED + "This needs to be lock picked!");
                            return;
                        }

                        ItemStack lock = locksUtil.getChestLock(chest.getInventory());
                        String lockType = Main.instance.lockManager.chests.get(lock.getTypeId() + ":" + lock.getDurability() + "-lock");

                        player.openInventory(locksUtil.loadInventory(player, block, lockType));
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPreLoot(PrePlayerLootEvent event) {
        Player player = event.getLooter();
        Block block = event.getChest().getBlock();

        if (Main.instance.lockManager.locks.containsKey(block.getX() + " " + block.getY() + " " + block.getZ() + " " + block.getWorld().getName())) {
            String lockType = Main.instance.lockManager.locks.get(block.getX() + " " + block.getY() + " " + block.getZ() + " " + block.getWorld().getName());

            if (!plugin.lockManager.unlocked.containsKey(block.getX() + " " + block.getY() + " " + block.getZ() + " " + block.getWorld().getName())) {
                event.setCancelled(true);

                if (!player.hasPermission(lockPickPerm)) {
                    player.sendMessage(ChatColor.RED + "Looks like I need someone with lock picking skills!");
                    return;
                }

                if (!locksUtil.isPick(player.getInventory().getItemInMainHand())) {
                    player.sendMessage(ChatColor.RED + "You have to have a lock pick in your hand!");
                    return;
                }

                player.openInventory(locksUtil.loadInventory(player, block, lockType));
            }
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Inventory inventory = event.getClickedInventory();

        if (inventory == null || inventory.getTitle() == null) {
            return;
        }

        if (!inventory.getTitle().equals("Lock picking")) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        ItemStack item = inventory.getItem(event.getSlot());
        if (item == null) {
            return;
        }

        // Cancel clicks
        event.setCancelled(true);

        int order = locksUtil.getClickOrder(inventory);
        String id = locksUtil.getLockID(inventory);

        if (item.getType() == Material.IRON_BLOCK) {
            int slot = event.getSlot();

            if (order == slot) {
                locksUtil.removeClickOrder(inventory, order);

                inventory.setItem(slot, null);

                if (slot - 9 > 0 && inventory.getItem(slot - 9) == null) {
                    inventory.setItem(slot - 9, item);
                } else {
                    inventory.setItem(slot + 9, item);
                }
            } else {
                lockPickFail(player, id.split(" ")[0], id);
            }

            if (locksUtil.getClickOrder(inventory) == -1) {
                player.closeInventory();

                plugin.lockManager.lastOrder.remove(player.getUniqueId());

                player.sendMessage(ChatColor.GOLD + "You lock picked the lock!");

                String[] data = id.split(" ");
                plugin.lockManager.unlocked.put(data[1] + " " + data[2] + " " + data[3] + " " + data[4], System.currentTimeMillis());
                Main.instance.getServer().getPluginManager().callEvent(new LockpickEvent(player, data[0], Bukkit.getWorld(data[4]).getBlockAt(Integer.valueOf(data[1]), Integer.valueOf(data[2]), Integer.valueOf(data[3]))));
            }
        } else {
            lockPickFail(player, id.split(" ")[0], id);
        }
    }

    private void lockPickFail(Player player, String name, String id) {
        if (!locksUtil.lockFail(player)) {
            player.closeInventory();

            player.sendMessage(ChatColor.RED + "You ran out of lock picks!");
        } else {
            player.openInventory(locksUtil.loadInventory(player, name, id));
        }
    }

    // Protection
    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        List<Block> destroyed = event.blockList();
        Iterator<Block> it = destroyed.iterator();
        while (it.hasNext()) {
            Block block = it.next();

            if (block.getType().toString().contains("DOOR")) {
                Door door = new Door(0, block.getData());

                if (door.isTopHalf()) {
                    block = block.getRelative(BlockFace.DOWN);
                }
            }

            if (Main.instance.lockManager.locks.containsKey(block.getX() + " " + block.getY() + " " + block.getZ() + " " + block.getWorld().getName()))
                it.remove();
        }
    }
}
