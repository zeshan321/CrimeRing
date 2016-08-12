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


        if (! raidnames.containsKey(raidName))  raidnames.put(raidName, filename);

        if (party == null) {
            if (min == 1) {
                return true;
            } else {
                player.sendMessage(ChatColor.RED + raidName + " requires a party of " + min + " players!");
                return false;
            }
        } else {
            if (!party.getOwner().getName().equals(player.getName())) {
                player.sendMessage(ChatColor.RED + "Only the party leader can start this raid!");
                return false;
            }

            if (party.size() < min || party.size() > max) {
                player.sendMessage(ChatColor.RED + raidName + " requires a minimum of " + min + " players and a maximum of " + max + " players!");
            } else {
                return true;
            }
        }

        return false;
    }

    public void startRaid(Player player, String filename) {
        BukkitTask task = Bukkit.getScheduler().runTaskAsynchronously(Main.instance, () -> {
            raids.put(player, filename);

            if (!isNext(player, filename)) {
                player.sendMessage(ChatColor.GOLD + "You have been added to the queue.");
            }

            while (!isNext(player, filename)) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            FileHandler fileHandler = new FileHandler("plugins/CrimeRing/raids/" + filename + ".yml");

            sendMessage(player, ChatColor.RED + "" + ChatColor.BOLD + "Raid: " + fileHandler.getString("info.name"));
            sendMessage(player, ChatColor.GOLD + fileHandler.getString("info.name") + " will be starting in " + ChatColor.GREEN + "2 minutes" + ChatColor.GOLD + "!");
            sendMessage(player, ChatColor.GOLD + "If you are not near the raid NPC you will be kicked from the raid!");

            BukkitTask task1 = new BukkitRunnable() {
                @Override
                public void run() {
                    PartyAPI partyAPI = new PartyAPI();
                    PartyObject party = partyAPI.getParty(player);
                    double radius = 15;

                    Location loc = new Location(Bukkit.getWorld(fileHandler.getString("info.worlds")), fileHandler.getInteger("info.xs"), fileHandler.getInteger("info.ys"), fileHandler.getInteger("info.zs"));
                    if (party == null) {
                        if (!(player.getLocation().distance(loc) <= radius)) {
                            sendMessage(player, ChatColor.RED + "You have been kicked from the raid!");
                            cancelRaid(player);
                            return;
                        }
                    } else {
                        for (Player players: party.getMembers()) {
                            if (!(players.getLocation().distance(loc) <= radius)) {
                                sendMessage(player, ChatColor.RED + "You have been kicked from the raid!");
                                cancelRaid(player);
                                return;
                            }
                        }
                    }

                    if (canStartRaid(player, filename)) {
                        player.sendMessage("Starting...");
                    } else {
                        sendMessage(player, ChatColor.RED + "You have been kicked from the raid!");
                        cancelRaid(player);
                    }
                }

            }.runTaskLater(Main.instance, 2400);

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
