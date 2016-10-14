package events;

import com.nitnelave.CreeperHeal.block.BurntBlockManager;
import com.shampaggon.crackshot.events.WeaponScopeEvent;
import com.zeshanaslam.crimering.Main;
import customevents.ArrowHitBlockEvent;
import net.minecraft.server.v1_10_R1.EnumItemSlot;
import net.minecraft.server.v1_10_R1.PacketPlayOutEntityEquipment;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import raids.PartyAPI;
import raids.PartyObject;
import utils.ItemUtils;

import java.util.Iterator;
import java.util.List;


public class BasicEvents implements Listener {

    private final Main plugin;

    public BasicEvents(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPluginsView(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String cmd = event.getMessage().toLowerCase();

        if (cmd.startsWith("/bukkit:pl") || cmd.startsWith("/bukkit:plugin") || cmd.startsWith("/bukkit:plugins") || cmd.startsWith("/pl") || cmd.startsWith("/plugin") || cmd.startsWith("/plugins")) {
            player.sendMessage(ChatColor.WHITE + "Plugins (1): " + ChatColor.GREEN + "CrimeRing");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamageItem(PlayerItemDamageEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockedCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String label = event.getMessage().split(" ")[0].toLowerCase();

        if (label.startsWith("/reload")) {
            event.setCancelled(true);
        }

        if (player.isOp()) {
            return;
        }

        if (plugin.getConfig().getStringList("Blocked-cmds").contains(label)) {
            player.sendMessage(ChatColor.RED + "You do not have access to this command!");
            event.setCancelled(true);
            return;
        }

        if (plugin.raidManager.isInRaid(player) && plugin.getConfig().getStringList("Blocked-cmds-raid").contains(label)) {
            player.sendMessage(ChatColor.RED + "You can not use that command while in a raid!");
            event.setCancelled(true);
            return;
        }

        PartyAPI partyAPI = new PartyAPI();
        PartyObject party = partyAPI.getParty(player);
        if (party != null) {
            if (party.getMembers().contains(player)) {
                if (plugin.raidManager.isInRaid(player) && plugin.getConfig().getStringList("Blocked-cmds-raid").contains(label)) {
                    player.sendMessage(ChatColor.RED + "You can not use that command while in a raid!");
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrop(BlockBreakEvent event) {
        List<Integer> remove = plugin.getConfig().getIntegerList("Blocked-items-drop");

        if (remove.contains(event.getBlock().getTypeId())) {
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getClickedBlock() == null) {
            return;
        }

        if (player.isOp()) {
            return;
        }

        if (plugin.getConfig().getIntegerList("Blocked-items-interact").contains(event.getClickedBlock().getTypeId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        if (event.toWeatherState()) {
            event.setCancelled(true);
            event.getWorld().setStorm(false);
            event.getWorld().setThundering(false);
            event.getWorld().setWeatherDuration(0);
        }
    }

    @EventHandler
    public void onCrackShotBug(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile) event.getDamager();

            if (projectile.getShooter() instanceof Player) {
                Player damaged = (Player) event.getEntity();
                Player shooter = (Player) projectile.getShooter();

                if (damaged == shooter) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMobSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onPaintingBreak(HangingBreakByEntityEvent event) {
        if (event.getRemover() instanceof Projectile) {
            event.setCancelled(true);
        }

        if (event.getRemover() instanceof Player) {
            Player player = (Player) event.getRemover();

            if (!(player.isOp())) {
                event.setCancelled(true);
            } else {
                if (!player.getInventory().getItemInMainHand().getType().equals(Material.PAINTING)) {
                    event.setCancelled(true);
                }
            }
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onScope(WeaponScopeEvent event) {
        Player player = event.getPlayer();

        if (player.getInventory().getItemInMainHand() == null) {
            return;
        }

        ItemStack inMainHand = player.getInventory().getItemInMainHand();
        if (!plugin.getConfig().getStringList("Scope-items").contains(inMainHand.getTypeId() + ":" + inMainHand.getDurability())) {
            return;
        }

        if (player.getGameMode() == GameMode.CREATIVE) {
            player.sendMessage(ChatColor.RED + "Scopes are disabled in creative!");
            event.setCancelled(true);
            return;
        }

        if (event.isZoomIn()) {
            ItemStack item = new ItemStack(Material.PUMPKIN);
            PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(player.getEntityId(), EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(item));
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        } else {
            player.updateInventory();
        }
    }

    // Stop paintings and cakes from blowing up
    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        Iterator<Block> it = event.blockList().iterator();
        while (it.hasNext()) {
            Block block = it.next();

            if (block.getType() == Material.CAKE_BLOCK) {
                it.remove();
            }
        }
    }

    @EventHandler
    public void onPhysics(BlockPhysicsEvent event) {
        if (event.getBlock().getType() == Material.CAKE_BLOCK) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHangingBreak(HangingBreakEvent event) {
        if (event.getCause() == HangingBreakEvent.RemoveCause.EXPLOSION || event.getCause() == HangingBreakEvent.RemoveCause.PHYSICS) {
            event.setCancelled(true);
        }
    }

    // Glass regen
    @EventHandler
    public void onArrowHitGlass(ArrowHitBlockEvent event) {
        if (event.getBlock().getType() == Material.GLASS || event.getBlock().getType() == Material.THIN_GLASS) {
            BurntBlockManager.recordBurntBlock(event.getBlock());

            event.getBlock().getWorld().playSound(event.getBlock().getLocation(), Sound.BLOCK_GLASS_BREAK, 10, 1);
            event.getBlock().breakNaturally();

            // Remove arrow on hit
            if (!event.getArrow().isDead()) {
                event.getArrow().remove();
            }
        }

        if (event.getBlock().getType() == Material.STAINED_GLASS || event.getBlock().getType() == Material.STAINED_GLASS_PANE) {
            if (event.getBlock().getData() == 15 || event.getBlock().getData() == 14 || event.getBlock().getData() == 11) {
                return;
            }

            BurntBlockManager.recordBurntBlock(event.getBlock());

            event.getBlock().getWorld().playSound(event.getBlock().getLocation(), Sound.BLOCK_GLASS_BREAK, 10, 1);
            event.getBlock().breakNaturally();

            // Remove arrow on hit
            if (!event.getArrow().isDead()) {
                event.getArrow().remove();
            }
        }
    }

    // Replace sign text that contains 'null'
    @EventHandler
    public void onSign(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            Block block = event.getClickedBlock();

            if (block.getType() == Material.SIGN_POST || block.getType() == Material.WALL_SIGN) {
                Sign sign = (Sign) block.getState();

                for (int i = 0; i < 4; i++) {
                    if (sign.getLine(i).contains("null")) {
                        sign.setLine(i, sign.getLine(i).replace("null", ""));
                    }
                }

                sign.update();
            }
        }
    }

    // Clear raid items on join in case of server crash
    @EventHandler
    public void onJoinRaid(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (player.getLocation().getWorld().getName().equals("RaidWorld")) {
            player.teleport(new Location(Bukkit.getWorld("world"), -132, 71, -95, (float) 91.1, (float) 1.5));
        }

        // Remove raid items
        new ItemUtils().clearRaidItems(player);
    }

    // Temp event
    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        World world = player.getLocation().getWorld();
        if (world.getName().equals("world")) {
            Chunk chunk = player.getLocation().getChunk();

            int cX = chunk.getX() * 16;
            int cZ = chunk.getZ() * 16;
            Biome biome = player.getLocation().getWorld().getBiome(cX, cZ);
            if (biome == Biome.SKY || biome == Biome.VOID || biome == Biome.HELL) {
                player.sendMessage(ChatColor.RED + "CR found '" + biome.name() + "' Biome. Fixing it... (Re-log for chunk updates)");

                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        world.setBiome(cX + x, cZ + z, Biome.FOREST);
                    }
                }
            }
        }
    }
}
