package locks;

import com.codisimus.plugins.phatloots.PhatLootsAPI;
import com.codisimus.plugins.phatloots.events.PrePlayerLootEvent;
import com.zeshanaslam.crimering.Main;
import customevents.LockpickEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Door;
import perks.arrest.PerkManager;
import script.ActionDefaults;
import script.ScriptObject;

import javax.script.*;
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
        if (event.getInventory().getType() != InventoryType.CHEST && event.getInventory().getType() != InventoryType.DISPENSER) {
            return;
        }

        ItemStack item = event.getCurrentItem();
        if (item == null) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        // Op bypass
        if (player.isOp()) {
            if (player.getInventory().getItemInMainHand() != null) {
                if (player.getInventory().getItemInMainHand().getType() == Material.PAINTING) {
                    return;
                }
            }
        }

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

                lore.add(ChatColor.translateAlternateColorCodes('&', "&7Use &a/lock &7to add or remove"));
                lore.add(ChatColor.translateAlternateColorCodes('&', "&7players and gangs to lock."));
                lore.add("");
                lore.add(ChatColor.translateAlternateColorCodes('&', "&7Place in &aFirst Slot &7to lock"));
                lore.add(ChatColor.translateAlternateColorCodes('&', "&7chest/door."));
                lore.add("");
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

        // Op bypass
        if (player.isOp()) {
            if (player.getInventory().getItemInMainHand() != null) {
                if (player.getInventory().getItemInMainHand().getType() == Material.PAINTING) {
                    return;
                }
            }
        }

        Block block = event.getClickedBlock();
        BlockState blockState = block.getState();

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

            if (block.getType().toString().contains("DOOR")) {
                if (!plugin.lockManager.unlocked.containsKey(block.getX() + " " + block.getY() + " " + block.getZ() + " " + block.getWorld().getName())) {
                    Block dispenser = block.getWorld().getBlockAt(block.getLocation().getBlockX(), block.getLocation().getBlockY() + 2, block.getLocation().getBlockZ());

                    if (!(dispenser.getState() instanceof Dispenser)) return;

                    Inventory dispenserInv = ((Dispenser) dispenser.getState()).getInventory();

                    if (dispenser.getType() == Material.DISPENSER) {
                        if (!locksUtil.isChestAllowed(player, dispenserInv)) {
                            event.setCancelled(true);

                            if (!player.hasPermission(lockPickPerm)) {
                                player.sendMessage(ChatColor.RED + "Looks like I need someone with lock picking skills!");
                                return;
                            }

                            if (!locksUtil.isPick(player.getInventory().getItemInMainHand())) {
                                player.sendMessage(ChatColor.RED + "This needs to be lock picked!");
                                return;
                            }

                            ItemStack lock = locksUtil.getChestLock(dispenserInv);
                            String lockType = Main.instance.lockManager.chests.get(lock.getTypeId() + ":" + lock.getDurability() + "-lock");

                            player.openInventory(locksUtil.loadInventory(player, block, lockType));
                        }
                    }
                }
            }

            // Dispenser protection
            if (blockState instanceof Dispenser) {
                Dispenser dispenser = (Dispenser) blockState;
                ItemStack itemStack = dispenser.getInventory().getItem(0);

                if (itemStack == null) return;

                if (locksUtil.isOwner(player, itemStack) == 2) {
                    Block doorBlock = block.getWorld().getBlockAt(block.getLocation().getBlockX(), block.getLocation().getBlockY() - 2, block.getLocation().getBlockZ());

                    if (doorBlock.getType().toString().contains("DOOR")) {
                        if (!plugin.lockManager.unlocked.containsKey(doorBlock.getX() + " " + doorBlock.getY() + " " + doorBlock.getZ() + " " + doorBlock.getWorld().getName())) {
                            player.sendMessage(ChatColor.RED + "The door needs to be lock picked first!");
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPreLoot(PrePlayerLootEvent event) {
        Player player = event.getLooter();
        Block block = event.getChest().getBlock();

        // Op bypass
        if (player.isOp()) {
            if (player.getInventory().getItemInMainHand() != null) {
                if (player.getInventory().getItemInMainHand().getType() == Material.PAINTING) {
                    return;
                }
            }
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
                String[] data = id.split(" ");

                // Trigger trap
                triggerTrapChest(player, data);

                lockPickFail(player, data[0], id);
            }

            if (locksUtil.getClickOrder(inventory) == -1) {
                player.closeInventory();

                plugin.lockManager.lastOrder.remove(player.getUniqueId());

                // Hook into crime
                Main.instance.perkManager.copUtil.logCrime(player, PerkManager.Crime.LOCKPICK);

                player.sendMessage(ChatColor.GOLD + "You lock picked the lock!");

                String[] data = id.split(" ");
                plugin.lockManager.unlocked.put(data[1] + " " + data[2] + " " + data[3] + " " + data[4], System.currentTimeMillis());
                Main.instance.getServer().getPluginManager().callEvent(new LockpickEvent(player, data[0], Bukkit.getWorld(data[4]).getBlockAt(Integer.valueOf(data[1]), Integer.valueOf(data[2]), Integer.valueOf(data[3]))));
            }
        } else {
            String[] data = id.split(" ");

            // Trigger trap
            triggerTrapChest(player, data);

            lockPickFail(player, data[0], id);
        }
    }

    private void lockPickFail(Player player, String name, String id) {
        // Hook into crime
        Main.instance.perkManager.copUtil.logCrime(player, PerkManager.Crime.LOCKPICK);

        if (!locksUtil.lockFail(player)) {
            player.closeInventory();

            player.sendMessage(ChatColor.RED + "You ran out of lock picks!");
        } else {
            player.openInventory(locksUtil.loadInventory(player, name, id));
        }
    }

    private void triggerTrapChest(Player player, String[] data) {
        Block block = Bukkit.getWorld(data[4]).getBlockAt(Integer.valueOf(data[1]), Integer.valueOf(data[2]), Integer.valueOf(data[3]));

        BlockState blockState = block.getState();

        if (blockState instanceof Chest) {
            Chest chest = (Chest) blockState;

            String name = locksUtil.useTrap(chest.getInventory());
            if (name != null) {
                if (Main.instance.scriptsManager.contains(name)) {
                    player.sendMessage(ChatColor.RED + "A trap was triggered from the chest!");

                    for (ScriptObject scriptObject : Main.instance.scriptsManager.getObjects(name))

                        try {
                            ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

                            // Objects
                            Bindings bindings = engine.createBindings();
                            bindings.put("player", player);
                            bindings.put("x", Integer.valueOf(data[1]));
                            bindings.put("y", Integer.valueOf(data[2]));
                            bindings.put("z", Integer.valueOf(data[3]));
                            bindings.put("world", data[4]);
                            bindings.put("lockType", data[0]);
                            bindings.put("CR", new ActionDefaults(name, engine));

                            ScriptContext scriptContext = engine.getContext();
                            scriptContext.setBindings(bindings, ScriptContext.ENGINE_SCOPE);

                            engine.eval(scriptObject.scriptData, scriptContext);
                        } catch (ScriptException e) {
                            e.printStackTrace();
                        }
                }
            }

            return;
        }

        Block dispenserBlock = block.getWorld().getBlockAt(block.getLocation().getBlockX(), block.getLocation().getBlockY() + 2, block.getLocation().getBlockZ());
        blockState = dispenserBlock.getState();

        if (blockState instanceof Dispenser) {
            Dispenser dispenser = (Dispenser) blockState;

            String name = locksUtil.useTrap(dispenser.getInventory());
            if (name != null) {
                if (Main.instance.scriptsManager.contains(name)) {
                    player.sendMessage(ChatColor.RED + "A trap was triggered from the chest!");

                    for (ScriptObject scriptObject : Main.instance.scriptsManager.getObjects(name))

                        try {
                            ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

                            // Objects
                            Bindings bindings = engine.createBindings();
                            bindings.put("player", player);
                            bindings.put("x", Integer.valueOf(data[1]));
                            bindings.put("y", Integer.valueOf(data[2]));
                            bindings.put("z", Integer.valueOf(data[3]));
                            bindings.put("world", data[4]);
                            bindings.put("lockType", data[0]);
                            bindings.put("CR", new ActionDefaults(name, engine));

                            ScriptContext scriptContext = engine.getContext();
                            scriptContext.setBindings(bindings, ScriptContext.ENGINE_SCOPE);

                            engine.eval(scriptObject.scriptData, scriptContext);
                        } catch (ScriptException e) {
                            e.printStackTrace();
                        }
                }
            }
        }
    }

    // Protection
    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        List<Block> destroyed = event.blockList();
        Iterator<Block> it = destroyed.iterator();
        while (it.hasNext()) {
            Block block = it.next();
            BlockState blockState = block.getState();

            if (block.getType().toString().contains("DOOR")) {
                Door door = new Door(0, block.getData());

                if (door.isTopHalf()) {
                    block = block.getRelative(BlockFace.DOWN);
                }
            }

            if (Main.instance.lockManager.locks.containsKey(block.getX() + " " + block.getY() + " " + block.getZ() + " " + block.getWorld().getName())) {
                it.remove();
            }

            if (blockState instanceof Chest) {
                Chest chest = (Chest) blockState;
                ItemStack itemStack = chest.getInventory().getItem(0);

                if (itemStack != null) {
                    if (Main.instance.lockManager.chests.containsKey(itemStack.getTypeId() + ":" + itemStack.getDurability() + "-lock"))
                        it.remove();
                }
            }

            if (block.getType().toString().contains("DOOR")) {
                Block dispenserBlock = block.getWorld().getBlockAt(block.getLocation().getBlockX(), block.getLocation().getBlockY() + 2, block.getLocation().getBlockZ());

                if (dispenserBlock.getState() instanceof Dispenser) {
                    Dispenser dispenser = (Dispenser) dispenserBlock.getState();
                    ItemStack itemStack = dispenser.getInventory().getItem(0);

                    if (itemStack != null) {
                        if (Main.instance.lockManager.chests.containsKey(itemStack.getTypeId() + ":" + itemStack.getDurability() + "-lock"))
                            it.remove();
                    }
                }
            }

            if (blockState instanceof Dispenser) {
                Dispenser dispenser = (Dispenser) blockState;
                ItemStack itemStack = dispenser.getInventory().getItem(0);

                if (itemStack != null) {
                    if (Main.instance.lockManager.chests.containsKey(itemStack.getTypeId() + ":" + itemStack.getDurability() + "-lock"))
                        it.remove();
                }
            }
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        BlockState blockState = block.getState();

        if (block.getType().toString().contains("DOOR")) {
            Door door = new Door(0, block.getData());

            if (door.isTopHalf()) {
                block = block.getRelative(BlockFace.DOWN);
            }
        }

        if (Main.instance.lockManager.locks.containsKey(block.getX() + " " + block.getY() + " " + block.getZ() + " " + block.getWorld().getName())) {
            event.setCancelled(true);
        }

        if (blockState instanceof Chest) {
            Chest chest = (Chest) blockState;
            ItemStack itemStack = chest.getInventory().getItem(0);

            if (itemStack != null) {
                if (Main.instance.lockManager.chests.containsKey(itemStack.getTypeId() + ":" + itemStack.getDurability() + "-lock"))
                    event.setCancelled(true);
            }
        }

        if (block.getType().toString().contains("DOOR")) {
            Block dispenserBlock = block.getWorld().getBlockAt(block.getLocation().getBlockX(), block.getLocation().getBlockY() + 2, block.getLocation().getBlockZ());

            if (dispenserBlock.getType() == Material.DISPENSER) {
                Dispenser dispenser = (Dispenser) dispenserBlock.getState();
                ItemStack itemStack = dispenser.getInventory().getItem(0);

                if (itemStack != null) {
                    if (Main.instance.lockManager.chests.containsKey(itemStack.getTypeId() + ":" + itemStack.getDurability() + "-lock"))
                        event.setCancelled(true);
                }
            }
        }

        if (blockState instanceof Dispenser) {
            Dispenser dispenser = (Dispenser) blockState;
            ItemStack itemStack = dispenser.getInventory().getItem(0);

            if (itemStack != null) {
                if (Main.instance.lockManager.chests.containsKey(itemStack.getTypeId() + ":" + itemStack.getDurability() + "-lock"))
                    event.setCancelled(true);
            }
        }
    }


    // Dispenser support
    /*@EventHandler
    public void onDispense(BlockDispenseEvent event) {
        ItemStack itemStack = event.getItem();
        MaterialData data = event.getBlock().getState().getData();
        Dispenser dispenser = (Dispenser) data;

        if (itemStack != null) {
            if (Main.instance.lockManager.chests.containsKey(itemStack.getTypeId() + ":" + itemStack.getDurability() + "-trap")) {
                String trap = Main.instance.lockManager.chests.get(itemStack.getTypeId() + ":" + itemStack.getDurability() + "-trap");
                Location loc = event.getBlock().getLocation();
                loc.add(dispenser.getFacing().getModX() + 1, dispenser.getFacing().getModY() + 1, dispenser.getFacing().getModZ() + 1);

                Arrow arrow = event.getBlock().getWorld().spawnArrow(loc, new Vector(dispenser.getFacing().getModX(), dispenser.getFacing().getModY(), dispenser.getFacing().getModZ()), 4.0f, 4.0f);
                Main.instance.lockManager.arrows.put(arrow.getUniqueId(), trap);
            }
        }
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Arrow && event.getEntity() instanceof Player) {
            Arrow arrow = (Arrow) event.getDamager();
            Player player = (Player) event.getEntity();

            System.out.println(arrow.getCustomName());
        }
    }*/
}
