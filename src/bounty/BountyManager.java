package bounty;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.zeshanaslam.crimering.FileHandler;
import com.zeshanaslam.crimering.Main;
import conversation.Conversation;
import conversation.ConversationCallback;
import conversation.ConversationObject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import utils.HiddenStringUtils;
import utils.ItemNames;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class BountyManager {

    private Multimap<UUID, BountyObject> bounties = ArrayListMultimap.create();

    public BountyManager() {
        bounties.clear();

        try (Stream<Path> paths = Files.walk(Paths.get("plugins/CrimeRing/bounties/"))) {
            paths.forEach(filePath -> {
                if (Files.isRegularFile(filePath)) {
                    FileHandler fileHandler = new FileHandler(filePath.toFile());

                    UUID target = UUID.fromString(fileHandler.getString("target"));
                    String details = fileHandler.getString("details");
                    UUID owner = UUID.fromString(fileHandler.getString("owner"));
                    List<String> items = fileHandler.getStringList("items");

                    bounties.put(target, new BountyObject(fileHandler.getName(), target, owner, details, items));
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("[CR] Loaded " + bounties.size() + " bounties.");
    }

    public void createBounty(Player player) {
        final String[] data = {null, null};

        player.sendMessage(ChatColor.BOLD + "" + ChatColor.GOLD + "Who do you want put a bounty on?");
        Main.instance.conversation.startConversation(new ConversationObject(player.getUniqueId(), Conversation.ConvoType.STRING, new ConversationCallback() {
            @Override
            public void onValid(Player player, String value) {
                switch (this.instance().getStage(player)) {
                    case 0:
                        if (Main.instance.essentials.getOfflineUser(value) == null) {
                            player.sendMessage(ChatColor.RED + value + ChatColor.GRAY + " does not exist.");
                            this.instance().endConversation(player);
                            return;
                        }

                        data[0] = value;
                        this.instance().newStage(player);

                        player.sendMessage(ChatColor.BOLD + "" + ChatColor.GOLD + "Any extra details: ");
                        break;

                    case 1:
                        data[1] = value;

                        player.sendMessage(ChatColor.BOLD + "" + ChatColor.GOLD + "Place bounty rewards in the inventory.");
                        // Open inv
                        Inventory inventory = Main.instance.actionDefaults.createInventory(player, ChatColor.LIGHT_PURPLE + "KillSomeone.Biz: " + ChatColor.GRAY + "Create " + data[0], 54);
                        inventory.setItem(52, Main.instance.actionDefaults.createItemStackWithMeta(35, 1, 14, ChatColor.RED + "Cancel", ChatColor.GRAY + "Cancel bounty."));
                        inventory.setItem(53, Main.instance.actionDefaults.createItemStackWithMeta(35, 1, 13, ChatColor.RED + "Submit", ChatColor.GRAY + "Submit bounty.\n" + HiddenStringUtils.encodeString(data[0] + "//n" + data[1] + "//n" + UUID.randomUUID().toString())));

                        player.openInventory(inventory);
                        this.instance().endConversation(player);
                        break;
                }
            }

            @Override
            public void onInvalid(Player player, String value) {
                player.sendMessage("Failed: " + value);
            }
        }));

    }

    public void viewBounties(Player player) {
        ArrayList<ItemStack> bounties = new ArrayList<>();

        for (BountyObject object : this.bounties.values()) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(object.target);
            String lore = ChatColor.GRAY + "Details:\n" +
                    ChatColor.RED + object.details + "\n\n" +
                    ChatColor.GRAY + "Reward:\n";

            ItemStack head = Main.instance.actionDefaults.getPlayerHead(offlinePlayer.getName());

            for (String items : object.items) {
                int id = Integer.valueOf(items.split(":")[0]);
                int data = Integer.valueOf(items.split(":")[1].split(" ")[0]);
                int amount = Integer.valueOf(items.split(" ")[1]);

                ItemStack renamed = Main.instance.actionDefaults.createItemStackWithRenamer(id, amount, data);
                lore = lore + ChatColor.RED + ChatColor.stripColor(ItemNames.lookup(renamed)) + " x " + renamed.getAmount() + "\n";
            }

            lore = lore + HiddenStringUtils.encodeString(object.file);
            head = Main.instance.actionDefaults.setItemStackMeta(head, ChatColor.GRAY + "Target: " + ChatColor.RED + offlinePlayer.getName(), lore);


            bounties.add(head);
        }

        Main.instance.actionDefaults.openPageInv(player, ChatColor.LIGHT_PURPLE + "KillSomeone.Biz: " + ChatColor.GRAY + "All Bounties", bounties);
    }

    public void getBounties(Player player) {
        ArrayList<ItemStack> bounties = new ArrayList<>();

        for (BountyObject object : this.bounties.values()) {
            if (object.owner.equals(player.getUniqueId())) continue;

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(object.target);
            String lore = ChatColor.GRAY + "Details:\n" +
                    ChatColor.RED + object.details + "\n\n" +
                    ChatColor.GRAY + "Reward:\n";

            ItemStack head = Main.instance.actionDefaults.getPlayerHead(offlinePlayer.getName());

            for (String items : object.items) {
                int id = Integer.valueOf(items.split(":")[0]);
                int data = Integer.valueOf(items.split(":")[1].split(" ")[0]);
                int amount = Integer.valueOf(items.split(" ")[1]);

                ItemStack renamed = Main.instance.actionDefaults.createItemStackWithRenamer(id, amount, data);
                lore = lore + ChatColor.RED + ChatColor.stripColor(ItemNames.lookup(renamed)) + " x " + renamed.getAmount() + "\n";
            }

            lore = lore + HiddenStringUtils.encodeString(object.file);
            head = Main.instance.actionDefaults.setItemStackMeta(head, ChatColor.GRAY + "Target: " + ChatColor.RED + offlinePlayer.getName(), lore);

            bounties.add(head);
        }

        Main.instance.actionDefaults.openPageInv(player, ChatColor.LIGHT_PURPLE + "KillSomeone.Biz: " + ChatColor.GRAY + "My Bounties", bounties);
    }

    public void saveFile(Player player, String id, UUID target, String details, List<String> items) {
        FileHandler fileHandler = new FileHandler("plugins/CrimeRing/bounties/" + id);

        fileHandler.set("target", target.toString());
        fileHandler.set("details", details);
        fileHandler.set("owner", player.getUniqueId().toString());
        fileHandler.set("items", items);

        fileHandler.save();

        bounties.put(target, new BountyObject(fileHandler.getName(), target, player.getUniqueId(), details, items));
    }

    public void getRewards(Player player, String id) {
        for (BountyObject object : this.bounties.values()) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(object.target);

            if (object.file.equals(id)) {
                Inventory inventory = Main.instance.actionDefaults.createInventory(player, ChatColor.GRAY + "Bounty Reward: " + ChatColor.RED + offlinePlayer.getName(), 54);

                for (String items : object.items) {
                    int itemID = Integer.valueOf(items.split(":")[0]);
                    int data = Integer.valueOf(items.split(":")[1]);
                    int amount = Integer.valueOf(items.split(" ")[1]);

                    ItemStack renamed = Main.instance.actionDefaults.createItemStackWithRenamer(itemID, amount, data);
                    inventory.addItem(renamed);
                }

                player.openInventory(inventory);
                break;
            }
        }
    }

    public void getAllRewards(Player player, UUID target) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(target);

        Inventory inventory = Main.instance.actionDefaults.createInventory(player, ChatColor.GRAY + "Bounty Reward: " + ChatColor.RED + offlinePlayer.getName(), 54);

        Iterator<BountyObject> iterator = this.bounties.values().iterator();
        while (iterator.hasNext()) {
            BountyObject object = iterator.next();
            iterator.remove();

            removeFile(object.file);

            for (String items : object.items) {
                int itemID = Integer.valueOf(items.split(":")[0]);
                int data = Integer.valueOf(items.split(":")[1].split(" ")[0]);
                int amount = Integer.valueOf(items.split(" ")[1]);

                ItemStack renamed = Main.instance.actionDefaults.createItemStackWithRenamer(itemID, amount, data);
                inventory.addItem(renamed);
            }
        }

        player.openInventory(inventory);
    }

    public void removeFile(String id) {
        Iterator<BountyObject> iterator = this.bounties.values().iterator();
        while (iterator.hasNext()) {
            BountyObject object = iterator.next();

            if (object.file.equals(id)) {
                iterator.remove();

                FileHandler fileHandler = new FileHandler("plugins/CrimeRing/bounties/" + id);
                fileHandler.delete();
                break;
            }
        }
    }
}
