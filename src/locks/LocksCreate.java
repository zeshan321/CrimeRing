package locks;

import com.zeshanaslam.crimering.FileHandler;
import com.zeshanaslam.crimering.Main;
import net.sothatsit.blockstore.BlockStoreApi;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Door;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LocksCreate implements CommandExecutor, Listener {

    private final Main plugin;
    private final LocksUtil locksUtil = new LocksUtil();

    public LocksCreate(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (commandLabel.equalsIgnoreCase("CRLock")) {
            if (sender.isOp()) {
                if (args.length < 1) {
                    sender.sendMessage(ChatColor.GOLD + "/CRLock create <inv size> <name>");
                    sender.sendMessage(ChatColor.GOLD + "/CRLock edit <name>");
                    sender.sendMessage(ChatColor.GOLD + "/CRLock set <name>");
                    sender.sendMessage(ChatColor.GOLD + "/CRLock remove");
                    return false;
                }

                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only can be ran in-game!");
                    return false;
                }

                Player player = (Player) sender;

                String type = args[0];

                if (type.equalsIgnoreCase("create")) {
                    if (args.length < 3) {
                        player.sendMessage(ChatColor.GOLD + "/CRLock create <inv size> <name>");
                        return false;
                    }

                    int size = Integer.parseInt(args[1]);
                    String name = args[2];

                    if (!FileHandler.fileExists("plugins/CrimeRing/locks/" + name + ".yml")) {
                        Inventory inventory = Bukkit.createInventory(player, size, "CR:LP editing: " + name);

                        player.openInventory(inventory);
                    } else {
                        player.sendMessage("Already exists! /CRLock edit " + name);
                    }
                }

                if (type.equalsIgnoreCase("edit")) {
                    if (args.length < 1) {
                        player.sendMessage(ChatColor.GOLD + "/CRLock edit <name>");
                        return false;
                    }

                    String name = args[1];

                    if (FileHandler.fileExists("plugins/CrimeRing/locks/" + name + ".yml")) {
                        player.openInventory(locksUtil.loadEditInventory(name));
                    } else {
                        player.sendMessage("Lock not found! /CRLock create <inv size> " + name);
                    }
                }

                if (type.equalsIgnoreCase("set")) {
                    plugin.lockManager.assign.put(player.getUniqueId(), args[1]);

                    player.sendMessage(ChatColor.GOLD + "Right click block to lock with " + args[1] + "!");
                }

                if (type.equalsIgnoreCase("remove")) {
                    plugin.lockManager.assign.put(player.getUniqueId(), "REMOVEPLZ");

                    player.sendMessage(ChatColor.GOLD + "Right click block to remove lock!");
                }
            }
        }
        return true;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Inventory inventory = event.getClickedInventory();

        if (!inventory.getTitle().startsWith("CR:LP editing: ")) {
            return;
        }

        String filename = inventory.getTitle().replace("CR:LP editing: ", "");

        // Cancel clicks
        event.setCancelled(true);

        ItemStack item = inventory.getItem(event.getSlot());
        boolean remove = true;

        // Add
        if (event.getClick() == ClickType.LEFT) {
            remove = false;
            inventory.setItem(event.getSlot(), locksUtil.getNextItem(item, inventory));
        }

        // Remove
        if (event.getClick() == ClickType.RIGHT) {
            remove = true;
            inventory.setItem(event.getSlot(), null);
        }

        // Update changes
        item = inventory.getItem(event.getSlot());

        FileHandler fileHandler = new FileHandler("plugins/CrimeRing/locks/" + filename + ".yml");

        List<String> listData;
        if (fileHandler.contains("data")) {
            listData = fileHandler.getStringList("data");
        } else {
            listData = new ArrayList<>();
        }

        // Remove previous ones
        Iterator<String> iterator = listData.iterator();
        while (iterator.hasNext()) {
            String data = iterator.next();

            if (data.startsWith(event.getSlot() + " ")) {
                iterator.remove();
            }
        }

        if (!remove) {
            listData.add(event.getSlot() + " " + item.getType().toString());
        }

        fileHandler.set("data", listData);
        fileHandler.set("size", inventory.getSize());
        fileHandler.save();
    }

    @EventHandler
    public void onBlockInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // Only use right hand
        if (event.getHand() == EquipmentSlot.OFF_HAND) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (!plugin.lockManager.assign.containsKey(player.getUniqueId())) {
            return;
        }

        Block block = event.getClickedBlock().getState().getBlock();

        if (block.getType().toString().contains("DOOR")) {
            Door door = new Door(0, block.getData());

            if (door.isTopHalf()) {
                block = block.getRelative(BlockFace.DOWN);
            }
        }

        String type = plugin.lockManager.assign.get(player.getUniqueId());

        if (type.equals("REMOVEPLZ")) {
            BlockStoreApi.removeBlockMeta(block, plugin, "CRLock");
            player.sendMessage(ChatColor.GOLD + "Removed lock from block!");
        } else {
            BlockStoreApi.setBlockMeta(block, plugin, "CRLock", plugin.lockManager.assign.get(player.getUniqueId()));
            player.sendMessage(ChatColor.GOLD + "Set lock to block!");
        }

        plugin.lockManager.assign.remove(player.getUniqueId());
    }
}
