package renamer;

import com.zeshanaslam.crimering.FileHandler;
import com.zeshanaslam.crimering.Main;
import haveric.recipeManager.api.events.RecipeManagerCraftEvent;
import haveric.recipeManager.api.events.RecipeManagerSmeltEvent;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_11_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import utils.ItemNames;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class RenamerManager {

    public HashMap<String, RenamerObject> items = new HashMap<>();
    private Listener bukkitListener;

    public RenamerManager() {
        // Register events and packet listener
        Main.instance.getServer().getPluginManager().registerEvents(
                bukkitListener = constructBukkit(), Main.instance);
    }

    public void load() {
        items.clear();

        FileHandler fileHandler = new FileHandler("plugins/CrimeRing/renamer.yml");

        for (String key : fileHandler.getKeys()) {
            if (fileHandler.contains(key + ".lore")) {
                List<String> newLore = fileHandler.getStringList(key + ".lore").stream().map(lore -> ChatColor.translateAlternateColorCodes('&', lore)).collect(Collectors.toList());

                items.put(key, new RenamerObject(ChatColor.translateAlternateColorCodes('&', fileHandler.getString(key + ".name")), newLore));
            } else {
                items.put(key, new RenamerObject(ChatColor.translateAlternateColorCodes('&', fileHandler.getString(key + ".name")), null));
            }
        }
    }

    public void clear() {
        items.clear();
    }

    /*public void loadListeners() {
        Set<PacketType> packets = new HashSet<>();
        packets.add(PacketType.Play.Server.SET_SLOT);
        packets.add(PacketType.Play.Server.WINDOW_ITEMS);

        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(Main.instance,
                packets) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                PacketType type = packet.getType();
                Player player = event.getPlayer();

                if (type == PacketType.Play.Server.WINDOW_ITEMS) {
                    try {
                        List<ItemStack> read = packet.getItemListModifier().read(0);

                        for (int i = 0; i < read.size(); i++) {
                            ItemStack itemStack = renameItem(read.get(i));

                            if (itemStack != null) {
                                //read.set(i, renameItem(itemStack));

                                player.getInventory().setItem(i, itemStack);
                            }
                        }

                        packet.getItemListModifier().write(0, read);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        packet.getItemModifier().write(0, renameItem(packet.getItemModifier().read(0)));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }*/

    private Listener constructBukkit() {
        return new Listener() {
            @EventHandler
            public void onJoin(PlayerJoinEvent event) {
                Player player = event.getPlayer();

                int i = -1;
                for (ItemStack itemStack : player.getInventory()) {
                    i++;

                    if (itemStack == null || itemStack.hasItemMeta()) continue;

                    player.getInventory().setItem(i, renameItem(itemStack));
                }
            }

            @EventHandler
            public void onOpen(InventoryOpenEvent event) {
                int i = -1;

                for (ItemStack itemStack : event.getInventory().getContents()) {
                    i++;

                    if (itemStack == null || itemStack.hasItemMeta()) continue;

                    event.getInventory().setItem(i, renameItem(itemStack));
                }
            }

            @EventHandler
            public void onSwitch(PlayerItemHeldEvent event) {
                Player player = event.getPlayer();

                ItemStack itemStack = player.getInventory().getItem(event.getNewSlot());

                if (itemStack == null || itemStack.hasItemMeta()) return;

                player.getInventory().setItem(event.getNewSlot(), renameItem(itemStack));
            }

            @EventHandler(priority = EventPriority.HIGHEST)
            public void onCraft(RecipeManagerCraftEvent event) {
                event.setResult(renameItem(event.getResult()));
            }

            @EventHandler(priority = EventPriority.HIGHEST)
            public void onSmelt(RecipeManagerSmeltEvent event) {
                event.setResult(renameItem(event.getResult()));
            }

            @EventHandler
            public void onClick(InventoryClickEvent event) {
                new BukkitRunnable() {
                    public void run() {
                        int i = -1;

                        for (ItemStack itemStack : event.getInventory().getContents()) {
                            i++;

                            if (itemStack == null || itemStack.hasItemMeta()) continue;

                            event.getInventory().setItem(i, renameItem(itemStack));
                        }
                    }
                }.runTaskLater(Main.instance, 20L);
            }

            @EventHandler
            public void onClickCreative(InventoryCreativeEvent event) {
                new BukkitRunnable() {
                    public void run() {
                        int i = -1;

                        for (ItemStack itemStack : event.getInventory().getContents()) {
                            i++;

                            if (itemStack == null || itemStack.hasItemMeta()) continue;

                            event.getInventory().setItem(i, renameItem(itemStack));
                        }
                    }
                }.runTaskLater(Main.instance, 20L);
            }
        };
    }

    public ItemStack renameItem(ItemStack itemStack) {
        if (itemStack == null) {
            return itemStack;
        }

        String name = CraftItemStack.asNMSCopy(itemStack).getName();

        if (name.contains("Potion")) {
            if (!items.containsKey(name)) {
                return itemStack;
            }

            RenamerObject renamerObject = items.get(name);

            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(renamerObject.name);

            if (renamerObject.lore != null) {
                itemMeta.setLore(renamerObject.lore);
            }

            itemStack.setItemMeta(itemMeta);
        } else {
            if (itemStack.hasItemMeta()) {
                return itemStack;
            }

            if (items.containsKey(itemStack.getTypeId() + ":" + itemStack.getDurability())) {

                RenamerObject renamerObject = items.get(itemStack.getTypeId() + ":" + itemStack.getDurability());

                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(renamerObject.name);

                if (renamerObject.lore != null) {
                    itemMeta.setLore(renamerObject.lore);
                }

                itemStack.setItemMeta(itemMeta);
            }

            if (items.containsKey(name)) {
                RenamerObject renamerObject = items.get(name);

                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(renamerObject.name);

                if (renamerObject.lore != null) {
                    itemMeta.setLore(renamerObject.lore);
                }

                itemStack.setItemMeta(itemMeta);
            }
        }

        return itemStack;
    }

    public String getNameByID(int id, int data) {
        if (items.containsKey(id + ":" + data)) {
            RenamerObject renamerObject = items.get(id + ":" + data);
            return renamerObject.name;
        }

        return ItemNames.lookup(new ItemStack(id, 1, (short) data));
    }
}
