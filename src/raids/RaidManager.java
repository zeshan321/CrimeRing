package raids;

import com.zeshanaslam.crimering.FileHandler;
import com.zeshanaslam.crimering.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import script.ActionDefaults;
import script.ScriptObject;
import utils.ItemUtils;
import utils.MessageUtil;

import javax.script.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RaidManager {

    public List<RaidObject> raids = new ArrayList<>();
    public List<PartyObject> parties = new ArrayList<>();
    public List<InviteObject> invites = new ArrayList<>();
    public HashMap<String, String> raidNames = new HashMap<>();

    public void openRaidMenu(Player player, String filename) {
        FileHandler fileHandler = new FileHandler("plugins/CrimeRing/raids/" + filename + ".yml");
        Inventory inventory = Bukkit.createInventory(null, 18, ChatColor.RED + "Raid: " + fileHandler.getString("info.name"));

        if (playersQueue(filename) == 0) {
            inventory.setItem(1, new ItemUtils().createItem(Material.DIAMOND_HOE, ChatColor.RED + "Start Raid", ChatColor.GOLD + "Click to start raid.", 1, 90));
        }

        if (playersQueue(filename) > 0) {
            inventory.setItem(1, new ItemUtils().createItem(Material.DIAMOND_HOE, ChatColor.RED + "Queue Raid", ChatColor.GOLD + "Click to Queue for the raid./n" + ChatColor.GOLD + "Number: " + ChatColor.WHITE + playersQueue(filename)));
        }

        inventory.setItem(6, new ItemUtils().createItem(Material.SHIELD, ChatColor.RED + "Cancel Raid", ChatColor.GOLD + "Click to cancel raid."));
        inventory.setItem(17, new ItemUtils().createItem(Material.BARRIER, ChatColor.RED + "Exit", ChatColor.GOLD + "Click to exit."));

        player.openInventory(inventory);
    }

    public int playersQueue(String filename) {
        int queue = 0;

        for (RaidObject raidObject : raids) {
            if (raidObject.raidID.equals(filename)) {
                queue++;
            }
        }

        return queue;
    }

    public boolean canStartRaid(Player player, String filename) {
        FileHandler fileHandler = new FileHandler("plugins/CrimeRing/raids/" + filename + ".yml");

        PartyAPI partyAPI = new PartyAPI();
        PartyObject party = partyAPI.getParty(player);

        int min = fileHandler.getInteger("info.min");
        int max = fileHandler.getInteger("info.max");
        String raidName = fileHandler.getString("info.name");

        if (!raidNames.containsKey(raidName)) raidNames.put(raidName, filename);

        if (party == null) {
            if (min == 1) {
                return true;
            } else {
                String[] message = ChatColor.translateAlternateColorCodes('&', Main.instance.getConfig().getString("Raids.Not-enough-players")).replace("%raid%", raidName).replace("%min%", String.valueOf(min)).replace("%max%", String.valueOf(max)).split("/n");
                player.sendMessage(message);
                return false;
            }
        } else {
            if (party.getOwner() != player) {
                String[] message = ChatColor.translateAlternateColorCodes('&', Main.instance.getConfig().getString("Raids.Not-leader")).replace("%raid%", raidName).replace("%min%", String.valueOf(min)).replace("%max%", String.valueOf(max)).split("/n");
                player.sendMessage(message);
                return false;
            }

            if (party.size() < min || party.size() > max) {
                String[] message = ChatColor.translateAlternateColorCodes('&', Main.instance.getConfig().getString("Raids.Party-large-or-small")).replace("%raid%", raidName).replace("%min%", String.valueOf(min)).replace("%max%", String.valueOf(max)).split("/n");
                player.sendMessage(message);
            } else {
                return true;
            }
        }

        return false;
    }

    public boolean canStartRaid(RaidObject raidObject, String filename) {
        FileHandler fileHandler = new FileHandler("plugins/CrimeRing/raids/" + filename + ".yml");
        int min = fileHandler.getInteger("info.min");
        int max = fileHandler.getInteger("info.max");
        String raidName = fileHandler.getString("info.name");

        if (!raidNames.containsKey(raidName)) raidNames.put(raidName, filename);

        if (raidObject.members.size() < min || raidObject.members.size() > max) {
            String[] message = ChatColor.translateAlternateColorCodes('&', Main.instance.getConfig().getString("Raids.Party-large-or-small")).replace("%raid%", raidName).replace("%min%", String.valueOf(min)).replace("%max%", String.valueOf(max)).split("/n");
            sendMessage(raidObject, message);
        } else {
            return true;
        }

        return false;
    }

    public void startRaid(String UUID, String filename) {
        BukkitTask task = Bukkit.getScheduler().runTaskAsynchronously(Main.instance, () -> {

            FileHandler fileHandler = new FileHandler("plugins/CrimeRing/raids/" + filename + ".yml");
            if (!isNext(getRaidByUUID(UUID))) {
                String[] message = ChatColor.translateAlternateColorCodes('&', Main.instance.getConfig().getString("Raids.Added-queue")).replace("%raid%", fileHandler.getString("info.name")).replace("%min%", fileHandler.getString("info.min")).replace("%max%", fileHandler.getString("info.max")).split("/n");
                sendMessage(getRaidByUUID(UUID), message);
            }

            while (!isNext(getRaidByUUID(UUID))) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            String[] message = ChatColor.translateAlternateColorCodes('&', Main.instance.getConfig().getString("Raids.Start")).replace("%raid%", fileHandler.getString("info.name")).replace("%min%", fileHandler.getString("info.min")).replace("%max%", fileHandler.getString("info.max")).split("/n");
            sendMessage(getRaidByUUID(UUID), message);

            final int[] starts = {Main.instance.getConfig().getInt("Raids.Time-wait")};

            BukkitTask task1 = new BukkitRunnable() {
                @Override
                public void run() {
                    RaidObject raidObject = getRaidByUUID(UUID);

                    if (starts[0] == 0) {
                        double radius = Main.instance.getConfig().getDouble("Raids.Radius");

                        Location loc = new Location(Bukkit.getWorld(fileHandler.getString("info.worlds")), fileHandler.getInteger("info.xs"), fileHandler.getInteger("info.ys"), fileHandler.getInteger("info.zs"), fileHandler.getInteger("info.yaws"), fileHandler.getInteger("info.pitchs"));

                        for (Player players : raidObject.members) {
                            if (!(players.getLocation().distance(loc) <= radius)) {
                                String[] kick = ChatColor.translateAlternateColorCodes('&', Main.instance.getConfig().getString("Raids.Kick")).replace("%raid%", fileHandler.getString("info.name")).replace("%min%", fileHandler.getString("info.min")).replace("%max%", fileHandler.getString("info.max")).split("/n");
                                sendMessage(raidObject, kick);

                                cancelRaid(raidObject);
                                return;
                            }
                        }

                        if (canStartRaid(raidObject, filename)) {
                            runScript(raidObject.members.get(0), filename);

                            for (Player players : raidObject.members) {
                                players.teleport(new Location(Bukkit.getWorld(fileHandler.getString("info.world")), fileHandler.getInteger("info.x"), fileHandler.getInteger("info.y"), fileHandler.getInteger("info.z"), fileHandler.getInteger("info.yaw"), fileHandler.getInteger("info.pitch")));

                            }
                        } else {
                            String[] kick = ChatColor.translateAlternateColorCodes('&', Main.instance.getConfig().getString("Raids.Kick")).replace("%raid%", fileHandler.getString("info.name")).replace("%min%", fileHandler.getString("info.min")).replace("%max%", fileHandler.getString("info.max")).split("/n");
                            sendMessage(raidObject, kick);
                            cancelRaid(raidObject);
                        }

                        this.cancel();
                    } else {
                        starts[0]--;

                        for (Player players : raidObject.members) {
                            new MessageUtil().sendActionBar(players, ChatColor.GOLD + "The raid will be starting in " + ChatColor.RED + starts[0] + " seconds" + ChatColor.GOLD + "!");
                        }
                    }
                }
            }.runTaskTimer(Main.instance, 0L, 20L);

            getRaidByUUID(UUID).tasks.add(task1.getTaskId());
        });

        getRaidByUUID(UUID).tasks.add(task.getTaskId());
    }

    public void cancelRaid(Player player) {
        RaidObject raidObject = getRaid(player);

        if (raidObject == null) {
            return;
        }

        // Stop threaded tasks
        for (Integer integer : raidObject.tasks) {
            Bukkit.getScheduler().cancelTask(integer);
        }

        raidObject.clearLists();
        raids.remove(raidObject);
    }

    public void cancelRaid(RaidObject raidObject) {
        if (raidObject == null) {
            return;
        }

        // Stop threaded tasks
        for (Integer integer : raidObject.tasks) {
            Bukkit.getScheduler().cancelTask(integer);
        }

        raidObject.clearLists();
        raids.remove(raidObject);
    }

    public void sendMessage(RaidObject raidObject, String message) {
        for (Player players : raidObject.members) {
            players.sendMessage(message);
        }
    }

    public void sendMessage(RaidObject raidObject, String[] message) {
        for (Player players : raidObject.members) {
            players.sendMessage(message);
        }
    }

    public boolean isNext(RaidObject raidObject) {
        if (raids.isEmpty()) {
            return true;
        }

        if (raids.get(0) == raidObject) {
            return true;
        }

        return false;
    }

    public void runScript(Player player, String name) {
        if (Main.instance.scriptsManager.contains("raid-" + name)) {
            ScriptObject scriptObject = Main.instance.scriptsManager.getObject("raid-" + name);

            try {
                ScriptEngine engine = Main.instance.scriptsManager.engine;
                CompiledScript compiledScript = scriptObject.script;

                // Objects
                Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
                bindings.put("player", player);
                bindings.put("CR", new ActionDefaults("raid-" + name, engine));

                compiledScript.eval(bindings);
            } catch (ScriptException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isInRaid(Player player) {
        RaidObject raidObject = getRaid(player);

        if (raidObject == null) {
            return false;
        }

        return true;
    }

    public RaidObject getRaid(Player player) {
        for (RaidObject raidObject1 : raids) {
            if (raidObject1.members.contains(player)) {
                return raidObject1;
            }
        }

        return null;
    }

    public RaidObject getRaidByUUID(String UUID) {
        for (RaidObject raidObject1 : raids) {
            if (raidObject1.id.equals(UUID)) {
                return raidObject1;
            }
        }

        return null;
    }
}