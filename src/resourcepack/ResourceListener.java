package resourcepack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.zeshanaslam.crimering.Main;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.lang.reflect.InvocationTargetException;

public class ResourceListener implements Listener {

    private final Main plugin;

    public ResourceListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            Player player = event.getPlayer();

            PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.RESOURCE_PACK_SEND);
            packetContainer.getStrings().write(0, plugin.getConfig().getString("Resource-pack"));
            packetContainer.getStrings().write(1, plugin.getConfig().getString("Resource-hash"));

            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, packetContainer);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }, 20L);
    }

    @EventHandler
    public void onStatus(PlayerResourcePackStatusEvent event) {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (event.getStatus() == PlayerResourcePackStatusEvent.Status.ACCEPTED || event.getStatus() == PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED) {
                event.getPlayer().setMetadata("hasResource", new FixedMetadataValue(plugin, true));
                return;
            }

            event.getPlayer().kickPlayer(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Resource-kick")));
        });
    }
}
