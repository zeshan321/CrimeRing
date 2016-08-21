package resourcepack;

import com.zeshanaslam.crimering.Main;
import org.apache.commons.io.FileUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class ResourceCommand implements CommandExecutor {

    private final Main plugin;

    public ResourceCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if (commandLabel.equalsIgnoreCase("crrpupdate")) {
            if (sender.isOp()) {
                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                    sender.sendMessage(ChatColor.GOLD + "Downloading resource pack...");

                    String randomUUID = UUID.randomUUID().toString();
                    try {
                        FileUtils.copyURLToFile(new URL(plugin.getConfig().getString("Resource-pack")), new File(randomUUID + ".zip"));
                    } catch (IOException e) {
                        sender.sendMessage(ChatColor.RED + "Unable to download resource pack!");
                        return;
                    }

                    sender.sendMessage(ChatColor.GOLD + "Download complete.");
                    sender.sendMessage(ChatColor.GOLD + "Generating hash...");

                    String hash = null;
                    try {
                        hash = getSHA1(new File(randomUUID + ".zip")).toLowerCase();
                    } catch (Exception e) {
                        sender.sendMessage(ChatColor.RED + "Unable to generate hash!");
                        return;
                    }

                    sender.sendMessage(ChatColor.GOLD + "Generating hash complete: " + hash);

                    String finalHash = hash;
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        plugin.getConfig().set("Resource-hash", finalHash);
                        plugin.saveConfig();
                        plugin.reloadConfig();
                    });

                    new File(randomUUID + ".zip").delete();
                    sender.sendMessage(ChatColor.GOLD + "Reconnect to the server.");
                });
            }
        }
        return false;
    }

    private String getSHA1(File file) throws IOException, NoSuchAlgorithmException {

        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        try (InputStream input = new FileInputStream(file)) {

            byte[] buffer = new byte[8192];
            int len = input.read(buffer);

            while (len != -1) {
                sha1.update(buffer, 0, len);
                len = input.read(buffer);
            }

            return new HexBinaryAdapter().marshal(sha1.digest());
        }
    }
}
