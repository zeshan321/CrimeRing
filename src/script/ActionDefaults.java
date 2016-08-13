package script;

import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.zeshanaslam.crimering.FileHandler;
import com.zeshanaslam.crimering.Main;
import net.elseland.xikage.MythicMobs.API.Bukkit.BukkitMobsAPI;
import net.elseland.xikage.MythicMobs.API.Exceptions.InvalidMobTypeException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import raids.PartyAPI;
import raids.PartyObject;
import utils.EnchantGlow;
import utils.ItemUtils;
import utils.MessageUtil;

import java.util.Collection;

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

    public void cancelRaid(Player player, String filename) {
        if (!FileHandler.fileExists("plugins/CrimeRing/raids/" + filename + ".yml")) {
            if (player.isOp()) {
                player.sendMessage(ChatColor.RED + "Error: Unable to create raid: " + filename);
            }

            System.out.println(ChatColor.RED + "Error: Unable to create raid: " + filename);
            return;
        }

        FileHandler fileHandler = new FileHandler("plugins/CrimeRing/raids/" + filename + ".yml");
        PartyAPI partyAPI = new PartyAPI();
        PartyObject party = partyAPI.getParty(player);

        if (party == null) {
            player.teleport(new Location(Bukkit.getWorld(fileHandler.getString("info.worlds")), fileHandler.getInteger("info.xs"), fileHandler.getInteger("info.ys"), fileHandler.getInteger("info.zs")));
            Main.instance.raidManager.cancelRaid(player);
        } else {
            for (Player players: party.getMembers()) {
                players.teleport(new Location(Bukkit.getWorld(fileHandler.getString("info.worlds")), fileHandler.getInteger("info.xs"), fileHandler.getInteger("info.ys"), fileHandler.getInteger("info.zs")));
                Main.instance.raidManager.cancelRaid(players);
            }
        }
    }

    public void addFlag(String key, String flag) {
        Main.instance.flag.add(key + "-" + flag);
    }

    public boolean hasFlag(String key, String flag) {
        return Main.instance.flag.contains(key + "-" + flag);
    }

    public void removeFlag(String key, String flag) {
        Main.instance.flag.remove(key + "-" + flag);
    }

    public void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        new MessageUtil().sendTitle(player, title, subtitle, fadeIn, stay, fadeOut);
    }

    public void sendActionBar(Player player, String message) {
        new MessageUtil().sendActionBar(player, message);
    }

    public void spawnMob(String type, String world, int amount, int x, int y, int z) {
        BukkitMobsAPI bukkitMobsAPI = new BukkitMobsAPI();
        try {
            for (int i = 1; i < amount + 1; i++) {
                bukkitMobsAPI.spawnMythicMob(bukkitMobsAPI.getMythicMob(type), new Location(Bukkit.getWorld(world), x, y, z));
            }
        } catch (InvalidMobTypeException e) {
            e.printStackTrace();
        }
    }

    public void clearMobs(String world, String regionName) {
        ProtectedRegion rg = Main.instance.worldGuardPlugin.getRegionManager(Bukkit.getWorld(world)).getRegion(regionName);

        if (rg != null) {
            Region region = new CuboidRegion(rg.getMaximumPoint(), rg.getMinimumPoint());
            Location centerLoc = new Location(Bukkit.getWorld(world), region.getCenter().getX(), region.getCenter().getY(), region.getCenter().getZ());
            Collection<Entity> entities = Bukkit.getWorld(world).getNearbyEntities(centerLoc, region.getWidth() / 2, region.getHeight() / 2, region.getLength() / 2);

            entities.stream().filter(entity -> !(entity instanceof Player)).forEach(Entity::remove);
        }
    }

    public void runCommand(String command) {
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    public void sendRaidMessage(Player player, String message, boolean skip) {
        PartyAPI partyAPI = new PartyAPI();
        PartyObject party = partyAPI.getParty(player);

        if (party == null) {
            player.sendMessage(message);
        } else {
            for (Player players: party.getMembers()) {
                if (skip && players.getName().equals(player.getName())) {
                    continue;
                }

                players.sendMessage(message);
            }
        }
    }

    public boolean hasItem(Player player, String itemname, boolean take) {
        if (player.getItemInHand() == null) {
            return false;
        }

        ItemStack item = player.getItemInHand();
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return false;
        }

        String displayname = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        if (displayname.equals(itemname)) {
            return true;
        }

        return false;
    }

    public void setValue(String id, int amount) {
        Main.instance.values.put(id, amount);
    }

    public int getValue(String id) {
        return Main.instance.values.get(id);
    }

    public void removeValue(String id) {
        Main.instance.values.remove(id);
    }

    public void addValue(String id) {
        Main.instance.values.put(id, Main.instance.values.get(id) + 1);
    }
}
