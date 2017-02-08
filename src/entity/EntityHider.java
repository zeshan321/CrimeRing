package entity;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static com.comphenix.protocol.PacketType.Play.Server.*;

public class EntityHider implements Listener {
    // Packets that update remote player entities
    private static final PacketType[] ENTITY_PACKETS = {
            ENTITY_EQUIPMENT, BED, ANIMATION, NAMED_ENTITY_SPAWN,
            COLLECT, SPAWN_ENTITY, SPAWN_ENTITY_LIVING, SPAWN_ENTITY_PAINTING, SPAWN_ENTITY_EXPERIENCE_ORB,
            ENTITY_VELOCITY, REL_ENTITY_MOVE, ENTITY_LOOK, ENTITY_MOVE_LOOK, ENTITY_MOVE_LOOK,
            ENTITY_TELEPORT, ENTITY_HEAD_ROTATION, ENTITY_STATUS, ATTACH_ENTITY, ENTITY_METADATA,
            ENTITY_EFFECT, REMOVE_ENTITY_EFFECT, BLOCK_BREAK_ANIMATION

            // We don't handle DESTROY_ENTITY though
    };
    // Current policy
    private ProtocolManager manager;

    // Listeners
    private Listener bukkitListener;
    private PacketAdapter protocolListener;

    // Entities
    private HashMap<Integer, List<UUID>> entities = new HashMap<>();

    /**
     * Construct a new entity hider.
     *
     * @param plugin - the plugin that controls this entity hider.
     */
    public EntityHider(Plugin plugin) {
        Preconditions.checkNotNull(plugin, "plugin cannot be NULL.");

        this.manager = ProtocolLibrary.getProtocolManager();

        // Register events and packet listener
        plugin.getServer().getPluginManager().registerEvents(
                bukkitListener = constructBukkit(), plugin);

        manager.addPacketListener(
                protocolListener = constructProtocol(plugin));
    }

    public boolean canSee(Player player, Entity entity) {
        if (entities.containsKey(entity.getEntityId())) {
            if (!(entities.get(entity.getEntityId())).contains(player.getUniqueId())) {
                return false;
            }
        }

        return true;
    }

    public boolean canSee(Player player, int id) {
        if (entities.containsKey(id)) {
            if (!(entities.get(id)).contains(player.getUniqueId())) {
                return false;
            }
        }

        return true;
    }

    public void removeEntity(Entity entity) {
        entities.remove(entity.getEntityId());
    }

    public void removePlayer(Player player) {
        Iterator<Integer> iterator = entities.keySet().iterator();

        while (iterator.hasNext()) {
            int id = iterator.next();
            if (entities.get(id).contains(player.getUniqueId()) || entities.get(id).isEmpty()) {
                iterator.remove();
            }
        }
    }

    public void hideEntity(Entity entity, Player player) {
        List<UUID> players;

        if (!entities.containsKey(entity.getEntityId())) {
            players = new ArrayList<>();
            players.add(player.getUniqueId());

            entities.put(entity.getEntityId(), players);
        } else {
            players = entities.get(entity.getEntityId());

            if (!players.contains(player.getUniqueId())) {
                players.add(player.getUniqueId());

                entities.put(entity.getEntityId(), players);
            }
        }

        for (Player playersOnline : Bukkit.getOnlinePlayers()) {
            if (players.contains(playersOnline.getUniqueId())) {
                continue;
            }

            PacketContainer destroyEntity = new PacketContainer(ENTITY_DESTROY);
            destroyEntity.getIntegerArrays().write(0, new int[]{entity.getEntityId()});

            // Make the entity disappear
            try {
                manager.sendServerPacket(playersOnline, destroyEntity);
            } catch (InvocationTargetException e) {
                throw new RuntimeException("Cannot send server packet.", e);
            }
        }
    }

    public void showEntity(Player player, Entity entity) {
        if (entities.containsKey(entity.getEntityId())) {
            List<UUID> players = entities.get(entity.getEntityId());
            players.add(player.getUniqueId());

            entities.put(entity.getEntityId(), players);

            manager.updateEntity(entity, Arrays.asList(player));
        }
    }


    /**
     * Construct the Bukkit event listener.
     *
     * @return Our listener.
     */
    private Listener constructBukkit() {
        return new Listener() {
            @EventHandler
            public void onEntityDeath(EntityDeathEvent e) {
                removeEntity(e.getEntity());
            }

            @EventHandler
            public void onEntityDamage(EntityDamageByEntityEvent e) {
                if (e.getDamager() instanceof Player) {
                    Player player = (Player) e.getDamager();

                    if (!canSee(player, e.getEntity())) {
                        e.setCancelled(true);
                    }
                }
            }

            @EventHandler
            public void onChunkUnload(ChunkUnloadEvent e) {
                for (Entity entity : e.getChunk().getEntities()) {
                    if (entity.isDead()) {
                        removeEntity(entity);
                    }
                }
            }

            @EventHandler
            public void onPlayerQuit(PlayerQuitEvent e) {
                removePlayer(e.getPlayer());
            }

            @EventHandler(priority = EventPriority.HIGHEST)
            public void onTarget(EntityTargetEvent event) {
                if (event.getTarget() instanceof Player) {
                    Player player = (Player) event.getTarget();

                    if (!canSee(player, event.getEntity())) {
                        event.setCancelled(true);
                    }
                }
            }
        };
    }

    /**
     * Construct the packet listener that will be used to intercept every entity-related packet.
     *
     * @param plugin - the parent plugin.
     * @return The packet listener.
     */
    private PacketAdapter constructProtocol(Plugin plugin) {
        return new PacketAdapter(plugin, ENTITY_PACKETS) {
            @Override
            public void onPacketSending(PacketEvent event) {
                int entityID = event.getPacket().getIntegers().read(0);

                // See if this packet should be cancelled
                if (!canSee(event.getPlayer(), entityID)) {
                    event.setCancelled(true);
                }
            }
        };
    }
}