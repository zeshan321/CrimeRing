package bounty;

import com.zeshanaslam.crimering.Main;
import conversation.Conversation;
import conversation.ConversationCallback;
import conversation.ConversationObject;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import utils.HiddenStringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BountyCreate implements Listener {

    private final Main plugin;

    public BountyCreate(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND) {
            return;
        }

        Player player = event.getPlayer();

        if (!player.hasPermission("CR.perk.bounty")) return;

        if (event.getItem() == null) {
            return;
        }

        ItemStack itemStack = event.getItem();

        if (itemStack.getType() == Material.SKULL_ITEM && itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName() && itemStack.getItemMeta().hasLore()) {
            String target = HiddenStringUtils.extractHiddenString(itemStack.getItemMeta().getLore().get(1));

            if (plugin.bountyManager.hasBounty(UUID.fromString(target))) {
                player.getInventory().setItemInMainHand(null);
                plugin.bountyManager.getAllRewards(player, UUID.fromString(target));
            }
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getClickedInventory() != null && event.getClickedInventory().getName() != null && ChatColor.stripColor(event.getClickedInventory().getName()).startsWith("KillSomeone.Biz: Create ")) {
            Player player = (Player) event.getWhoClicked();

            ItemStack itemStack = event.getInventory().getItem(53);

            String[] lore = HiddenStringUtils.extractHiddenString(itemStack.getItemMeta().getLore().get(1)).split("//n");
            String target = lore[0];
            String details = lore[1];
            String id = lore[2];


            if (event.getSlot() == 52) {
                event.setCancelled(true);
                player.closeInventory();

                int i = -1;
                for (ItemStack items: event.getClickedInventory().getContents()) {
                    i++;
                    if (items != null) {
                        if (i == 52 || i == 53) {
                            continue;
                        }
                        
                        player.getInventory().addItem(items);
                    }
                }

                player.sendMessage(ChatColor.GRAY + "Bounty canceled for " + ChatColor.RED + target + ChatColor.GRAY + ".");
            }

            if (event.getSlot() == 53) {
                event.setCancelled(true);

                List<String> items = new ArrayList<>();

                int i = -1;
                for (ItemStack rewards : event.getInventory().getContents()) {
                    i++;

                    if (i == 52 || i == 53) {
                        break;
                    }

                    if (rewards == null) continue;

                    items.add(rewards.getTypeId() + ":" + rewards.getDurability() + " " + rewards.getAmount());
                }

                if (items.isEmpty()) {
                    player.sendMessage(ChatColor.GRAY + "Bounties require at least one reward!");
                    return;
                }

                plugin.bountyManager.saveFile(player, id, Main.instance.essentials.getOfflineUser(target).getConfigUUID(), details, items);

                player.closeInventory();
                player.sendMessage(ChatColor.GRAY + "Bounty created for " + ChatColor.RED + target + ChatColor.GRAY + ".");
            }
        }

        if (event.getClickedInventory() != null && event.getClickedInventory().getName() != null && ChatColor.stripColor(event.getClickedInventory().getName()).startsWith("KillSomeone.Biz: All Bounties")) {
            event.setCancelled(true);
        }

        if (event.getClickedInventory() != null && event.getClickedInventory().getName() != null && ChatColor.stripColor(event.getClickedInventory().getName()).startsWith("KillSomeone.Biz: My Bounties")) {
            event.setCancelled(true);

            ItemStack item = event.getCurrentItem();
            if ((item == null) || (item.getItemMeta() == null) || (item.getItemMeta().getDisplayName() == null)) {
                return;
            }

            Player player = (Player) event.getWhoClicked();
            player.closeInventory();

            String target = ChatColor.stripColor(item.getItemMeta().getDisplayName()).replace("Target: ", "");

            player.sendMessage(ChatColor.BOLD + "" + ChatColor.GOLD + "Are you sure you want to cancel the bounty on " + target + "?");
            plugin.conversation.startConversation(new ConversationObject(player.getUniqueId(), Conversation.ConvoType.BOOLEAN, new ConversationCallback() {
                @Override
                public void onValid(Player player, String value) {
                    if (this.instance().getBoolean(value)) {
                        String id = HiddenStringUtils.extractHiddenString(item.getItemMeta().getLore().get(item.getItemMeta().getLore().size() - 1));
                        plugin.bountyManager.getRewards(player, id);
                        plugin.bountyManager.removeFile(id);

                        player.sendMessage(ChatColor.GRAY + "Bounty canceled for " + ChatColor.RED + target + ChatColor.GRAY + ".");

                        this.instance().endConversation(player);
                    } else {
                        this.instance().endConversation(player);
                    }
                }

                @Override
                public void onInvalid(Player player, String value) {
                    player.sendMessage(ChatColor.BOLD + "" + ChatColor.GOLD + "Are you sure you want to cancel the bounty on " + target + "?");
                }
            }));
        }
    }
}
