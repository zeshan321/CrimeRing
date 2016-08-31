package script;

import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.zeshanaslam.crimering.FileHandler;
import com.zeshanaslam.crimering.Main;
import me.robin.battlelevels.api.BattleLevelsAPI;
import net.elseland.xikage.MythicMobs.API.Bukkit.BukkitMobsAPI;
import net.elseland.xikage.MythicMobs.API.Exceptions.InvalidMobTypeException;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftEntity;
import org.bukkit.entity.*;
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
import java.util.*;

public class ActionDefaults {

    private String scriptID;
    private ScriptEngine engine;

    public ActionDefaults(String scriptID, ScriptEngine engine) {
        this.scriptID = scriptID;
        this.engine = engine;
    }

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
            player.sendMessage(message.split("\\n"));
        } else {
            for (Player players : party.getMembers()) {
                if (skip && players.getName().equals(player.getName())) {
                    continue;
                }

                players.sendMessage(message.split("\\n"));
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
        Main.instance.entityManager.navigate(player, engine, entity, script, x, y, z);
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

    public void removeSBObjective(Player player, String objectiveName) {
        PartyAPI partyAPI = new PartyAPI();
        PartyObject partyObject = partyAPI.getParty(player);

        if (partyObject == null) {
            if (player.getScoreboard() == null) {
                return;
            }

            Scoreboard scoreboard = player.getScoreboard();

            scoreboard.resetScores(objectiveName);
        } else {
            for (Player players : partyObject.getMembers()) {
                if (players.getScoreboard() == null) {
                    continue;
                }

                Scoreboard scoreboard = player.getScoreboard();

                scoreboard.resetScores(objectiveName);
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
                bindings.put("CR", new ActionDefaults(scriptID, engine));

                compiledScript.eval(bindings);
            } catch (ScriptException e) {
                e.printStackTrace();
            }
        }, seconds * 20);
    }

    public void runFunctionLater(String name, int seconds) {
        Main.instance.getServer().getScheduler().runTaskLater(Main.instance, () -> {
            Invocable invocable = (Invocable) engine;
            try {
                invocable.invokeFunction(name);
            } catch (ScriptException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }, seconds * 20);
    }


    public void teleportEntity(LivingEntity entity, String world, int x, int y, int z, float yaw, float pitch) {
        Location loc = new Location(Bukkit.getWorld(world), x, y, z);
        loc.setYaw(yaw);
        loc.setPitch(pitch);

        entity.teleport(loc);

        ((CraftEntity) entity).getHandle().setPositionRotation(x, y, z, yaw, pitch);
    }

    public void sendMessageLater(Player player, String message, int seconds) {
        Main.instance.getServer().getScheduler().runTaskLater(Main.instance, () -> {
            player.sendMessage(message.split("\\n"));
        }, seconds * 20);
    }

    public void equipItem(LivingEntity entity, String location, String material, int meta) {
        location = location.toUpperCase();

        ItemStack itemStack = new ItemStack(Material.valueOf(material), 1);
        itemStack.setDurability((short) meta);
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

    public void playSound(Player player, String sound, int seconds) {
        if (seconds == 0) {
            player.playSound(player.getLocation(), Sound.valueOf(sound), 1, 0);
            return;
        }

        Main.instance.getServer().getScheduler().runTaskLater(Main.instance, () -> {
            player.playSound(player.getLocation(), Sound.valueOf(sound), 1, 0);
        }, seconds * 20);
    }

    public void showFakeblock(Player player, String file) {
        FileHandler yaml = new FileHandler("plugins/CrimeRing/fakeblocks/" + file + ".yml");

        if (yaml.getBoolean("Editing")) {
            player.sendMessage(ChatColor.GOLD + "This area is currently being edited by a Admin. Sorry for the inconvenience.");
            return;
        }

        for (String location : yaml.getStringList("List")) {
            String[] location1 = location.split(" ");
            int x = Integer.parseInt(location1[0]);
            int y = Integer.parseInt(location1[1]);
            int z = Integer.parseInt(location1[2]);
            String world = location1[3];
            Material material = Material.matchMaterial(location1[4]);
            int byteData = Integer.parseInt(location1[5]);

            if (!(Main.instance.fakeBlocksLocation.containsKey(x + "-" + y + "-" + z + "-" + world))) {
                Main.instance.fakeBlocksLocation.put(x + "-" + y + "-" + z + "-" + world, material.toString() + " " + byteData);
            }

            Location loc = new Location(Bukkit.getWorld(world), x, y, z);
            player.sendBlockChange(loc, material, (byte) byteData);
        }
    }

    public void removeFakeblock(Player player, String file) {
        FileHandler yaml = new FileHandler("plugins/CrimeRing/fakeblocks/" + file + ".yml");

        for (String location : yaml.getStringList("List")) {
            String[] location1 = location.split(" ");
            int x = Integer.parseInt(location1[0]);
            int y = Integer.parseInt(location1[1]);
            int z = Integer.parseInt(location1[2]);
            String world = location1[3];

            if (Main.instance.fakeBlocksLocation.containsKey(x + "-" + y + "-" + z + "-" + world)) {
                Main.instance.fakeBlocksLocation.remove(x + "-" + y + "-" + z + "-" + world);
            }

            Location loc = new Location(Bukkit.getWorld(world), x, y, z);
            Bukkit.getWorld(world).getBlockAt(loc).getState().update();
        }
    }

    public boolean isEquipped(Player player, String location, String type, int data) {
        switch (location) {
            case "HELMET":
                if (player.getEquipment().getHelmet() != null && player.getEquipment().getHelmet().getType().equals(Material.valueOf(type)) && player.getEquipment().getHelmet().getDurability() == (short) data) {
                    return true;
                }
                break;
            case "CHESTPLATE":
                if (player.getEquipment().getChestplate() != null && player.getEquipment().getChestplate().getType().equals(Material.valueOf(type)) && player.getEquipment().getChestplate().getDurability() == (short) data) {
                    return true;
                }
                break;
            case "LEGGINGS":
                if (player.getEquipment().getLeggings() != null && player.getEquipment().getLeggings().getType().equals(Material.valueOf(type)) && player.getEquipment().getLeggings().getDurability() == (short) data) {
                    return true;
                }
                break;
            case "BOOTS":
                if (player.getEquipment().getBoots() != null && player.getEquipment().getBoots().getType().equals(Material.valueOf(type)) && player.getEquipment().getBoots().getDurability() == (short) data) {
                    return true;
                }
                break;
            case "HAND":
                if (player.getEquipment().getItemInMainHand() != null && player.getEquipment().getItemInMainHand().getType().equals(Material.valueOf(type)) && player.getEquipment().getItemInMainHand().getDurability() == (short) data) {
                    return true;
                }
                break;
        }

        return false;
    }

    public void startListener(Player player, String type, String trigger, String method) {
        type = type.toUpperCase();

        if (Main.instance.listeners.contains(player.getUniqueId(), type + "-" + trigger)) {
            stopListener(player, type, trigger);
        }

        ListenerObject listenerObject = new ListenerObject(scriptID, engine, method);
        switch (type) {
            case "INVENTORY":
                Main.instance.listeners.put(player.getUniqueId(), type + "-" + trigger, listenerObject);
                break;

            case "REGION_ENTER":
                Main.instance.listeners.put(player.getUniqueId(), type + "-" + trigger, listenerObject);
                break;

            case "REGION_LEAVE":
                Main.instance.listeners.put(player.getUniqueId(), type + "-" + trigger, listenerObject);
                break;

            case "BLOCK":
                Main.instance.listeners.put(player.getUniqueId(), type + "-" + trigger, listenerObject);
                break;

            case "DEATH":
                Main.instance.listeners.put(player.getUniqueId(), type + "-" + trigger, listenerObject);
                break;

            case "NPC":
                Main.instance.listeners.put(player.getUniqueId(), type + "-" + trigger, listenerObject);
                break;

            case "EQUIP":
                Main.instance.listeners.put(player.getUniqueId(), type + "-" + trigger, listenerObject);
                break;

            case "UNEQUIP":
                Main.instance.listeners.put(player.getUniqueId(), type + "-" + trigger, listenerObject);
                break;
        }
    }

    public void stopListener(Player player, String type, String trigger) {
        type = type.toUpperCase();

        switch (type) {
            case "INVENTORY":
                Main.instance.listeners.remove(player.getUniqueId(), type + "-" + trigger);
                break;

            case "REGION_ENTER":
                Main.instance.listeners.remove(player.getUniqueId(), type + "-" + trigger);
                break;

            case "REGION_LEAVE":
                Main.instance.listeners.remove(player.getUniqueId(), type + "-" + trigger);
                break;

            case "BLOCK":
                Main.instance.listeners.remove(player.getUniqueId(), type + "-" + trigger);
                break;

            case "DEATH":
                Main.instance.listeners.remove(player.getUniqueId(), type + "-" + trigger);
                break;

            case "NPC":
                Main.instance.listeners.remove(player.getUniqueId(), type + "-" + trigger);
                break;

            case "EQUIP":
                Main.instance.listeners.remove(player.getUniqueId(), type + "-" + trigger);
                break;

            case "UNEQUIP":
                Main.instance.listeners.remove(player.getUniqueId(), type + "-" + trigger);
                break;
        }
    }

    public void clearListener(Player player) {
        Iterator<UUID> iterator = Main.instance.listeners.rowKeySet().iterator();

        while (iterator.hasNext()) {
            if (iterator.next() == player.getUniqueId()) {
                iterator.remove();
            }
        }
    }

    public void setItemframeItem(String world, int x, int y, int z, String type, int data) {
        ItemStack itemStack = new ItemStack(Material.valueOf(type));

        if (data > 0)
        itemStack.setDurability((short) data);

        Location location = new Location(Bukkit.getWorld(world), x, y ,z);
        for(Entity entity : location.getWorld().getEntities()) {
            if(entity.getLocation().distance(location) <= 2) {
                if (entity instanceof ItemFrame) {
                    ItemFrame itemFrame = (ItemFrame) entity;
                    itemFrame.setItem(itemStack);
                    return;
                }
            }
        }
    }

    public void setItemframeRotation(String world, int x, int y, int z, String rotation) {
        Location location = new Location(Bukkit.getWorld(world), x, y ,z);
        for(Entity entity : location.getWorld().getEntities()) {
            if(entity.getLocation().distance(location) <= 2) {
                if (entity instanceof ItemFrame) {
                    ItemFrame itemFrame = (ItemFrame) entity;
                    itemFrame.setRotation(Rotation.valueOf(rotation));
                    return;
                }
            }
        }
    }
}
