package events;

import com.zeshanaslam.crimering.Main;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

public class StackEvent implements Listener {

    private final Main plugin;
    private final int maxSize = 64;
    private final Material materialStack = Material.WOOD_AXE;

    public StackEvent(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        if (event.isCancelled()) {
            return;
        }

        if (current == null || cursor == null) {
            return;
        }

        if (!isValid(current, cursor)) {
            return;
        }

        updateAmount(player, current, cursor);
        event.setCancelled(true);
    }


    @EventHandler
    public void onPick(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();

        ItemStack item = event.getItem().getItemStack();

        for (ItemStack itemStack : player.getInventory().getContents()) {
            if (itemStack == null) {
                continue;
            }

            if (isValid(itemStack, item)) {
                if (updateAmount(null, itemStack, item)) {
                    event.getItem().remove();
                    event.setCancelled(true);
                    break;
                }
            }
        }
    }

    private boolean isValid(ItemStack current, ItemStack newItem) {
        if (current.getTypeId() == newItem.getTypeId() && current.getDurability() == newItem.getDurability() && current.getType() == materialStack && newItem.getType() == materialStack) {
            return true;
        }

        return false;
    }

    private boolean updateAmount(Player player, ItemStack current, ItemStack newItem) {
        if (current.getAmount() == maxSize) {
            return false;
        }

        int amount = Math.abs(current.getAmount() - newItem.getAmount());
        System.out.println(amount + " " + (current.getAmount() - newItem.getAmount()));

        if (amount == 0) {
            amount = maxSize - newItem.getAmount();

            current.setAmount(current.getAmount() + amount);
            newItem.setAmount(newItem.getAmount() - amount);
            return true;
        }

        if (amount < 0) {
            current.setAmount(current.getAmount() + amount);
            newItem.setAmount(newItem.getAmount() - amount);
        } else {
            if (player != null) {
                player.setItemOnCursor(null);
            }

            current.setAmount(current.getAmount() + newItem.getAmount());
        }

        return true;
    }
}
