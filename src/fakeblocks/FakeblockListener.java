package fakeblocks;

import com.zeshanaslam.crimering.FileHandler;
import com.zeshanaslam.crimering.Main;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.List;

public class FakeblockListener implements Listener {

    private final Main plugin;

    public FakeblockListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlock(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        if (!(plugin.fakeBlocks.containsKey(player.getName()))) {
            return;
        }

        if (!(player.getGameMode() == GameMode.CREATIVE)) {
            return;
        }

        String fileName = plugin.fakeBlocks.get(player.getName());
        FileHandler yaml = new FileHandler("plugins/CrimeRing/fakeblocks/" + fileName + ".yml");


        List<String> listOfBlocks = yaml.getStringList("List");

        Block block = event.getBlock();
        listOfBlocks.add(block.getX() + " " + block.getY() + " " + block.getZ() + " " + block.getWorld().getName() + " " + block.getTypeId() + " " + block.getData());


        yaml.set("List", listOfBlocks);
        yaml.save();

        player.sendMessage(ChatColor.GRAY + "Added!");
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (!(plugin.fakeBlocks.containsKey(player.getName()))) {
            return;
        }

        if (!(player.getGameMode() == GameMode.CREATIVE)) {
            return;
        }

        String fileName = plugin.fakeBlocks.get(player.getName());
        FileHandler yaml = new FileHandler("plugins/CrimeRing/fakeblocks/" + fileName + ".yml");

        List<String> listOfBlocks = yaml.getStringList("List");

        Block block = event.getBlock();
        String s = block.getX() + " " + block.getY() + " " + block.getZ() + " " + block.getWorld().getName() + " " + block.getTypeId() + " " + block.getData();
        listOfBlocks.remove(s);

        yaml.set("List", listOfBlocks);
        yaml.save();

        player.sendMessage(ChatColor.GRAY + "Removed!");
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!(plugin.fakeBlocks.containsKey(player.getName()))) {
            return;
        }

        if (!(player.getGameMode() == GameMode.CREATIVE)) {
            return;
        }

        // Only use right hand
        if (event.getHand() == EquipmentSlot.OFF_HAND) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (player.getInventory().getItemInMainHand() == null || player.getInventory().getItemInMainHand().getType() != Material.FEATHER) {
            return;
        }

        String fileName = plugin.fakeBlocks.get(player.getName());
        FileHandler yaml = new FileHandler("plugins/CrimeRing/fakeblocks/" + fileName + ".yml");

        List<String> listOfBlocks = yaml.getStringList("List");

        Block block = event.getClickedBlock();

        String blockData = block.getX() + " " + block.getY() + " " + block.getZ() + " " + block.getWorld().getName() + " " + "0" + " " + block.getData() + " feather";
        if (listOfBlocks.contains(blockData)) {
            listOfBlocks.remove(blockData);
            player.sendMessage(ChatColor.GRAY + "Removed!");
        } else {
            listOfBlocks.add(blockData);
            player.sendMessage(ChatColor.GRAY + "Added!");
        }

        yaml.set("List", listOfBlocks);
        yaml.save();
    }
}
