package script;

import com.enjin.officialplugin.points.PointsAPI;
import com.shampaggon.crackshot.CSUtility;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.zeshanaslam.crimering.FileHandler;
import com.zeshanaslam.crimering.Main;
import es.pollitoyeye.Bikes.BikeManager;
import es.pollitoyeye.Bikes.CarManager;
import es.pollitoyeye.Bikes.VehiclesMain;
import me.Stijn.AudioClient.AudioClient;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MiscDisguise;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.robin.battlelevels.api.BattleLevelsAPI;
import merchants.api.Merchant;
import net.elseland.xikage.MythicMobs.API.Bukkit.BukkitMobsAPI;
import net.elseland.xikage.MythicMobs.API.Exceptions.InvalidMobTypeException;
import net.elseland.xikage.MythicMobs.API.ThreatTables;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_11_R1.PacketPlayOutCustomSoundEffect;
import net.minecraft.server.v1_11_R1.SoundCategory;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.inventivetalent.bossbar.BossBar;
import org.inventivetalent.bossbar.BossBarAPI;
import org.inventivetalent.particle.ParticleEffect;
import raids.PartyAPI;
import raids.PartyObject;
import utils.ItemUtils;
import utils.MessageUtil;
import utils.MoneyUtil;
import utils.TargetHelper;

