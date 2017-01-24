package locks;

import com.zeshanaslam.crimering.FileHandler;
import com.zeshanaslam.crimering.Main;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import utils.HiddenStringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class LocksUtil {

    public ItemStack createLockCylinder() {
        ItemStack itemStack = new ItemStack(Material.IRON_BLOCK);

        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.GRAY + "Lock Cylinder");
        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    public ItemStack createBars() {
        ItemStack itemStack = new ItemStack(Material.IRON_FENCE);

        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(" ");
        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    public ItemStack createStart() {
        ItemStack itemStack = new ItemStack(Material.DIAMOND_HOE);
        itemStack.setDurability((short) 499);

        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.GRAY + "Start");
        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    public ItemStack getNextItem(ItemStack itemStack, Inventory inventory) {
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return createLockCylinder();
        }

        if (itemStack.getType() == Material.IRON_BLOCK) {
            return createBars();
        }

        if (!inventory.contains(createStart())) {
            return createStart();
        }

        return createLockCylinder();
    }

    public ItemStack getItemFromName(String type) {
        if (type.equals("IRON_FENCE")) {
            return createBars();
        }

        if (type.equals("DIAMOND_HOE")) {
            return createStart();
        }

        return createLockCylinder();
    }

    public Inventory loadEditInventory(String name) {
        FileHandler fileHandler = new FileHandler("plugins/CrimeRing/locks/" + name + ".yml");

        Inventory inventory = Bukkit.createInventory(null, fileHandler.getInteger("size"), "CR:LP editing: " + name);

        fileHandler.getStringList("data").stream().filter(data -> data.contains(" ")).forEach(data -> {
            String[] dataSplit = data.split(" ");

            int slot = Integer.valueOf(dataSplit[0]);
            String type = dataSplit[1];

            inventory.setItem(slot, getItemFromName(type));
        });

        return inventory;
    }

    public Inventory loadInventory(Player player, Block block, String name) {
        FileHandler fileHandler = new FileHandler("plugins/CrimeRing/locks/" + name + ".yml");

        Inventory inventory = Bukkit.createInventory(null, fileHandler.getInteger("size"), "Lock picking");
        List<String> lore = new ArrayList<>();
        int start = 0;

        for (String data : fileHandler.getStringList("data")) {
            String[] dataSplit = data.split(" ");

            int slot = Integer.valueOf(dataSplit[0]);
            String type = dataSplit[1];

            if (type.equals("IRON_BLOCK")) {
                lore.add(String.valueOf(slot));
            }

            if (type.equals("DIAMOND_HOE")) {
                start = slot;
            }

            inventory.setItem(slot, getItemFromName(type));
        }

        // Shuffle the list
        Collections.shuffle(lore, new Random(System.nanoTime()));
        String data = lore.stream().map(Object::toString).collect(Collectors.joining(", "));

        lore.clear();

        String encodedData = HiddenStringUtils.encodeString(data);
        lore.add(encodedData);
        lore.add(HiddenStringUtils.encodeString(name + " " + block.getX() + " " + block.getY() + " " + block.getZ() + " " + block.getWorld().getName()));

        // Store data in starting item
        ItemMeta itemMeta = inventory.getItem(start).getItemMeta();
        itemMeta.setLore(lore);

        inventory.getItem(start).setItemMeta(itemMeta);

        Main.instance.lockManager.lastOrder.put(player.getUniqueId(), encodedData);

        return inventory;
    }

    public Inventory loadInventory(Player player, String name, String id) {
        FileHandler fileHandler = new FileHandler("plugins/CrimeRing/locks/" + name + ".yml");

        Inventory inventory = Bukkit.createInventory(null, fileHandler.getInteger("size"), "Lock picking");
        int start = 0;

        for (String data : fileHandler.getStringList("data")) {
            String[] dataSplit = data.split(" ");

            int slot = Integer.valueOf(dataSplit[0]);
            String type = dataSplit[1];

            if (type.equals("DIAMOND_HOE")) {
                start = slot;
            }

            inventory.setItem(slot, getItemFromName(type));
        }

        List<String> lore = new ArrayList<>();
        lore.add(Main.instance.lockManager.lastOrder.get(player.getUniqueId()));
        lore.add(HiddenStringUtils.encodeString(id));

        ItemMeta itemMeta = inventory.getItem(start).getItemMeta();
        itemMeta.setLore(lore);

        inventory.getItem(start).setItemMeta(itemMeta);

        return inventory;
    }

    public int getClickOrder(Inventory inventory) {
        ItemStack start = null;
        for (ItemStack item : inventory.getContents()) {
            if (item == null) {
                continue;
            }

            if (item.getType() == Material.DIAMOND_HOE) {
                start = item;
                break;
            }
        }

        String encoded = start.getItemMeta().getLore().get(0);
        encoded = HiddenStringUtils.extractHiddenString(encoded);

        if (encoded.split(", ")[0].equals("") || encoded.split(", ")[0].equals(" ")) {
            return -1;
        }

        return Integer.valueOf(encoded.split(", ")[0]);
    }

    public void removeClickOrder(Inventory inventory, int slot) {
        ItemStack start = null;
        int i = -1;
        for (ItemStack item : inventory.getContents()) {
            i++;

            if (item == null) {
                continue;
            }

            if (item.getType() == Material.DIAMOND_HOE) {
                start = item;
                break;
            }
        }

        ItemMeta itemMeta = start.getItemMeta();

        String encoded = HiddenStringUtils.extractHiddenString(itemMeta.getLore().get(0));
        if (encoded.contains(", ")) {
            encoded = encoded.split(slot + ", ")[1];
        } else {
            encoded = encoded.replace(String.valueOf(slot), "");
        }

        List<String> data = itemMeta.getLore();
        data.set(0, HiddenStringUtils.encodeString(encoded));

        itemMeta.setLore(data);
        start.setItemMeta(itemMeta);

        inventory.setItem(i, start);
    }

    public boolean lockFail(Player player) {
        int defaultChance = 75;
        double random = Math.random() * 100;

        if (random <= defaultChance) {
            if (isPick(player.getInventory().getItemInMainHand())) {
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0F, 1.0F);

                ItemStack localItemStack = player.getInventory().getItemInMainHand();
                localItemStack.setAmount(localItemStack.getAmount() - 1);

                player.getInventory().setItemInMainHand(localItemStack);
            } else {
                return false;
            }
        }

        return true;
    }

    public String getLockID(Inventory inventory) {
        ItemStack start = null;
        for (ItemStack item : inventory.getContents()) {
            if (item == null) {
                continue;
            }

            if (item.getType() == Material.DIAMOND_HOE) {
                start = item;
                break;
            }
        }

        return HiddenStringUtils.extractHiddenString(start.getItemMeta().getLore().get(1));
    }


    public boolean isPick(ItemStack item) {
        if (item != null) {
            if (item.getTypeId() == 293 && item.getDurability() == 499) {
                return true;
            }
        }

        return false;
    }

    public boolean isChestAllowed(Player player, Inventory inventory) {
        ItemStack firstSlot = inventory.getItem(0);

        if (firstSlot == null || !firstSlot.hasItemMeta() || !firstSlot.getItemMeta().hasLore() || ChatColor.stripColor(firstSlot.getItemMeta().getLore().get(0)).contains("Unactivated")) {
            return true;
        }

        if (Main.instance.lockManager.chests.containsKey(firstSlot.getTypeId() + ":" + firstSlot.getDurability() + "-lock")) {
            String gangLock = null;
            List<String> users = new ArrayList<>();

            for (String s : firstSlot.getItemMeta().getLore()) {
                s = ChatColor.stripColor(s);

                if (s.startsWith("Gang: ")) {
                    gangLock = s.replace("Gang: ", "");
                } else if (s.startsWith("Owner: ")) {
                    users.add(s.replace("Owner: ", ""));
                } else {
                    users.add(s);
                }
            }

            if (users.contains(player.getName())) {
                return true;
            }

            Clan clan = Main.instance.clanManager.getClanByPlayerUniqueId(player.getUniqueId());

            if (clan != null) {
                if (clan.getName().equals(gangLock)) {
                    return true;
                }
            }
        }

        return false;
    }

    public ItemStack getChestLock(Inventory inventory) {
        ItemStack firstSlot = inventory.getItem(0);

        return firstSlot;
    }

    public int isOwner(Player player, ItemStack itemStack) {
        if (!Main.instance.lockManager.chests.containsKey(itemStack.getTypeId() + ":" + itemStack.getDurability() + "-lock")) {
            return 0;
        }

        if (!itemStack.hasItemMeta() || !itemStack.getItemMeta().hasLore() || ChatColor.stripColor(itemStack.getItemMeta().getLore().get(0)).contains("unactivated")) {
            return 0;
        }

        for (String lore : itemStack.getItemMeta().getLore()) {
            lore = ChatColor.stripColor(lore);

            if (lore.startsWith("Owner: ")) {
                String owner = lore.replace("Owner: ", "");

                if (owner.equals(player.getName())) {
                    return 1;
                }
            }
        }

        return 2;
    }
}
