package script;

import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.zeshanaslam.crimering.FileHandler;
import com.zeshanaslam.crimering.Main;
import me.robin.battlelevels.api.BattleLevelsAPI;
import net.elseland.xikage.MythicMobs.API.Bukkit.BukkitMobsAPI;
import net.elseland.xikage.MythicMobs.API.Exceptions.InvalidMobTypeException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import raids.PartyAPI;
import raids.PartyObject;
import utils.EnchantGlow;
import utils.ItemUtils;
import utils.MessageUtil;

import javax.script.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

        try {
            Inventory inventory = Bukkit.createInventory(null, fileHandler.getInteger("Info.Size"), fileHandler.getString("Info.Title").replaceAll("<player>", player.getName()).replaceAll("&", "§"));

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

            // Remove raid items
            new ItemUtils().clearRaidItems(player);
        } else {
            for (Player players : party.getMembers()) {
                players.teleport(new Location(Bukkit.getWorld(fileHandler.getString("info.worlds")), fileHandler.getInteger("info.xs"), fileHandler.getInteger("info.ys"), fileHandler.getInteger("info.zs")));
                Main.instance.raidManager.cancelRaid(players);

                // Remove raid items
                new ItemUtils().clearRaidItems(players);
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

    public void addGlobal(String key, String flag) {
        Main.instance.globalFlags.put(key, flag);
    }

    public String getGlobal(String key) {
        return Main.instance.globalFlags.get(key);
    }

    public boolean hasGlobal(String key) {
        return Main.instance.globalFlags.containsKey(key);
    }

    public void removeGlobal(String key) {
        Main.instance.globalFlags.remove(key);
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

    public int getEntitiesInRegion(String world, String regionName) {
        ProtectedRegion rg = Main.instance.worldGuardPlugin.getRegionManager(Bukkit.getWorld(world)).getRegion(regionName);

        int amount = 0;

        if (rg != null) {
            Region region = new CuboidRegion(rg.getMaximumPoint(), rg.getMinimumPoint());
            Location centerLoc = new Location(Bukkit.getWorld(world), region.getCenter().getX(), region.getCenter().getY(), region.getCenter().getZ());
            Collection<Entity> entities = Bukkit.getWorld(world).getNearbyEntities(centerLoc, region.getWidth() / 2, region.getHeight() / 2, region.getLength() / 2);

            for (Entity entity : entities) {
                if (entity instanceof Monster && !entity.isDead()) {
                    amount++;
                }
            }
        }

        return amount;
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
            for (Player players : party.getMembers()) {
                if (skip && players.getName().equals(player.getName())) {
                    continue;
                }

                players.sendMessage(message);
            }
        }
    }

    public boolean hasItem(Player player, String itemname, boolean take) {
        if (player.getInventory().getItemInMainHand() == null) {
            return false;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return false;
        }

        String displayname = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        if (displayname.equals(itemname)) {
            if (take) {
                player.getInventory().setItemInMainHand(null);
            }
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
        if (!Main.instance.values.containsKey(id)) {
            Main.instance.values.put(id, 0);
            return;
        }

        Main.instance.values.put(id, Main.instance.values.get(id) + 1);
    }

    public boolean containsValue(String id) {
        return Main.instance.values.containsKey(id);
    }

    public ItemStack getItem(String name, int amount) {
        FileHandler fileHandler = new FileHandler("plugins/CrimeRing/items/" + name + ".yml");

        ItemStack item = fileHandler.getItemStack("Item");
        item.setAmount(amount);

        return item;
    }

    public boolean isInRaid(Player player, String name) {
        return Main.instance.raidManager.isInRaid(player);
    }

    public void giveItem(Player player, String name, int amount, boolean remove) {
        FileHandler fileHandler = new FileHandler("plugins/CrimeRing/items/" + name + ".yml");

        ItemStack item = fileHandler.getItemStack("Item");
        item.setAmount(amount);

        // Add 'Raid Item' to lore
        if (remove) {
            ItemMeta itemMeta = item.getItemMeta();
            List<String> lore = new ArrayList<>();

            if (itemMeta.hasLore()) {
                lore = itemMeta.getLore();
            }

            lore.add(ChatColor.GOLD + "Raid Item");
            itemMeta.setLore(lore);

            item.setItemMeta(itemMeta);
        }

        player.getInventory().addItem(item);
    }

    public void dropItemAtLocation(Location location, String name, int amount, boolean remove) {
        FileHandler fileHandler = new FileHandler("plugins/CrimeRing/items/" + name + ".yml");

        ItemStack item = fileHandler.getItemStack("Item");
        item.setAmount(amount);

        // Add 'Raid Item' to lore
        if (remove) {
            ItemMeta itemMeta = item.getItemMeta();
            List<String> lore = new ArrayList<>();

            if (itemMeta.hasLore()) {
                lore = itemMeta.getLore();
            }

            lore.add(ChatColor.GOLD + "Raid Item");
            itemMeta.setLore(lore);

            item.setItemMeta(itemMeta);
        }

        location.getWorld().dropItem(location, item);
    }

    public void setPushBlock(String world, int x, int y, int z) {
        Bukkit.getWorld(world).getBlockAt(new Location(Bukkit.getWorld(world), x, y, z)).setType(Material.QUARTZ_ORE);
    }

    public void removePushBlocks(String world, String regionName) {
        ProtectedRegion rg = Main.instance.worldGuardPlugin.getRegionManager(Bukkit.getWorld(world)).getRegion(regionName);

        if (rg != null) {
            Region region = new CuboidRegion(rg.getMaximumPoint(), rg.getMinimumPoint());

            com.sk89q.worldedit.Vector max = region.getMaximumPoint();
            com.sk89q.worldedit.Vector min = region.getMinimumPoint();

            for (int i = min.getBlockX(); i <= max.getBlockX(); i++) {
                for (int j = min.getBlockY(); j <= max.getBlockY(); j++) {
                    for (int k = min.getBlockZ(); k <= max.getBlockZ(); k++) {
                        Block block = Bukkit.getServer().getWorld(world).getBlockAt(i, j, k);

                        if (block.getType() == Material.QUARTZ_ORE) {
                            block.setType(Material.AIR);
                        }
                    }
                }
            }
        }
    }

    public void createEntityWithSkin(Player player, String type, String name, String skin, boolean hidden, String world, int x, int y, int z, float yaw, float pitch) {
        Main.instance.entityManager.createEntityWithSkin(player, type, name, skin, hidden, world, x, y, z, yaw, pitch);
    }

    public LivingEntity getEntity(Player player, String name) {
        return Main.instance.entityManager.getEntity(player, name);
    }

    public void navigate(Player player, LivingEntity entity, String script, int x, int y, int z) {
        Main.instance.entityManager.navigate(player, entity, script, x, y, z);
    }

    public void addSkillPoint(Player player) {
        if (FileHandler.fileExists("plugins/CrimeRing/player/" + player.getUniqueId().toString())) {
            return;
        }

        FileHandler fileHandler = new FileHandler(("plugins/CrimeRing/player/" + player.getUniqueId().toString()));
        fileHandler.increment("skill-points");

        fileHandler.save();
    }

    public void setSkillPoint(Player player, int amount) {
        if (FileHandler.fileExists("plugins/CrimeRing/player/" + player.getUniqueId().toString())) {
            return;
        }

        FileHandler fileHandler = new FileHandler(("plugins/CrimeRing/player/" + player.getUniqueId().toString()));
        fileHandler.set("skill-points", amount);

        fileHandler.save();
    }

    public int getSkillPoint(Player player) {
        if (FileHandler.fileExists("plugins/CrimeRing/player/" + player.getUniqueId().toString())) {
            return 0;
        }

        FileHandler fileHandler = new FileHandler(("plugins/CrimeRing/player/" + player.getUniqueId().toString()));

        return fileHandler.getInteger("skill-points");
    }

    public int getPlayerLevel(Player player) {
        return BattleLevelsAPI.getLevel(player.getUniqueId());
    }

    public void createScoreboard(Player player, String name) {
        PartyAPI partyAPI = new PartyAPI();
        PartyObject partyObject = partyAPI.getParty(player);

        if (partyObject == null) {
            ScoreboardManager manager = Bukkit.getScoreboardManager();
            Scoreboard scoreboard = manager.getNewScoreboard();

            Objective objective = scoreboard.registerNewObjective("dummy", "title");
            objective.setDisplayName(name);
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);

            player.setScoreboard(scoreboard);
        } else {
            ScoreboardManager manager = Bukkit.getScoreboardManager();
            Scoreboard scoreboard = manager.getNewScoreboard();

            Objective objective = scoreboard.registerNewObjective("dummy", "title");
            objective.setDisplayName(name);
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);

            for (Player players : partyObject.getMembers()) {
                players.setScoreboard(scoreboard);
            }
        }
    }

    public void setSBObjective(Player player, String objectiveName, int value) {
        PartyAPI partyAPI = new PartyAPI();
        PartyObject partyObject = partyAPI.getParty(player);

        if (partyObject == null) {
            if (player.getScoreboard() == null) {
                return;
            }

            Scoreboard scoreboard = player.getScoreboard();
            Objective objective = scoreboard.getObjective(DisplaySlot.SIDEBAR);

            objective.getScore(objectiveName).setScore(value);
        } else {
            for (Player players : partyObject.getMembers()) {
                if (players.getScoreboard() == null) {
                    continue;
                }

                Scoreboard scoreboard = player.getScoreboard();
                Objective objective = scoreboard.getObjective(DisplaySlot.SIDEBAR);

                objective.getScore(objectiveName).setScore(value);
            }
        }
    }

    public void addSBObjective(Player player, String objectiveName) {
        PartyAPI partyAPI = new PartyAPI();
        PartyObject partyObject = partyAPI.getParty(player);

        if (partyObject == null) {
            if (player.getScoreboard() == null) {
                return;
            }

            Scoreboard scoreboard = player.getScoreboard();
            Objective objective = scoreboard.getObjective(DisplaySlot.SIDEBAR);

            if (objective.getScore(objectiveName) != null) {
                objective.getScore(objectiveName).setScore(objective.getScore(objectiveName).getScore() + 1);
            } else {
                objective.getScore(objectiveName).setScore(0);
            }
        } else {
            for (Player players : partyObject.getMembers()) {
                if (players.getScoreboard() == null) {
                    continue;
                }

                Scoreboard scoreboard = players.getScoreboard();
                Objective objective = scoreboard.getObjective(DisplaySlot.SIDEBAR);

                if (objective.getScore(objectiveName) != null) {
                    objective.getScore(objectiveName).setScore(objective.getScore(objectiveName).getScore() + 1);
                } else {
                    objective.getScore(objectiveName).setScore(0);
                }
            }
        }
    }

    public void subSBObjective(Player player, String objectiveName) {
        PartyAPI partyAPI = new PartyAPI();
        PartyObject partyObject = partyAPI.getParty(player);

        if (partyObject == null) {
            if (player.getScoreboard() == null) {
                return;
            }

            Scoreboard scoreboard = player.getScoreboard();
            Objective objective = scoreboard.getObjective(DisplaySlot.SIDEBAR);

            if (objective.getScore(objectiveName) != null) {
                objective.getScore(objectiveName).setScore(objective.getScore(objectiveName).getScore() - 1);
            } else {
                objective.getScore(objectiveName).setScore(0);
            }
        } else {
            for (Player players : partyObject.getMembers()) {
                if (players.getScoreboard() == null) {
                    continue;
                }

                Scoreboard scoreboard = players.getScoreboard();
                Objective objective = scoreboard.getObjective(DisplaySlot.SIDEBAR);

                if (objective.getScore(objectiveName) != null) {
                    objective.getScore(objectiveName).setScore(objective.getScore(objectiveName).getScore() - 1);
                } else {
                    objective.getScore(objectiveName).setScore(0);
                }
            }
        }
    }

    public void removeSB(Player player) {
        PartyAPI partyAPI = new PartyAPI();
        PartyObject partyObject = partyAPI.getParty(player);

        if (partyObject == null) {
            if (player.getScoreboard() == null) {
                return;
            }

            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        } else {
            for (Player players : partyObject.getMembers()) {
                if (players.getScoreboard() == null) {
                    continue;
                }

                player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            }
        }
    }

    public void runScriptLater(Player player, String name, int seconds) {
        Main.instance.getServer().getScheduler().runTaskLater(Main.instance, () -> {
            ScriptObject scriptObject = Main.instance.scriptsManager.getObject(name);

            try {
                ScriptEngine engine = Main.instance.scriptsManager.engine;
                CompiledScript compiledScript = scriptObject.script;

                // Objects
                Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
                bindings.put("player", player);
                bindings.put("CR", new ActionDefaults());

                compiledScript.eval(bindings);
            } catch (ScriptException e) {
                e.printStackTrace();
            }
        }, seconds * 20);
    }

    public void teleport(LivingEntity entity, String world, int x, int y, int z, float yaw, float pitch) {
        Location loc = new Location(Bukkit.getWorld(world), x, y, z);
        loc.setYaw(yaw);
        loc.setPitch(pitch);

        entity.teleport(loc);
    }

    public void sendMessageLater(Player player, String message, int seconds) {
        Main.instance.getServer().getScheduler().runTaskLater(Main.instance, () -> {
            player.sendMessage(message);
        }, seconds * 20);
    }

    public void equipItem(LivingEntity entity, String location, String material, int meta) {
        location = location.toUpperCase();

        ItemStack itemStack = new ItemStack(Material.valueOf(material), 1, (byte) meta);
        switch (location) {
            case "HELMET":
                entity.getEquipment().setHelmet(itemStack);
                break;
            case "CHESTPLATE":
                entity.getEquipment().setChestplate(itemStack);
                break;
            case "LEGGINGS":
                entity.getEquipment().setLeggings(itemStack);
                break;
            case "BOOTS":
                entity.getEquipment().setBoots(itemStack);
                break;
            case "HAND":
                entity.getEquipment().setItemInMainHand(itemStack);
                break;
        }
    }
}
