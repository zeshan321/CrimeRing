package renamer;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.zeshanaslam.crimering.FileHandler;
import com.zeshanaslam.crimering.Main;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RenamerManager {

    private HashMap<String, RenamerObject> items = new HashMap<>();

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

    public void loadListeners() {
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
                        ItemStack[] read = packet.getItemArrayModifier().read(0);

                        for (int i = 0; i < read.length; i++) {
                            ItemStack itemStack = renameItem(read[i]);

                            if (itemStack != null) {
                                read[i] = renameItem(read[i]);

                                if (player.getOpenInventory() == null) {
                                    player.getInventory().setItem(i, read[i]);
                                } else {
                                    InventoryView inventory = player.getOpenInventory();
                                    inventory.setItem(i, read[i]);
                                }
                            }
                        }

                        packet.getItemArrayModifier().write(0, read);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        packet.getItemModifier().write(0, renameItem(packet.getItemModifier().read(0)));
                        //System.out.println(packet.getShorts().getField(1));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private ItemStack renameItem(ItemStack itemStack) {
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
}
