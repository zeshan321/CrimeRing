package events;

import com.zeshanaslam.crimering.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.List;


public class BasicEvents implements Listener {

    private final Main plugin;
    private List<Block> lampBlocks = new ArrayList<Block>();

    public BasicEvents(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPluginsView(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String cmd = event.getMessage().toLowerCase();

        if (cmd.equalsIgnoreCase("/Bukkit:pl") || cmd.equalsIgnoreCase("/Bukkit:plugin") || cmd.equalsIgnoreCase("/Bukkit:plugins") || cmd.equalsIgnoreCase("/pl") || cmd.equalsIgnoreCase("/plugin") || cmd.equalsIgnoreCase("/plugins")) {
            player.sendMessage(ChatColor.WHITE + "Plugins (1): " + ChatColor.GREEN + "CrimeRing");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void interactBlock(PlayerInteractEvent e) {
        if (!e.getPlayer().isOp()) return;

        if (e.getClickedBlock() == null) return;

        Block block = e.getClickedBlock();
        if (block.getType() != Material.REDSTONE_LAMP_OFF) return;

        Block b = block.getLocation().add(0, 1, 0).getBlock();
        Material type = b.getType();
        MaterialData data = b.getState().getData();

        b.setType(Material.REDSTONE_BLOCK);
        lampBlocks.add(block);

        Bukkit.getScheduler().runTask(plugin, () -> {
            b.setType(type);
            b.getState().setData(data);
        });
    }

    @EventHandler
    public void RedstoneEvent(BlockRedstoneEvent e) {
        if (!lampBlocks.contains(e.getBlock())) return;

        e.setNewCurrent(1);
        lampBlocks.remove(e.getBlock());

    }
}