import javax.script.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

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

    public List<Entity> getAllEntitiesInRegion(String world, String regionName, boolean monsterOnly) {
        List<Entity> entityList = new ArrayList<>();

        ProtectedRegion rg = Main.instance.worldGuardPlugin.getRegionManager(Bukkit.getWorld(world)).getRegion(regionName);

        if (rg != null) {
            Region region = new CuboidRegion(rg.getMaximumPoint(), rg.getMinimumPoint());
            Location centerLoc = new Location(Bukkit.getWorld(world), region.getCenter().getX(), region.getCenter().getY(), region.getCenter().getZ());
            Collection<Entity> entities = Bukkit.getWorld(world).getNearbyEntities(centerLoc, region.getWidth() / 2, region.getHeight() / 2, region.getLength() / 2);

            for (Entity entity : entities) {
                if (monsterOnly) {
                    if (entity instanceof Monster && !entity.isDead()) {
                        entityList.add(entity);
                    }
                } else {
                    entityList.add(entity);
                }
            }
        }

        return entityList;
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

    public ItemStack getItem(Player player, String name, int amount, boolean remove) {
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

        return item;
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
        if (!FileHandler.fileExists("plugins/CrimeRing/player/" + player.getUniqueId().toString())) {
            return;
        }

        FileHandler fileHandler = new FileHandler("plugins/CrimeRing/player/" + player.getUniqueId().toString());
        fileHandler.increment("skill-points");

        fileHandler.save();
    }

    public void removeSkillPoint(Player player, int amount) {
        if (!FileHandler.fileExists("plugins/CrimeRing/player/" + player.getUniqueId().toString())) {
            return;
        }

        FileHandler fileHandler = new FileHandler("plugins/CrimeRing/player/" + player.getUniqueId().toString());
        fileHandler.set("skill-points", fileHandler.getInteger("skill-points") - amount);

        fileHandler.save();
    }

    public void setSkillPoint(Player player, int amount) {
        if (!FileHandler.fileExists("plugins/CrimeRing/player/" + player.getUniqueId().toString())) {
            return;
        }

        FileHandler fileHandler = new FileHandler("plugins/CrimeRing/player/" + player.getUniqueId().toString());
        fileHandler.set("skill-points", amount);

        fileHandler.save();
    }

    public int getSkillPoint(Player player) {
        if (!FileHandler.fileExists("plugins/CrimeRing/player/" + player.getUniqueId().toString())) {
            return -1;
        }

        FileHandler fileHandler = new FileHandler("plugins/CrimeRing/player/" + player.getUniqueId().toString());

        return fileHandler.getInteger("skill-points");
    }

    public int getPlayerLevel(Player player) {
        return BattleLevelsAPI.getLevel(player.getUniqueId());
    }

    public void addPlayerScore(Player player, double amount) {
        BattleLevelsAPI.addScore(player.getUniqueId(), amount, true);
    }

    public double getPlayerScore(Player player) {
        return BattleLevelsAPI.getScore(player.getUniqueId());
    }

    public int getPlayerDeaths(Player player) {
        return BattleLevelsAPI.getDeaths(player.getUniqueId());
    }

    public int getPlayerKills(Player player) {
        return BattleLevelsAPI.getKills(player.getUniqueId());
    }

    public int getPlayerKillstreak(Player player) {
        return BattleLevelsAPI.getKillstreak(player.getUniqueId());
    }

    public int getPlayerTopKillstreak(Player player) {
        return BattleLevelsAPI.getTopKillstreak(player.getUniqueId());
    }

    public double getPlayerKDR(Player player) {
        return BattleLevelsAPI.getKdr(player.getUniqueId());
    }

    public String getTopKills() {
        String leader = "";
        int i = 0;
        List<Map.Entry<String, Integer>> top = BattleLevelsAPI.getTopKills();

        for (Map.Entry<String, Integer> data : top) {
            i++;
            leader = leader + ChatColor.GREEN + i + ". " + data.getKey() + ": " + data.getValue() + "\n";
        }

        return leader;
    }

    public String getTopDeaths() {
        String leader = "";
        int i = 0;
        List<Map.Entry<String, Integer>> top = BattleLevelsAPI.getTopDeaths();

        for (Map.Entry<String, Integer> data : top) {
            i++;
            leader = leader + ChatColor.GREEN + i + ". " + data.getKey() + ": " + data.getValue() + "\n";
        }

        return leader;
    }

    public String getTopLevels() {
        String leader = "";
        int i = 0;
        List<Map.Entry<String, Integer>> top = BattleLevelsAPI.getTopLevels();

        for (Map.Entry<String, Integer> data : top) {
            i++;
            leader = leader + ChatColor.GREEN + i + ". " + data.getKey() + ": " + data.getValue() + "\n";
        }

        return leader;
    }

    public String getTopHighestKillstreaks() {
        String leader = "";
        int i = 0;
        List<Map.Entry<String, Integer>> top = BattleLevelsAPI.getTopHighestKillstreaks();

        for (Map.Entry<String, Integer> data : top) {
            i++;
            leader = leader + ChatColor.GREEN + i + ". " + data.getKey() + ": " + data.getValue() + "\n";
        }

        return leader;
    }

    public String getTopScores() {
        String leader = "";
        int i = 0;

        List<Map.Entry<String, Double>> top = BattleLevelsAPI.getTopScores();

        for (Map.Entry<String, Double> data : top) {
            i++;
            leader = leader + ChatColor.GREEN + i + ". " + data.getKey() + ": " + data.getValue() + "\n";
        }

        return leader;
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
            for (ScriptObject scriptObject : Main.instance.scriptsManager.getObjects(name)) {

                try {
                    ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

                    // Objects
                    Bindings bindings = engine.createBindings();
                    bindings.put("player", player);
                    bindings.put("CR", new ActionDefaults(scriptID, engine));

                    ScriptContext scriptContext = engine.getContext();
                    scriptContext.setBindings(bindings, scriptContext.ENGINE_SCOPE);

                    engine.eval(scriptObject.scriptData, scriptContext);
                } catch (ScriptException e) {
                    e.printStackTrace();
                }
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

    // Cancels on player death
    public void runFunctionLater(Player player, String name, int seconds) {
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                Main.instance.playerTasks.remove(this.getTaskId());

                Invocable invocable = (Invocable) engine;
                try {
                    invocable.invokeFunction(name);
                } catch (ScriptException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskLater(Main.instance, seconds * 20);

        Main.instance.playerTasks.put(task.getTaskId(), player.getUniqueId());
    }

    // Repeating function
    public void repeatFunction(String name, int seconds) {
        final int[] id = {0};

        id[0] = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.instance, () -> {
            int task = id[0];

            Invocable invocable = (Invocable) engine;
            try {
                invocable.invokeFunction(name, task);
            } catch (ScriptException | NoSuchMethodException e) {
                e.printStackTrace();
            }

        }, 0L, seconds * 20);
    }

    public void repeatFunction(Player player, String name, int seconds) {
        final int[] id = {0};

        id[0] = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.instance, () -> {
            int task = id[0];

            Main.instance.playerTasks.remove(task);
            Invocable invocable = (Invocable) engine;
            try {
                invocable.invokeFunction(name, task);
            } catch (ScriptException | NoSuchMethodException e) {
                e.printStackTrace();
            }

        }, 0L, seconds * 20);

        Main.instance.playerTasks.put(id[0], player.getUniqueId());
    }

    public void cancelTask(int id) {
        Bukkit.getScheduler().cancelTask(id);
    }

    public void runScript(Player player, String name) {
        for (ScriptObject scriptObject : Main.instance.scriptsManager.getObjects(name)) {

            try {
                ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

                // Objects
                Bindings bindings = engine.createBindings();
                bindings.put("player", player);
                bindings.put("CR", new ActionDefaults(scriptID, engine));

                ScriptContext scriptContext = engine.getContext();
                scriptContext.setBindings(bindings, scriptContext.ENGINE_SCOPE);

                engine.eval(scriptObject.scriptData, scriptContext);
            } catch (ScriptException e) {
                e.printStackTrace();
            }
        }
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
            case "LOCKPICK":
                Main.instance.listeners.put(player.getUniqueId(), type + "-" + trigger, listenerObject);
                break;

            case "TRADE":
                Main.instance.listeners.put(player.getUniqueId(), type + "-" + trigger, listenerObject);
                break;

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

            case "DEATH_ENTITY":
                Main.instance.listeners.put(player.getUniqueId(), type + "-" + trigger, listenerObject);
                break;

            case "DEATH_PLAYER":
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

            case "BREAK":
                Main.instance.listeners.put(player.getUniqueId(), type + "-" + trigger, listenerObject);
                break;

            case "INTERACT":
                Main.instance.listeners.put(player.getUniqueId(), type + "-" + trigger, listenerObject);
                break;

            // 'GROW' will not work with listener
            case "GROW":
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

            case "DEATH_ENTITY":
                Main.instance.listeners.remove(player.getUniqueId(), type + "-" + trigger);
                break;

            case "DEATH_PLAYER":
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

            case "BREAK":
                Main.instance.listeners.remove(player.getUniqueId(), type + "-" + trigger);
                break;

            case "INTERACT":
                Main.instance.listeners.remove(player.getUniqueId(), type + "-" + trigger);
                break;

            case "GROW":
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

    public void setItemframeItem(String world, int x, int y, int z, int id, int data) {
        ItemStack itemStack = new ItemStack(id);

        if (data > 0)
            itemStack.setDurability((short) data);

        Location location = new Location(Bukkit.getWorld(world), x, y, z);

        for (Entity entity : location.getWorld().getNearbyEntities(location, 0.5, 0.5, 0.5)) {
            if (entity instanceof ItemFrame) {
                ItemFrame itemFrame = (ItemFrame) entity;
                itemFrame.setItem(itemStack);
            }
        }
    }

    public void setItemframeItem(Entity entity, int id, int data) {
        ItemStack itemStack = new ItemStack(id);

        if (data > 0)
            itemStack.setDurability((short) data);

        if (entity instanceof ItemFrame) {
            ItemFrame itemFrame = (ItemFrame) entity;
            itemFrame.setItem(itemStack);
        }
    }

    public void setItemframeRotation(String world, int x, int y, int z, String rotation) {
        Location location = new Location(Bukkit.getWorld(world), x, y, z);

        for (Entity entity : location.getWorld().getNearbyEntities(location, 0.5, 0.5, 0.5)) {
            if (entity instanceof ItemFrame) {
                ItemFrame itemFrame = (ItemFrame) entity;
                itemFrame.setRotation(Rotation.valueOf(rotation));
            }
        }
    }

    public boolean itemframeHasItem(Entity entity) {
        if (entity instanceof ItemFrame) {
            ItemFrame itemFrame = (ItemFrame) entity;
            ItemStack frame = itemFrame.getItem();

            if (frame != null) {
                return true;
            }
        }

        return false;
    }

    public boolean itemframeHasItem(Entity entity, ItemStack itemStack) {
        if (entity instanceof ItemFrame) {
            ItemFrame itemFrame = (ItemFrame) entity;
            ItemStack frame = itemFrame.getItem();

            if (frame != null) {
                return frame.getTypeId() == itemStack.getTypeId() && frame.getDurability() == itemStack.getDurability();
            }
        }

        return false;
    }

    public void takeItemFromHand(Player player) {
        if (player.getInventory().getItemInMainHand().getAmount() > 1) {
            player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
    }

    public void addPotionEffect(Player player, String type, int duration, int level) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.getByName(type), duration, level));
    }

    public void removePotionEffect(Player player, String type) {
        player.removePotionEffect(PotionEffectType.getByName(type));
    }

    public void clearPotionEffects(Player player) {
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
    }

    public boolean hasPotionEffect(Player player, String type) {
        return player.hasPotionEffect(PotionEffectType.getByName(type));
    }

    public void showParticlesAll(String type, Location location, float offsetX, float offsetY, float offsetZ, float speed, int amount) {
        ParticleEffect.valueOf(type).send(Bukkit.getOnlinePlayers(), location, offsetX, offsetY, offsetZ, speed, amount);
    }

    public void showParticles(String type, Player player, Location location, float offsetX, float offsetY, float offsetZ, float speed, int amount) {
        List<Player> players = new ArrayList<>();
        players.add(player);

        ParticleEffect.valueOf(type).send(players, location, offsetX, offsetY, offsetZ, speed, amount);
    }

    public void setBlockAtLocation(Location location, String type, int data) {
        location.getWorld().getBlockAt(location).setType(Material.valueOf(type));
        location.getWorld().getBlockAt(location).setData((byte) data);
    }

    public void setBlockAtLocation(String world, int x, int y, int z, int id, int data) {
        Location location = new Location(Bukkit.getWorld(world), x, y, z);

        location.getWorld().getBlockAt(location).setTypeId(id);
        location.getWorld().getBlockAt(location).setData((byte) data);
    }

    public void sendConsoleMessage(String message) {
        System.out.println(message);
    }

    public Inventory createInventory(Player player, String type, String title) {
        return Bukkit.createInventory(player, InventoryType.valueOf(type), title);
    }

    public Inventory createInventory(Player player, String title, int size) {
        return Bukkit.createInventory(player, size, title);
    }

    public ItemStack createItemStack(int id, int amount, int data) {
        return new ItemStack(id, amount, (short) data);
    }

    public ItemStack createItemStackWithMeta(int id, int amount, int data, String display, String lore) {
        ItemStack itemStack = new ItemStack(id, amount, (short) data);

        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(display);
        itemMeta.setLore(Arrays.asList(lore.split("\\n")));

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public ItemStack setItemStackMeta(ItemStack itemStack, String display, String lore) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(display);
        itemMeta.setLore(Arrays.asList(lore.split("\\n")));

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    public ItemStack getPlayerHead(String name) {
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);

        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skullMeta.setOwner(name);
        skull.setItemMeta(skullMeta);

        return skull;
    }

    public String getItemDisplay(ItemStack itemStack) {
        String name = null;

        if (itemStack.hasItemMeta()) {
            ItemMeta itemMeta = itemStack.getItemMeta();

            if (itemMeta.hasDisplayName()) {
                name = itemMeta.getDisplayName();
            }
        }

        return name;
    }

    public String getItemLore(ItemStack itemStack) {
        String lore = "";

        if (itemStack.hasItemMeta()) {
            ItemMeta itemMeta = itemStack.getItemMeta();

            if (itemMeta.hasLore()) {
                for (String s : itemMeta.getLore()) {
                    lore = lore + s;
                }
            }
        }

        return lore;
    }

    public int getItemWorth(ItemStack itemStack) {
        return new MoneyUtil().getItemWorth(itemStack);
    }

    public void addInvBills(Player player, int amount) {
        new MoneyUtil().giveBills(player, amount);
    }

    public void takeInvMoney(Player player, int amount) {
        new MoneyUtil().takeMoney(player, amount);
    }

    public int getInvMoney(Player player) {
        return new MoneyUtil().getInvMoney(player);
    }

    public Merchant createTrade(String title) {
        Merchant merchant = Main.instance.merchantAPI.newMerchant(title);
        merchant.setTitle(title, false);

        return merchant;
    }

    public void addTrade(Merchant merchant, ItemStack first, ItemStack result) {
        first = Main.instance.renamerManager.renameItem(first);
        result = Main.instance.renamerManager.renameItem(result);

        merchant.addOffer(Main.instance.merchantAPI.newOffer(result, first));
    }

    public void addTrade(Merchant merchant, ItemStack first, ItemStack second, ItemStack result) {
        first = Main.instance.renamerManager.renameItem(first);
        second = Main.instance.renamerManager.renameItem(second);
        result = Main.instance.renamerManager.renameItem(result);

        merchant.addOffer(Main.instance.merchantAPI.newOffer(result, first, second));
    }

    public void openTrade(Player player, Merchant merchant) {
        merchant.addCustomer(player);
    }

    public boolean isDay(Player player) {
        long time = player.getWorld().getTime();

        return time < 12300 || time > 23850;
    }

    public boolean isInRegion(Player player, String regionName) {
        boolean isInRegion = false;

        ApplicableRegionSet applicableRegions = Main.instance.worldGuardPlugin.getRegionManager(player.getWorld()).getApplicableRegions(player.getLocation());
        for (ProtectedRegion region : applicableRegions) {
            if (region.getId().equals(regionName)) {
                isInRegion = true;
            }
        }

        return isInRegion;
    }

    public boolean isInRegion(Entity entity, String regionName) {
        boolean isInRegion = false;

        ApplicableRegionSet applicableRegions = Main.instance.worldGuardPlugin.getRegionManager(entity.getWorld()).getApplicableRegions(entity.getLocation());
        for (ProtectedRegion region : applicableRegions) {
            if (region.getId().equals(regionName)) {
                isInRegion = true;
            }
        }

        return isInRegion;
    }

    public List<String> getApplicableRegions(Player player) {
        List<String> regions = new ArrayList<>();

        ApplicableRegionSet applicableRegions = Main.instance.worldGuardPlugin.getRegionManager(player.getWorld()).getApplicableRegions(player.getLocation());
        for (ProtectedRegion region : applicableRegions) {
            regions.add(region.getId());
        }

        return regions;
    }

    public List<Entity> createMythicEntity(String name, int amount, String world, int x, int y, int z) {
        List<Entity> entities = new ArrayList<>();

        try {
            for (int i = 1; i < amount + 1; i++) {
                Entity entity = Main.instance.mythicAPI.spawnMythicMob(Main.instance.mythicAPI.getMythicMob(name), new Location(Bukkit.getWorld(world), x, y, z));

                entities.add(entity);
            }
        } catch (InvalidMobTypeException e) {
            e.printStackTrace();
        }

        return entities;
    }

    public Entity createMythicEntity(String name, String world, int x, int y, int z) {
        Entity entity = null;

        try {
            entity = Main.instance.mythicAPI.spawnMythicMob(Main.instance.mythicAPI.getMythicMob(name), new Location(Bukkit.getWorld(world), x, y, z));
        } catch (InvalidMobTypeException e) {
            e.printStackTrace();
        }

        return entity;
    }


    public void hideEntityForOthers(Player player, Entity entity) {
        Main.instance.entityManager.entityHider.hideEntity(entity, player);
    }

    public void showEntity(Player player, Entity entity) {
        Main.instance.entityManager.entityHider.showEntity(player, entity);
    }

    public String getMMInternalName(Entity entity) {
        return Main.instance.mythicAPI.getMythicMobInstance(entity).getType().getInternalName();
    }

    public void setMMTarget(Entity entity, Player target) {
        ThreatTables.taunt(entity, target);
    }

    public void castMMSkill(Entity entity, String skill) {
        Main.instance.mythicAPI.castSkill(entity, skill);
    }

    public void castMMSkill(Player player, Entity entity, String skill) {
        Collection<Entity> eTargets = new ArrayList<>();
        eTargets.add(player);

        Main.instance.mythicAPI.castSkill(entity, skill, player, player.getLocation(), eTargets, null, 1.0F);
    }

    public void disguiseEntity(Entity entity, String type, String name) {
        type = type.toUpperCase();

        switch (type) {
            case "PLAYER":
                PlayerDisguise playerDisguise = new PlayerDisguise(name);
                DisguiseAPI.disguiseToAll(entity, playerDisguise);
                break;

            case "MOB":
                MobDisguise mobDisguise = new MobDisguise(DisguiseType.valueOf(name));
                DisguiseAPI.disguiseToAll(entity, mobDisguise);
                break;

            case "MISC":
                MiscDisguise miscDisguise = new MiscDisguise(DisguiseType.valueOf(name));
                DisguiseAPI.disguiseToAll(entity, miscDisguise);
                break;

            case "FALLING_BLOCK":
                String[] data = name.split(" ");

                int id = Integer.valueOf(data[0]);
                int dura = Integer.valueOf(data[1]);

                MiscDisguise fallingBlock = new MiscDisguise(DisguiseType.FALLING_BLOCK, id, dura);
                DisguiseAPI.disguiseToAll(entity, fallingBlock);
                break;

            case "REMOVE":
                DisguiseAPI.undisguiseToAll(entity);
                break;
        }
    }

    public boolean isOnBlock(Player player, String type) {
        Location loc = player.getPlayer().getLocation();
        loc.setY(loc.getY() - 1);

        return loc.getWorld().getBlockAt(loc).getType().toString().equals(type);
    }

    public List<Player> getParty(Player player) {
        PartyAPI partyAPI = new PartyAPI();
        PartyObject party = partyAPI.getParty(player);
        List<Player> players = new ArrayList<>();

        if (party == null) {
            players.add(player);
            return players;
        }

        return party.getMembers();
    }

    public List<Player> getParty(Player player, int radius) {
        PartyAPI partyAPI = new PartyAPI();
        PartyObject party = partyAPI.getParty(player);
        List<Player> players = new ArrayList<>();

        if (party == null) {
            players.add(player);
            return players;
        }

        for (Player members : party.getMembers()) {
            if (player.getLocation().distance(members.getLocation()) <= radius) {
                players.add(members);
            }
        }

        return players;
    }

    public List<Location> getRandomLocations(Location origin, int radius, int amount, boolean safe) {
        List<Location> locations = new ArrayList<>();

        while (amount != 0) {
            Location location = new Location(origin.getWorld(), 0, 0, 0);

            location.setX(origin.getX() + Math.random() * radius * 2 - radius);
            location.setZ(origin.getZ() + Math.random() * radius * 2 - radius);

            if (safe) {
                location.setY(origin.getWorld().getHighestBlockAt(location.getBlockX(), location.getBlockZ()).getY());
            } else {
                location.setY(origin.getY() + Math.random() * radius * 2 - radius);
            }

            if (!location.getWorld().getBlockAt(location).getType().isSolid()) {
                locations.add(location);
                amount--;
            }
        }

        return locations;
    }

    public void playCustomSound(Player player, String name, String category, int x, int y, int z, float volume, float pitch) {
        PacketPlayOutCustomSoundEffect packet = new PacketPlayOutCustomSoundEffect(name, SoundCategory.valueOf(category), x, y, z, volume, pitch);

        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    public void depositMoney(Player player, double amount) {
        Main.instance.economy.depositPlayer(player.getName(), amount);
    }

    public double getBalance(Player player) {
        return Main.instance.economy.getBalance(player.getName());
    }

    public void withdrawMoney(Player player, double amount) {
        Main.instance.economy.withdrawPlayer(player, amount);
    }

    public void addTokens(Player player, int amount) {
        PointsAPI.modifyPointsToPlayerAsynchronously(player.getName(), amount, PointsAPI.Type.AddPoints);
    }

    public void removeTokens(Player player, int amount) {
        PointsAPI.modifyPointsToPlayerAsynchronously(player.getName(), amount, PointsAPI.Type.RemovePoints);
    }

    public int getTokens(Player player) {
        try {
            return PointsAPI.getPointsForPlayer(player.getName());
        } catch (Exception e) {
            return -1;
        }
    }

    public List<Player> getPlayersInCone(Entity entity, int arc, int range) {
        List<Player> players = new ArrayList<>();
        List<LivingEntity> livingEntities = TargetHelper.getConeTargets((LivingEntity) entity, arc, range);

        for (LivingEntity livingEntity : livingEntities) {
            if (!(livingEntity instanceof Player)) {
                continue;
            }

            if (!TargetHelper.isObstructed(entity.getLocation(), livingEntity.getLocation())) {
                players.add((Player) livingEntity);
            }
        }

        return players;
    }

    public BossBar createBossBar(Player player, String text, String color, String style, int timeout) {
        BossBar bossBar = BossBarAPI.addBar(new TextComponent(text),
                BossBarAPI.Color.valueOf(color),
                BossBarAPI.Style.valueOf(style),
                0.0f);

        bossBar.addPlayer(player);

        return bossBar;
    }

    public BossBar getBossBar(Player player, String style) {
        for (BossBar bossBar : BossBarAPI.getBossBars(player)) {
            if (bossBar.getStyle() == BossBarAPI.Style.valueOf(style)) {
                return bossBar;
            }
        }

        return null;
    }

    public void addBossBarProgress(BossBar bossBar, float amount) {
        float progress = bossBar.getProgress();

        if (progress + amount > 1) {
            bossBar.setProgress(1.0f);
        } else {
            bossBar.setProgress(progress + amount);
        }
    }

    public void removeAllBossBars(Player player) {
        BossBarAPI.removeAllBars(player);
    }

    public void addDetectionToEntity(Entity entity, String script) {
        Main.instance.entityDetection.addEntity(entity, script);
    }

    public String getWeaponTitle(ItemStack item) {
        return new CSUtility().getWeaponTitle(item);
    }

    public void setBlocks(String world, int x1, int y1, int z1, int x2, int y2, int z2, int id, int data) {
        runCommand("credit " + world + " " + x1 + " " + y1 + " " + z1 + " " + x2 + " " + y2 + " " + z2 + " " + Material.getMaterial(id).name() + " " + data);
    }

    public void giveLootbag(Player player) {
        player.getInventory().setItemInOffHand(this.getItem(player, "LootBag", 1, true));
    }

    public boolean hasLootbag(Player player) {
        ItemStack itemStack = player.getInventory().getItemInOffHand();

        if (itemStack != null) {
            return itemStack.getType() == Material.DIAMOND_HOE && itemStack.getDurability() == 497;
        }

        return false;
    }

    public void removeLootbag(Player player) {
        player.getInventory().setItemInOffHand(null);
    }

    public void spawnCar(String world, int x, int y, int z, String owner, String type) {
        if (owner.equals("ADMIN")) {
            owner = "eb44b54e-2f44-4825-a41e-a87fbe288865";
        }

        CarManager.spawnCar(new Location(Bukkit.getWorld(world), x, y, z), owner, VehiclesMain.carTypefromString(type));
    }

    public void spawnBike(String world, int x, int y, int z, String owner, String type) {
        if (owner.equals("ADMIN")) {
            owner = "eb44b54e-2f44-4825-a41e-a87fbe288865";
        }

        BikeManager.spawnBike(new Location(Bukkit.getWorld(world), x, y, z), owner, VehiclesMain.bikeTypefromString(type));
    }

    public int getPlayerFileInt(Player player, String key) {
        if (!FileHandler.fileExists("plugins/CrimeRing/player/" + player.getUniqueId().toString())) {
            return 0;
        }

        FileHandler fileHandler = new FileHandler("plugins/CrimeRing/player/" + player.getUniqueId().toString());

        if (fileHandler.contains(key)) {
            return fileHandler.getInteger(key);
        }

        return 0;
    }

    public String getPlayerFileString(Player player, String key) {
        if (!FileHandler.fileExists("plugins/CrimeRing/player/" + player.getUniqueId().toString())) {
            return null;
        }

        FileHandler fileHandler = new FileHandler("plugins/CrimeRing/player/" + player.getUniqueId().toString());

        if (fileHandler.contains(key)) {
            return fileHandler.getString(key);
        }

        return null;
    }

    public void setPlayerFileString(Player player, String key, String value) {
        if (!FileHandler.fileExists("plugins/CrimeRing/player/" + player.getUniqueId().toString())) {
            return;
        }

        FileHandler fileHandler = new FileHandler("plugins/CrimeRing/player/" + player.getUniqueId().toString());

        fileHandler.set(key, value);
        fileHandler.save();
    }

    public void setPlayerFileInt(Player player, String key, int value) {
        if (!FileHandler.fileExists("plugins/CrimeRing/player/" + player.getUniqueId().toString())) {
            return;
        }

        FileHandler fileHandler = new FileHandler("plugins/CrimeRing/player/" + player.getUniqueId().toString());

        fileHandler.set(key, value);
        fileHandler.save();
    }

    public void addPlayerFileInt(Player player, String key) {
        if (!FileHandler.fileExists("plugins/CrimeRing/player/" + player.getUniqueId().toString())) {
            return;
        }

        FileHandler fileHandler = new FileHandler("plugins/CrimeRing/player/" + player.getUniqueId().toString());

        if (fileHandler.contains(key)) {
            fileHandler.increment(key);
        } else {
            fileHandler.set(key, 0);
        }

        fileHandler.save();
    }

    public World getWorld(String world) {
        return Bukkit.getWorld(world);
    }

    public void audioPlayToPlayer(Player player, String sound) {
        AudioClient.playToPlayer(player, sound);
    }

    public void audioPlayToAll(String sound) {
        AudioClient.playToAll(sound);
    }

    public void audioPlayEffectToPlayer(Player player, String sound) {
        AudioClient.playEffectToPlayer(player, sound);
    }

    public void audioPlayEffectToAll(String sound) {
        AudioClient.playEffectToAll(sound);
    }

    public void audioPlayInArea(String world, int x1, int y1, int z1, int x2, int y2, int z2, String sound) {
        AudioClient.playInArea(world, x1, y1, z1, x2, y2, z1, sound);
    }

    public void playEffectInArea(String world, int x1, int y1, int z1, int x2, int y2, int z2, String sound) {
        AudioClient.playEffectInArea(world, x1, y1, z1, x2, y2, z1, sound);
    }

    public long getTimeDifference(long timestamp) {
        return TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - timestamp);
    }

    public String getDate() {
        return new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date());
    }

    public int getDrugPoints(Player player, String soft, String hard) {
        int points = 0;

        List<String> listSoft = Arrays.asList(soft.split(", "));
        List<String> listHard = Arrays.asList(hard.split(", "));

        for (ItemStack itemStack : player.getInventory().getContents()) {
            if (itemStack == null) {
                continue;
            }

            if (listSoft.contains(itemStack.getTypeId() + ":" + itemStack.getDurability())) {
                points = points + itemStack.getAmount();
                continue;
            }

            if (listHard.contains(itemStack.getTypeId() + ":" + itemStack.getDurability())) {
                points = points + (3 * itemStack.getAmount());
            }
        }

        return points;
    }

    public int getWeaponPoints(Player player, String soft, String hard) {
        int points = 0;

        List<String> listSoft = Arrays.asList(soft.split(", "));
        List<String> listHard = Arrays.asList(hard.split(", "));

        for (ItemStack itemStack : player.getInventory().getContents()) {
            if (itemStack == null) {
                continue;
            }

            if (listSoft.contains(itemStack.getTypeId() + ":" + itemStack.getDurability())) {
                points = points + (15 * itemStack.getAmount());
                continue;
            }

            if (listHard.contains(itemStack.getTypeId() + ":" + itemStack.getDurability())) {
                points = points + (25 * itemStack.getAmount());
            }
        }

        return points;
    }

    public void showConfiscatedItems(Player player, Player cop, String items) {
        List<String> listItems = Arrays.asList(items.split(", "));
        Inventory inventory = createInventory(player, player.getName() + "'s confiscated items", 54);

        for (ItemStack itemStack : player.getInventory().getContents()) {
            if (itemStack == null) {
                continue;
            }

            if (listItems.contains(itemStack.getTypeId() + ":" + itemStack.getDurability())) {
                inventory.addItem(itemStack);
                player.getInventory().remove(itemStack);
            }
        }

        cop.openInventory(inventory);
    }

    public Player getPlayerByName(String name) {
        return Bukkit.getPlayer(name);
    }

    public Location createLocation(String world, int x, int y, int z) {
        return new Location(Bukkit.getWorld(world), x, y, z);
    }

    public Location getShortestLocationFromPlayer(Player player, Location... args) {
        TreeMap<Double, Location> map = new TreeMap<>();

        for (Location location : args) {
            map.put(player.getLocation().distance(location), location);
        }

        return map.firstEntry().getValue();
    }

    public List<ItemStack> getPlayerInv(Player player, boolean trade) {
        List<ItemStack> list = new ArrayList<>();

        if (trade) {
            for (ItemStack itemStack : player.getInventory().getContents()) {
                if (itemStack == null || new MoneyUtil().isMoney(itemStack)) continue;

                itemStack = itemStack.clone();
                if (!list.contains(itemStack)) {
                    itemStack.setAmount(1);
                    list.add(itemStack);
                }
            }
        } else {
            list = Arrays.asList(player.getInventory().getContents());
        }

        return list;
    }


    public ItemStack createMoneyBag(int amount) {
        return createItemStackWithMeta(293, 1, 497, ChatColor.GOLD + "Bag of cash", ChatColor.GRAY + "Click to receive $" + amount);
    }

    public ItemStack createItemStackWithRenamer(int id, int amount, int data) {
        ItemStack itemStack = createItemStack(id, amount, data);

        return Main.instance.renamerManager.renameItem(itemStack);
    }

    public ItemStack createItemStackWithCrackShot(String name) {
        return new CSUtility().generateWeapon(name);
    }
}
