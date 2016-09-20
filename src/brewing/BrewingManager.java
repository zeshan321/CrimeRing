package brewing;

import com.zeshanaslam.crimering.FileHandler;
import com.zeshanaslam.crimering.Main;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BrewingStand;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static java.lang.Math.toIntExact;

public class BrewingManager {

    public HashMap<String, BrewObject> brews = new HashMap<>();
    private List<BrewObject> brewObjectList = new ArrayList<>();

    public BrewingManager() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(Main.instance, () -> {
            Iterator<String> iterator = brews.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();

                BrewObject brewObject = brews.get(key);

                long secondsLeft = ((brewObject.start / 1000) + brewObject.duration) - (System.currentTimeMillis() / 1000);

                if (secondsLeft <= 0) {
                    Bukkit.getScheduler().runTask(Main.instance, () -> {
                        brews.remove(key);

                        String[] cords = key.split(" ");

                        Block brewery = Bukkit.getWorld(cords[0]).getBlockAt(Integer.valueOf(cords[1]), Integer.valueOf(cords[2]), Integer.valueOf(cords[3]));
                        BrewingStand brewingStand = (BrewingStand) brewery.getState();
                        BrewerInventory brewerInventory = brewingStand.getInventory();

                        int id1 = Integer.valueOf(brewObject.slotC1.split(" ")[0].split(":")[0]);
                        int data1 = Integer.valueOf(brewObject.slotC1.split(" ")[0].split(":")[1]);
                        int amount1 = Integer.valueOf(brewObject.slotC1.split(" ")[1]);
                        brewerInventory.setItem(0, new ItemStack(id1, amount1, (short) data1));

                        int id2 = Integer.valueOf(brewObject.slotC2.split(" ")[0].split(":")[0]);
                        int data2 = Integer.valueOf(brewObject.slotC2.split(" ")[0].split(":")[1]);
                        int amount2 = Integer.valueOf(brewObject.slotC2.split(" ")[1]);
                        brewerInventory.setItem(1, new ItemStack(id2, amount2, (short) data2));

                        int id3 = Integer.valueOf(brewObject.slotC3.split(" ")[0].split(":")[0]);
                        int data3 = Integer.valueOf(brewObject.slotC3.split(" ")[0].split(":")[1]);
                        int amount3 = Integer.valueOf(brewObject.slotC3.split(" ")[1]);
                        brewerInventory.setItem(2, new ItemStack(id3, amount3, (short) data3));

                        int id4 = Integer.valueOf(brewObject.slotC4.split(" ")[0].split(":")[0]);
                        int data4 = Integer.valueOf(brewObject.slotC4.split(" ")[0].split(":")[1]);
                        int amount4 = Integer.valueOf(brewObject.slotC4.split(" ")[1]);
                        brewerInventory.setItem(3, new ItemStack(id4, amount4, (short) data4));
                    });
                }
            }

            Bukkit.getOnlinePlayers().stream().filter(players -> players.getOpenInventory() != null && players.getOpenInventory().getTopInventory().getType() == InventoryType.BREWING).forEach(players -> {
                BrewerInventory brewer = (BrewerInventory) players.getOpenInventory().getTopInventory();
                Block brewery = brewer.getHolder().getBlock();

                String id = brewery.getWorld().getName() + " " + brewery.getX() + " " + brewery.getY() + " " + brewery.getZ();
                BrewObject brewObject = brews.get(id);

                if (brewObject != null) {
                    InventoryView view = players.getOpenInventory();
                    view.setProperty(InventoryView.Property.BREW_TIME, toIntExact(((brewObject.start / 1000) + brewObject.duration) - (System.currentTimeMillis() / 1000)));
                }
            });
        }, 0L, 1L);
    }

    public void load() {
        brewObjectList.clear();

        FileHandler fileHandler = new FileHandler("plugins/CrimeRing/brewing.yml");

        for (String key : fileHandler.getKeys()) {
            String slot1 = fileHandler.getString(key + ".slot1");
            String slot2 = fileHandler.getString(key + ".slot2");
            String slot3 = fileHandler.getString(key + ".slot3");
            String slot4 = fileHandler.getString(key + ".slot4");
            String slotC1 = fileHandler.getString(key + ".complete.slot1");
            String slotC2 = fileHandler.getString(key + ".complete.slot2");
            String slotC3 = fileHandler.getString(key + ".complete.slot3");
            String slotC4 = fileHandler.getString(key + ".complete.slot4");
            int duration = fileHandler.getInteger(key + ".duration");
            int fuel = fileHandler.getInteger(key + ".fuel");

            brewObjectList.add(new BrewObject(slot1, slot2, slot3, slot4, slotC1, slotC2, slotC3, slotC4, fuel, duration));
        }
    }

    public BrewObject getBrew(String slot1, String slot2, String slot3, String slot4) {
        BrewObject brewObject = null;

        for (BrewObject object : brewObjectList) {

            if (object.slot1.equals(slot1) && object.slot2.equals(slot2) && object.slot3.equals(slot3) && object.slot4.equals(slot4)) {
                brewObject = object;
                break;
            }
        }

        return brewObject;
    }
}
