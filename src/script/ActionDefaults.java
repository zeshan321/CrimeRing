package script;

import com.zeshanaslam.crimering.FileHandler;
import com.zeshanaslam.crimering.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import raids.PartyAPI;
import raids.PartyObject;
import utils.EnchantGlow;
import utils.ItemUtils;

public class ActionDefaults {

    public void teleport(Player player, String world, int x, int y, int z, float yaw, float pitch) {
        Location loc = new Location(Bukkit.getWorld(world), x, y, z);
        loc.setYaw(yaw);
        loc.setPitch(pitch);

        player.teleport(loc);
    }

    public void openInv(Player player, String filename) {
        if (!FileHandler.fileExists("plugins/CrimeRing/inv/" + filename + ".yml")) {
            if (player.isOp()) {
                player.sendMessage(ChatColor.RED + "Error: Unable to create inventory: " + filename);
            }

            System.out.println(ChatColor.RED + "Error: Unable to create inventory: " + filename);
            return;
        }

        FileHandler fileHandler = new FileHandler("plugins/CrimeRing/inv/" + filename + ".yml");

        Inventory inventory = Bukkit.createInventory(null, fileHandler.getInteger("Info.Size"), fileHandler.getString("Info.Title").replaceAll("<player>", player.getName()).replaceAll("&", "§"));
        try {
            String material;
            String displayName;
            String lore;
            String perm = null;
            boolean usingPerm = false;
            int position;
            int bytedata = 0;
            int amount = 0;

            for (String s : fileHandler.getKeys()) {

                if (s.equals("Info")) {
                    continue;
                }

                material = fileHandler.getString(s + ".Material");
                displayName = fileHandler.getString(s + ".Name").replaceAll("&", "§");
                lore = fileHandler.getString(s + ".Lore").replaceAll("&", "§");
                position = fileHandler.getInteger(s + ".Pos");

                if (!(fileHandler.contains(s + ".Amount"))) {
                    amount = 1;
                }

                if (fileHandler.contains(s + ".Amount")) {
                    amount = fileHandler.getInteger(s + ".Amount");
                }

                if (!(fileHandler.contains(s + ".Meta"))) {
                    bytedata = 0;
                }

                if (fileHandler.contains(s + ".Meta")) {
                    bytedata = fileHandler.getInteger(s + ".Meta");
                }

                if (fileHandler.contains(s + ".Perm")) {
                    perm = fileHandler.getString(s + ".Perm");
                    usingPerm = true;
                }

                if (!usingPerm) {
                    if (!(fileHandler.contains(s + ".Meta"))) {
                        inventory.setItem(position, new ItemUtils().createItem(Material.getMaterial(material), displayName, lore));
                    }

                    if (fileHandler.contains(s + ".Meta")) {
                        inventory.setItem(position, new ItemUtils().createItem(Material.getMaterial(material), displayName, lore, amount, bytedata));
                    }

                    if (fileHandler.contains(s + ".Glow")) {
                        ItemStack item = inventory.getItem(position);
                        EnchantGlow.addGlow(item);
                        inventory.setItem(position, item);
                    }

                }
                if (usingPerm && player.hasPermission(perm)) {
                    if (!(fileHandler.contains(s + ".Meta"))) {
                        inventory.setItem(position, new ItemUtils().createItem(Material.getMaterial(material), displayName, lore));
                    }

                    if (fileHandler.contains(s + ".Meta")) {
                        inventory.setItem(position, new ItemUtils().createItem(Material.getMaterial(material), displayName, lore, amount, bytedata));
                    }

                    if (fileHandler.contains(s + ".Glow")) {
                        ItemStack item = inventory.getItem(position);
                        EnchantGlow.addGlow(item);
                        inventory.setItem(position, item);
                    }
                }
            }

            player.openInventory(inventory);
        } catch (NullPointerException e) {
            if (player.isOp()) {
                player.sendMessage(ChatColor.RED + "Error: Unable to create inventory: " + filename);
            }

            System.out.println(ChatColor.RED + "Error: Unable to create inventory: " + filename);
        }
    }

    public void openRaid(Player player, String filename) {
        if (!FileHandler.fileExists("plugins/CrimeRing/raids/" + filename + ".yml")) {
            if (player.isOp()) {
                player.sendMessage(ChatColor.RED + "Error: Unable to create raid: " + filename);
            }

            System.out.println(ChatColor.RED + "Error: Unable to create raid: " + filename);
            return;
        }

        if (Main.instance.raidManager.canStartRaid(player, filename)) {
            Main.instance.raidManager.openRaidMenu(player, filename);
        }
    }
}
