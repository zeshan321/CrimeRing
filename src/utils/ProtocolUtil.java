package utils;

import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import net.minecraft.server.v1_10_R1.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftItemStack;

import java.lang.reflect.Field;
import java.util.List;

public class ProtocolUtil {

    public void modifyWatchable(PacketEvent event, int index, Object value) {
        // Determine if we need to modify this packet
        if (hasIndex(getWatchable(event), index)) {
            // We do - clone it first, as it might have been broadcasted
            event.setPacket(event.getPacket().deepClone());

            for (WrappedWatchableObject object : getWatchable(event)) {
                if (object.getIndex() == index) {
                    object.setValue(value);
                }
            }
        }
    }

    public List<WrappedWatchableObject> getWatchable(PacketEvent event) {
        return event.getPacket().getWatchableCollectionModifier().read(0);
    }

    public boolean hasIndex(List<WrappedWatchableObject> list, int index) {
        for (WrappedWatchableObject object : list) {
            if (object.getIndex() == index) {
                return true;
            }
        }
        return false;
    }

    public NBTTagCompound getTag(org.bukkit.inventory.ItemStack item) {
        if ((item instanceof CraftItemStack)) {
            try {
                Field field = CraftItemStack.class.getDeclaredField("handle");
                field.setAccessible(true);
                return ((net.minecraft.server.v1_10_R1.ItemStack) field.get(item)).getTag();
            } catch (Exception e) {
            }
        }
        return null;
    }

    public org.bukkit.inventory.ItemStack setTag(org.bukkit.inventory.ItemStack item, NBTTagCompound tag) {
        CraftItemStack craftItem = null;
        if ((item instanceof CraftItemStack)) {
            craftItem = (CraftItemStack) item;
        } else {
            craftItem = CraftItemStack.asCraftCopy(item);
        }
        net.minecraft.server.v1_10_R1.ItemStack nmsItem = null;
        try {
            Field field = CraftItemStack.class.getDeclaredField("handle");
            field.setAccessible(true);
            nmsItem = (net.minecraft.server.v1_10_R1.ItemStack) field.get(item);
        } catch (Exception e) {
        }
        if (nmsItem == null) {
            nmsItem = CraftItemStack.asNMSCopy(craftItem);
        }
        nmsItem.setTag(tag);
        try {
            Field field = CraftItemStack.class.getDeclaredField("handle");
            field.setAccessible(true);
            field.set(craftItem, nmsItem);
        } catch (Exception e) {
        }
        return craftItem;
    }

    public org.bukkit.inventory.ItemStack setItemHideFlags(org.bukkit.inventory.ItemStack i) {
        if (i == null) {
            return i;
        }

        if (i.getType() == Material.BOOK_AND_QUILL) {
            return i;
        }

        if (!(i instanceof CraftItemStack)) {
            i = CraftItemStack.asCraftCopy(i);
        }

        NBTTagCompound tag = getTag(i);

        if (tag == null) {
            tag = new NBTTagCompound();
        }

        tag.setInt("HideFlags", 63);
        tag.setInt("Unbreakable", 1);

        return setTag(i, tag);
    }
}
