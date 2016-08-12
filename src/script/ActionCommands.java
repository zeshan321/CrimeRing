package script;

import com.zeshanaslam.crimering.FileHandler;
import com.zeshanaslam.crimering.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

public class ActionCommands implements Listener, CommandExecutor {

    public static HashMap<String, CopyObject> clicks = new HashMap<>();
    private final Main plugin;

    public ActionCommands(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if (commandLabel.equalsIgnoreCase("action") && sender.isOp()) {
            if (args.length == 0) {
                sender.sendMessage(ChatColor.GOLD + "/action copy <blocks | NPC> <name> <name>");
                sender.sendMessage(ChatColor.GOLD + "/action loc");
                return false;
            }

            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "You have to be in-game to run this command.");
            }

            Player player = (Player) sender;
            if (args[0].equalsIgnoreCase("copy")) {
                if (args.length > 3) {
                    try {
                        copyFile(new File("plugins/CrimeRing/scripts/" + args[2] + ".yml"), new File("plugins/CrimeRing/scripts/" + args[3] + ".yml"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    clicks.put(player.getName(), new CopyObject(args[1], args[2], args[3]));

                    player.sendMessage(ChatColor.GOLD + "Click a block to attach Action Script.");
                } else {
                    clicks.put(player.getName(), new CopyObject(args[1], args[2], "null"));

                    player.sendMessage(ChatColor.GOLD + "Click a block to attach Action Script.");
                }
            }

            if (args[0].equalsIgnoreCase("loc")) {
                System.out.println("CR.teleport(player, \"" + player.getWorld().getName() +"\", " + player.getLocation().getBlockX() + ", " + player.getLocation().getBlockY() + ", " + player.getLocation().getBlockZ() + ", " + player.getLocation().getYaw() + ", " + player.getLocation().getPitch() + ");");
                player.sendMessage(ChatColor.GOLD + "Sent to console!");
            }
        }
        return true;
    }

    private void copyFile(File from, File to) throws IOException {
        to.createNewFile();

        try (FileChannel in = new FileInputStream(from).getChannel(); FileChannel out = new FileOutputStream(to).getChannel()) {
            out.transferFrom(in, 0, in.size());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getHand() == EquipmentSlot.OFF_HAND) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (clicks.containsKey(player.getName())) {
            CopyObject copyObject = clicks.get(player.getName());

            int x = event.getClickedBlock().getLocation().getBlockX();
            int y = event.getClickedBlock().getLocation().getBlockY();
            int z = event.getClickedBlock().getLocation().getBlockZ();
            String world = player.getWorld().getName();

            if (copyObject.type.equalsIgnoreCase("blocks")) {
                FileHandler imports = new FileHandler("plugins/CrimeRing/scripts.yml");

                if (imports.contains(x + " " + y + " " + z + " " + world)) {
                    player.sendMessage(ChatColor.RED + "This block is already attached to an Action Script!");
                    return;
                }

                imports.set(x + " " + y + " " + z + " " + world + ".type", "BLOCKS");

                if (copyObject.name2.equals("null")) {
                    imports.set(x + " " + y + " " + z + " " + world + ".dir", copyObject.name1 + ".yml");

                    try {
                        Main.instance.scriptsManager.scriptData.put(x + " " + y + " " + z + " " + world,
                                new ScriptObject(x + " " + y + " " + z + " " + world
                                        , copyObject.name1 + ".yml",
                                        String.join("\n", Files.readAllLines(Paths.get("plugins/CrimeRing/scripts/" + File.separator + copyObject.name1 + ".yml")))));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    imports.set(x + " " + y + " " + z + " " + world + ".dir", copyObject.name2 + ".yml");

                    try {
                        Main.instance.scriptsManager.scriptData.put(x + " " + y + " " + z + " " + world,
                                new ScriptObject(x + " " + y + " " + z + " " + world
                                        , copyObject.name1 + ".yml",
                                        String.join("\n", Files.readAllLines(Paths.get("plugins/CrimeRing/scripts/" + File.separator + copyObject.name2 + ".yml")))));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                imports.save();

                clicks.remove(player.getName());
                player.sendMessage(ChatColor.GOLD + "Action Script set!");
            }
        }
    }
}
