package brewing;

import com.zeshanaslam.crimering.Main;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.BrewingStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import utils.ItemUtils;

public class BrewListener implements Listener {

    private final Main plugin;

    public BrewListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Inventory inv = event.getClickedInventory();
        Player player = (Player) event.getWhoClicked();

        if (inv == null || InventoryType.BREWING != inv.getType()) return;

        if (!(inv instanceof BrewerInventory)) return;

        BrewerInventory brewer = (BrewerInventory) inv;
        Block brewery = brewer.getHolder().getBlock();
        BlockState state = brewery.getState();

        if (event.getSlot() >= 0 || event.getSlot() <= 4) {
            if (brewer.getItem(event.getSlot()) == null) {
                event.setCancelled(true);

                if (event.getClick() == ClickType.RIGHT) {
                    ItemStack itemStack = player.getItemOnCursor().clone();
                    itemStack.setAmount(1);

                    brewer.setItem(event.getSlot(), itemStack);

                    if (player.getItemOnCursor().getAmount() == 1) {
                        player.setItemOnCursor(null);
                    } else {
                        player.getItemOnCursor().setAmount(player.getItemOnCursor().getAmount() - 1);
                    }
                } else {
                    brewer.setItem(event.getSlot(), player.getItemOnCursor());
                    player.setItemOnCursor(null);
                }
            } else {
                if (new ItemUtils().isSameItem(brewer.getItem(event.getSlot()), player.getItemOnCursor())) {
                    event.setCancelled(true);

                    if (event.getClick() == ClickType.RIGHT) {
                        ItemStack itemStack = player.getItemOnCursor().clone();
                        itemStack.setAmount(brewer.getItem(event.getSlot()).getAmount() + 1);

                        brewer.setItem(event.getSlot(), itemStack);

                        if (player.getItemOnCursor().getAmount() == 1) {
                            player.setItemOnCursor(null);
                        } else {
                            player.getItemOnCursor().setAmount(player.getItemOnCursor().getAmount() - 1);
                        }
                    } else {
                        ItemStack itemStack = brewer.getItem(event.getSlot());
                        itemStack.setAmount(itemStack.getAmount() + player.getItemOnCursor().getAmount());

                        brewer.setItem(event.getSlot(), itemStack);
                        player.setItemOnCursor(null);
                    }
                }
            }
        }

        if (state instanceof BrewingStand) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                int fuel = brewer.getHolder().getFuelLevel();
                BrewingStand brewingStand = (BrewingStand) state;
                BrewObject brewObject = getBrew(brewer, player);

                String id = brewery.getWorld().getName() + " " + brewery.getX() + " " + brewery.getY() + " " + brewery.getZ();
                if (brewObject == null) {
                    if (plugin.brewingManager.brews.containsKey(id)) {
                        plugin.brewingManager.brews.remove(id);
                    }
                    return;
                }

                if (fuel >= brewObject.fuel) {
                    brewObject.start = System.currentTimeMillis();
                    plugin.brewingManager.brews.put(id, brewObject);

                    // Use fuel
                    brewingStand.setFuelLevel(brewingStand.getFuelLevel() - brewObject.fuel);
                }
            }, 1);
        }
    }

    private BrewObject getBrew(BrewerInventory brewer, Player player) {
        String slot1 = "0:0 1";
        String slot2 = "0:0 1";
        String slot3 = "0:0 1";
        String slot4 = "0:0 1";

        if (brewer.getItem(0) != null) {
            slot1 = brewer.getItem(0).getTypeId() + ":" + brewer.getItem(0).getDurability() + " " + brewer.getItem(0).getAmount();
        }

        if (brewer.getItem(1) != null) {
            slot2 = brewer.getItem(1).getTypeId() + ":" + brewer.getItem(1).getDurability() + " " + brewer.getItem(1).getAmount();
        }

        if (brewer.getItem(2) != null) {
            slot3 = brewer.getItem(2).getTypeId() + ":" + brewer.getItem(2).getDurability() + " " + brewer.getItem(2).getAmount();
        }

        if (brewer.getItem(3) != null) {
            slot4 = brewer.getItem(3).getTypeId() + ":" + brewer.getItem(3).getDurability() + " " + brewer.getItem(3).getAmount();
        }

        return plugin.brewingManager.getBrew(player, slot1, slot2, slot3, slot4);
    }
}
