package packets;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import com.google.common.base.Preconditions;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public abstract class EntityFlagFilter {
    public static final int ENTITY_ON_FIRE = 1 << 0;
    public static final int ENTITY_CROUCHED = 1 << 1;
    public static final int ENTITY_SPRINTING = 1 << 3;
    public static final int ENTITY_CONSUMING = 1 << 4;
    public static final int ENTITY_INVISIBLE = 1 << 5;
    public static final int ENTITY_GLOWING = 1 << 6;

    private ProtocolManager manager;
    private PacketListener packetListener;
    private boolean registered;
    private boolean closed;

    public EntityFlagFilter(Plugin plugin) {
        packetListener = new PacketAdapter(plugin, WrapperPlayServerEntityMetadata.TYPE) {
            @Override
            public void onPacketSending(PacketEvent event) {
                EntityFlagFilter.this.onPacketSending(event);
            }
        };
    }

    public void register() {
        Preconditions.checkState(!registered, "Filter already registered.");
        Preconditions.checkState(!closed, "Filter has been closed.");
        registered = true;
        manager = ProtocolLibrary.getProtocolManager();
        manager.addPacketListener(packetListener);
    }

    public void unregister() {
        if (registered && !closed) {
            manager.removePacketListener(packetListener);
            manager = null;
            closed = true;
        }
    }

    public void updateEntity(Entity entity) {
        Preconditions.checkState(!closed, "Filter has been closed.");
        Byte flag = WrappedDataWatcher.getEntityWatcher(entity).getByte(0);

        // It doesn't matter much
        if (flag == null) {
            flag = 0;
        }

        // Create the packet we will transmit
        WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata();
        WrappedDataWatcher watcher = WrappedDataWatcher.getEntityWatcher(entity);
        WrappedDataWatcher.Serializer serializer = WrappedDataWatcher.Registry.get(Byte.class);
        WrappedDataWatcher.WrappedDataWatcherObject object = new WrappedDataWatcher.WrappedDataWatcherObject(0, serializer);
        watcher.setObject(object, flag);

        packet.setEntityID(entity.getEntityId());
        packet.setMetadata(watcher.getWatchableObjects());

        // Broadcast the packet
        for (Player observer : manager.getEntityTrackers(entity)) {
            try {
                manager.sendServerPacket(observer, packet.getHandle());
            } catch (Exception e) {
                throw new RuntimeException("Cannot send packet " + packet.getHandle() + " to " + observer, e);
            }
        }
    }

    private void onPacketSending(PacketEvent event) {
        WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(event.getPacket());
        Entity entity = packet.getEntity(event);
        Byte input = null;

        // Find the flag value
        for (WrappedWatchableObject object : packet.getMetadata()) {
            if (object.getIndex() == 0) {
                input = (Byte) object.getValue();
                break;
            }
        }

        if (input != null) {
            // Allow our filter to process it
            int filtered = filterFlag(event.getPlayer(), entity, input);

            // Clone and update changes
            if (filtered != input) {
                packet = new WrapperPlayServerEntityMetadata(packet.getHandle().deepClone());
                WrappedDataWatcher watcher = new WrappedDataWatcher(packet.getMetadata());
                WrappedDataWatcher.Serializer serializer = WrappedDataWatcher.Registry.get(Byte.class);
                WrappedDataWatcher.WrappedDataWatcherObject object = new WrappedDataWatcher.WrappedDataWatcherObject(0, serializer);
                watcher.setObject(object, (byte) filtered);
                packet.setMetadata(watcher.getWatchableObjects());
                event.setPacket(packet.getHandle());
            }
        }
    }

    protected abstract int filterFlag(Player observer, Entity observed, int flagValues);
}
