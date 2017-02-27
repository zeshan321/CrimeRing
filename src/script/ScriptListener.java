package script;

import brewing.BrewObject;
import com.zeshanaslam.crimering.Main;
import haveric.recipeManager.RecipeManager;
import haveric.recipeManager.recipes.BaseRecipe;
import haveric.recipeManager.recipes.CombineRecipe;
import haveric.recipeManager.recipes.CraftRecipe;
import haveric.recipeManager.recipes.SmeltRecipe;
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
import org.bukkit.inventory.Inventory;
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

    // Recipes
    @EventHandler
    public void onRecipeClick(InventoryClickEvent event) {
        if (event.getClickedInventory() != null) {
            Player player = (Player) event.getWhoClicked();
            ItemStack item = event.getCurrentItem();

            if (event.getClickedInventory().getName() != null) {
                if (ChatColor.stripColor(event.getClickedInventory().getName()).startsWith("Recipe Viewer: ")) {
                    event.setCancelled(true);
                    return;
                }
            }

            if (item == null || item.getItemMeta() == null || item.getItemMeta().getDisplayName() == null) {
                return;
            }


            if (ChatColor.stripColor(item.getItemMeta().getDisplayName()).startsWith("Recipe: ")) {
                String name = ChatColor.stripColor(item.getItemMeta().getDisplayName()).replace("Recipe: ", "");
                BaseRecipe recipe = RecipeManager.getRecipes().getRecipeByName(name);

                if (recipe != null) {
                    if (recipe instanceof CraftRecipe) {
                        CraftRecipe craftRecipe = (CraftRecipe) recipe;

                        Inventory inventory = Bukkit.getServer().createInventory(player, InventoryType.WORKBENCH, "Recipe Viewer: " + name);

                        int i = 0;
                        for (ItemStack itemStack : craftRecipe.getIngredients()) {
                            i++;

                            if (itemStack == null) continue;

                            inventory.setItem(i, plugin.renamerManager.renameItem(itemStack));
                        }

                        inventory.setItem(0, craftRecipe.getFirstResult());
                        player.openInventory(inventory);
                    }

                    if (recipe instanceof CombineRecipe) {
                        CombineRecipe craftRecipe = (CombineRecipe) recipe;

                        Inventory inventory = Bukkit.getServer().createInventory(player, InventoryType.WORKBENCH, "Recipe Viewer: " + name);

                        int i = 0;
                        for (ItemStack itemStack : craftRecipe.getIngredients()) {
                            i++;

                            if (itemStack == null) continue;

                            inventory.setItem(i, plugin.renamerManager.renameItem(itemStack));
                        }

                        inventory.setItem(0, craftRecipe.getFirstResult());
                        player.openInventory(inventory);
                    }

                    if (recipe instanceof SmeltRecipe) {
                        SmeltRecipe smeltRecipe = (SmeltRecipe) recipe;

                        Inventory inventory = Bukkit.getServer().createInventory(player, InventoryType.FURNACE, "Recipe Viewer: " + name);

                        inventory.setItem(0, plugin.renamerManager.renameItem(smeltRecipe.getIngredient()));
                        inventory.setItem(1, plugin.renamerManager.renameItem(smeltRecipe.getFuel()));
                        inventory.setItem(2, plugin.renamerManager.renameItem(smeltRecipe.getResult()));

                        player.openInventory(inventory);
                    }
                } else {
                    if (plugin.brewingManager.brewsName.containsKey(name)) {
                        BrewObject brewObject = plugin.brewingManager.brewObjectList.get(plugin.brewingManager.brewsName.get(name));

                        Inventory inventory = Bukkit.getServer().createInventory(player, InventoryType.BREWING, "Recipe Viewer: " + name);

                        if (!brewObject.slot1.startsWith("0:0")) {
                            int idStore = Integer.valueOf(brewObject.slot1.split(" ")[0].split(":")[0]);
                            int dataStore = Integer.valueOf(brewObject.slot1.split(" ")[0].split(":")[1]);
                            int amountStore = Integer.valueOf(brewObject.slot1.split(" ")[1]);

                            inventory.setItem(0, plugin.actionDefaults.createItemStackWithRenamer(idStore, amountStore, dataStore));
                        }

                        if (!brewObject.slot2.startsWith("0:0")) {
                            int idStore = Integer.valueOf(brewObject.slot2.split(" ")[0].split(":")[0]);
                            int dataStore = Integer.valueOf(brewObject.slot2.split(" ")[0].split(":")[1]);
                            int amountStore = Integer.valueOf(brewObject.slot2.split(" ")[1]);

                            inventory.setItem(1, plugin.actionDefaults.createItemStackWithRenamer(idStore, amountStore, dataStore));
                        }

                        if (!brewObject.slot3.startsWith("0:0")) {
                            int idStore = Integer.valueOf(brewObject.slot3.split(" ")[0].split(":")[0]);
                            int dataStore = Integer.valueOf(brewObject.slot3.split(" ")[0].split(":")[1]);
                            int amountStore = Integer.valueOf(brewObject.slot3.split(" ")[1]);

                            inventory.setItem(2, plugin.actionDefaults.createItemStackWithRenamer(idStore, amountStore, dataStore));
                        }

                        if (!brewObject.slot4.startsWith("0:0")) {
                            int idStore = Integer.valueOf(brewObject.slot4.split(" ")[0].split(":")[0]);
                            int dataStore = Integer.valueOf(brewObject.slot4.split(" ")[0].split(":")[1]);
                            int amountStore = Integer.valueOf(brewObject.slot4.split(" ")[1]);

                            inventory.setItem(3, plugin.actionDefaults.createItemStackWithRenamer(idStore, amountStore, dataStore));
                        }

                        inventory.setItem(4, plugin.actionDefaults.createItemStackWithRenamer(377, 1, 0));

                        player.openInventory(inventory);
                    }
                }
            }
        }
    }
}
