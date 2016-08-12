package raids;

import com.zeshanaslam.crimering.FileHandler;
import com.zeshanaslam.crimering.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitTask;
import utils.ItemUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class RaidManager {

    public ArrayList<PartyObject> parties = new ArrayList();
    public ArrayList<InviteObject> invites = new ArrayList();
    public HashMap<Player, String> raids = new HashMap();
    public HashMap<Player, Integer> tasks = new HashMap();
    public HashMap<String, String> raidnames = new HashMap();

    public void openRaidMenu(Player player, String filename) {
        FileHandler fileHandler = new FileHandler("plugins/CrimeRing/raids/" + filename + ".yml");
        Inventory inventory = Bukkit.createInventory(null, 18, ChatColor.RED + "Raid: " + fileHandler.getString("info.name"));

        if (playersQueue(filename) == 0) {
            inventory.setItem(1, new ItemUtils().createItem(Material.DIAMOND_HOE, ChatColor.RED + "Start Raid", ChatColor.GOLD + "Click to start raid."));
        }

        if (playersQueue(filename) > 0) {
            inventory.setItem(1, new ItemUtils().createItem(Material.DIAMOND_HOE, ChatColor.RED + "Queue Raid", ChatColor.GOLD + "Click to Queue for the raid./n" + ChatColor.GOLD + "Number: " + ChatColor.WHITE + playersQueue(filename)));
        }

        inventory.setItem(6, new ItemUtils().createItem(Material.SHIELD, ChatColor.RED + "Cancel Raid", ChatColor.GOLD + "Click to cancel raid."));
        inventory.setItem(17, new ItemUtils().createItem(Material.REDSTONE_LAMP_OFF, ChatColor.RED + "Exit", ChatColor.GOLD + "Click to exit."));

        player.openInventory(inventory);
    }

    public int playersQueue(String filename) {
        int queue = 0;

        for (Player players : raids.keySet()) {
            if (raids.get(players).equals(filename)) {
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


        if (!raidnames.containsKey(raidName)) raidnames.put(raidName, filename);

        if (party == null) {
            if (min == 1) {
                return true;
            } else {
                String[] message = ChatColor.translateAlternateColorCodes('&', Main.instance.getConfig().getString("Raids.Not-enough-players")).replace("%raid%", raidName).replace("%min%", String.valueOf(min)).replace("%max%", String.valueOf(max)).split("/n");
                player.sendMessage(message);
                return false;
            }
        } else {
            if (!party.getOwner().getName().equals(player.getName())) {
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

    public void startRaid(Player player, String filename) {
        BukkitTask task = Bukkit.getScheduler().runTaskAsynchronously(Main.instance, () -> {
            raids.put(player, filename);

            FileHandler fileHandler = new FileHandler("plugins/CrimeRing/raids/" + filename + ".yml");
            if (!isNext(player, filename)) {
                String[] message = ChatColor.translateAlternateColorCodes('&', Main.instance.getConfig().getString("Raids.Added-queue")).replace("%raid%", fileHandler.getString("info.name")).replace("%min%", fileHandler.getString("info.min")).replace("%max%", fileHandler.getString("info.max")).split("/n");
                player.sendMessage(message);
            }

            while (!isNext(player, filename)) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            String[] message = ChatColor.translateAlternateColorCodes('&', Main.instance.getConfig().getString("Raids.Start")).replace("%raid%", fileHandler.getString("info.name")).replace("%min%", fileHandler.getString("info.min")).replace("%max%", fileHandler.getString("info.max")).split("/n");
            sendMessage(player, message);

            BukkitTask task1 = Bukkit.getScheduler().runTaskLaterAsynchronously(Main.instance, () -> {
                PartyAPI partyAPI = new PartyAPI();
                PartyObject party = partyAPI.getParty(player);
                double radius = Main.instance.getConfig().getDouble("Raids.Radius");

                Location loc = new Location(Bukkit.getWorld(fileHandler.getString("info.worlds")), fileHandler.getInteger("info.xs"), fileHandler.getInteger("info.ys"), fileHandler.getInteger("info.zs"));
                if (party == null) {
                    if (!(player.getLocation().distance(loc) <= radius)) {
                        String[] kick = ChatColor.translateAlternateColorCodes('&', Main.instance.getConfig().getString("Raids.Kick")).replace("%raid%", fileHandler.getString("info.name")).replace("%min%", fileHandler.getString("info.min")).replace("%max%", fileHandler.getString("info.max")).split("/n");
                        sendMessage(player, kick);
                        cancelRaid(player);
                        return;
                    }
                } else {
                    for (Player players : party.getMembers()) {
                        if (!(players.getLocation().distance(loc) <= radius)) {
                            String[] kick = ChatColor.translateAlternateColorCodes('&', Main.instance.getConfig().getString("Raids.Kick")).replace("%raid%", fileHandler.getString("info.name")).replace("%min%", fileHandler.getString("info.min")).replace("%max%", fileHandler.getString("info.max")).split("/n");
                            sendMessage(player, kick);
                            cancelRaid(player);
                            return;
                        }
                    }
                }

                if (canStartRaid(player, filename)) {
                    if (party == null) {
                        player.teleport(new Location(Bukkit.getWorld(fileHandler.getString("info.world")), fileHandler.getInteger("info.x"), fileHandler.getInteger("info.y"), fileHandler.getInteger("info.z"), fileHandler.getInteger("info.yaw"), fileHandler.getInteger("info.pitch")));
                    } else {
                        for (Player players : party.getMembers()) {
                            players.teleport(new Location(Bukkit.getWorld(fileHandler.getString("info.world")), fileHandler.getInteger("info.x"), fileHandler.getInteger("info.y"), fileHandler.getInteger("info.z"), fileHandler.getInteger("info.yaw"), fileHandler.getInteger("info.pitch")));
                        }
                    }
                } else {
                    String[] kick = ChatColor.translateAlternateColorCodes('&', Main.instance.getConfig().getString("Raids.Kick")).replace("%raid%", fileHandler.getString("info.name")).replace("%min%", fileHandler.getString("info.min")).replace("%max%", fileHandler.getString("info.max")).split("/n");
                    sendMessage(player, kick);
                    cancelRaid(player);
                }
            }, Main.instance.getConfig().getInt("Raids.Time-wait") * 20);

            tasks.put(player, task1.getTaskId());
        });

        tasks.put(player, task.getTaskId());
    }

    public void cancelRaid(Player player) {
        Bukkit.getScheduler().cancelTask(tasks.get(player));

        raids.remove(player);
        tasks.remove(player);
    }

    public void sendMessage(Player player, String message) {
        PartyAPI partyAPI = new PartyAPI();
        PartyObject party = partyAPI.getParty(player);

        if (party == null) {
            player.sendMessage(message);
        } else {
            party.sendMessage(message);
        }
    }

    public void sendMessage(Player player, String[] message) {
        PartyAPI partyAPI = new PartyAPI();
        PartyObject party = partyAPI.getParty(player);

        if (party == null) {
            player.sendMessage(message);
        } else {
            for (String s : message)
                party.sendMessage(s);
        }
    }

    public boolean isNext(Player player, String filename) {
        if (raids.isEmpty()) {
            return true;
        }

        List<Player> tempList = raids.keySet().stream().filter(players -> raids.get(players).equals(filename)).collect(Collectors.toList());
        if (tempList.get(0).getName().equals(player.getName())) {
            return true;
        }

        return false;
    }
}
