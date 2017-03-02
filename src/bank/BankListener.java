package bank;

import com.zeshanaslam.crimering.Main;
import conversation.Conversation;
import conversation.ConversationCallback;
import conversation.ConversationObject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class BankListener implements Listener {

    private final Main plugin;

    public BankListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();

        // Only use right hand
        if (event.getHand() == EquipmentSlot.OFF_HAND) {
            return;
        }


        if (event.getRightClicked() instanceof LivingEntity) {
            LivingEntity interacted = (LivingEntity) event.getRightClicked();

            if (interacted.getCustomName() != null && ChatColor.stripColor(interacted.getCustomName()).equals("Banker")) {
                openInventory(player, false);
            }
        }
    }

    @EventHandler
    public void onInteractATM(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // Only use right hand
        if (event.getHand() == EquipmentSlot.OFF_HAND) {
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();

            // Check if block is atm
            if (block.getTypeId() == 145) {
                if (block.getData() == 4 || block.getData() == 5 || block.getData() == 6 || block.getData() == 7) {
                    event.setCancelled(true);
                    openInventory(player, true);
                }
            }
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getInventory().getType() != InventoryType.CHEST || event.getInventory().getTitle() == null) {
            return;
        }

        ItemStack item = event.getCurrentItem();
        if ((item == null) || (item.getItemMeta() == null) || (item.getItemMeta().getDisplayName() == null)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        if (event.getInventory().getTitle().equals("Cogent City Bank")) {
            event.setCancelled(true);

            switch (ChatColor.stripColor(item.getItemMeta().getDisplayName())) {
                case "Check balance":
                    // Update balance
                    double bankBalance = Main.instance.actionDefaults.getBalance(player);
                    int playerBalance = Main.instance.actionDefaults.getInvMoney(player);

                    event.getInventory().setItem(event.getSlot(), Main.instance.actionDefaults.createItemStackWithMeta(293, 1, 412, ChatColor.GOLD + "Check balance", ChatColor.GREEN + "Bank Balance: " + ChatColor.AQUA + "$" + bankBalance + "\n" + ChatColor.GREEN + "Player Balance: " + ChatColor.AQUA + "$" + playerBalance));
                    break;

                case "Deposit":
                    player.closeInventory();
                    player.sendMessage(ChatColor.BOLD + "" + ChatColor.GOLD + "How much you would like to deposit?");
                    plugin.conversation.startConversation(new ConversationObject(player.getUniqueId(), Conversation.ConvoType.INT, new ConversationCallback() {
                        @Override
                        public void onValid(Player player, String value) {
                            int amount = Integer.valueOf(value);

                            if (Main.instance.actionDefaults.getInvMoney(player) >= amount) {
                                Main.instance.actionDefaults.takeInvMoney(player, amount);

                                double taxed = amount * 0.20;
                                double newAmount = amount - taxed;

                                Main.instance.actionDefaults.depositMoney(player, newAmount);

                                player.sendMessage(ChatColor.GOLD + "Here's your updated balances:");
                                player.sendMessage(ChatColor.GREEN + "Bank Balance: " + ChatColor.AQUA + "$" + Main.instance.actionDefaults.getBalance(player) + "\n" + ChatColor.GREEN + "Player Balance: " + ChatColor.AQUA + "$" + Main.instance.actionDefaults.getInvMoney(player));
                            } else {
                                player.sendMessage(ChatColor.RED + "You do not have enough cash on you!");
                            }

                            plugin.conversation.endConversation(player);
                        }

                        @Override
                        public void onInvalid(Player player, String value) {
                            player.sendMessage(ChatColor.RED + "Only numerical values are accepted!");
                        }
                    }));
                    break;

                case "Withdraw":
                    player.closeInventory();
                    player.sendMessage(ChatColor.BOLD + "" + ChatColor.GOLD + "How much you would like to withdraw?");

                    plugin.conversation.startConversation(new ConversationObject(player.getUniqueId(), Conversation.ConvoType.INT, new ConversationCallback() {
                        @Override
                        public void onValid(Player player, String value) {
                            int amount = Integer.valueOf(value);

                            if (Main.instance.actionDefaults.getBalance(player) >= amount) {
                                Main.instance.actionDefaults.withdrawMoney(player, amount);
                                Main.instance.actionDefaults.addInvBills(player, amount);

                                player.sendMessage(ChatColor.GOLD + "Here's your updated balances:");
                                player.sendMessage(ChatColor.GREEN + "Bank Balance: " + ChatColor.AQUA + "$" + Main.instance.actionDefaults.getBalance(player) + "\n" + ChatColor.GREEN + "Player Balance: " + ChatColor.AQUA + "$" + Main.instance.actionDefaults.getInvMoney(player));
                            } else {
                                player.sendMessage(ChatColor.RED + "You do not have enough money in the bank!");
                            }

                            plugin.conversation.endConversation(player);
                        }

                        @Override
                        public void onInvalid(Player player, String value) {
                            player.sendMessage(ChatColor.RED + "Only numerical values are accepted!");
                        }
                    }));
                    break;

                case "Convert":
                    player.closeInventory();
                    int cash = plugin.actionDefaults.getInvMoney(player);
                    plugin.actionDefaults.takeInvMoney(player, cash);

                    for (ItemStack money : plugin.actionDefaults.getBillsStack(cash)) {
                        if (money == null) continue;

                        player.getInventory().addItem(money);
                    }
                    break;
            }
        }
    }

    private void openInventory(Player player, boolean atm) {
        // End convo
        plugin.conversation.endConversation(player);

        double bankBalance = Main.instance.actionDefaults.getBalance(player);
        int playerBalance = Main.instance.actionDefaults.getInvMoney(player);

        Inventory inventory = Bukkit.createInventory(player, 9, "Cogent City Bank");
        if (!atm) {
            inventory.setItem(1, Main.instance.actionDefaults.createItemStackWithMeta(293, 1, 412, ChatColor.GOLD + "Check balance", ChatColor.GREEN + "Bank Balance: " + ChatColor.AQUA + "$" + bankBalance + "\n" + ChatColor.GREEN + "Player Balance: " + ChatColor.AQUA + "$" + playerBalance));
            inventory.setItem(3, Main.instance.actionDefaults.createItemStackWithMeta(293, 1, 411, ChatColor.GOLD + "Withdraw", ChatColor.GREEN + "Click to withdraw"));
            inventory.setItem(5, Main.instance.actionDefaults.createItemStackWithMeta(293, 1, 410, ChatColor.GOLD + "Deposit", ChatColor.GREEN + "Click to deposit"));
            inventory.setItem(7, Main.instance.actionDefaults.createItemStackWithMeta(293, 1, 410, ChatColor.GOLD + "Convert", ChatColor.GREEN + "Click to convert your cash to bigger bills"));
        } else {
            inventory.setItem(2, Main.instance.actionDefaults.createItemStackWithMeta(293, 1, 412, ChatColor.GOLD + "Check balance", ChatColor.GREEN + "Bank Balance: " + ChatColor.AQUA + "$" + bankBalance + "\n" + ChatColor.GREEN + "Player Balance: " + ChatColor.AQUA + "$" + playerBalance));
            inventory.setItem(6, Main.instance.actionDefaults.createItemStackWithMeta(293, 1, 411, ChatColor.GOLD + "Withdraw", ChatColor.GREEN + "Click to withdraw"));
        }


        player.openInventory(inventory);
    }
}
