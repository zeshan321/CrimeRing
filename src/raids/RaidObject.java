package raids;

import com.zeshanaslam.crimering.FileHandler;
import com.zeshanaslam.crimering.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RaidObject {

    public String id;
    public String raidID;
    public List<Player> members = new ArrayList<>();
    public List<Integer> tasks = new ArrayList<>();

    public RaidObject(String raidID) {
        this.raidID = raidID;
        this.id = UUID.randomUUID().toString();
    }

    public void removeMember(Player player) {
        FileHandler fileHandler = new FileHandler("plugins/CrimeRing/raids/" + raidID + ".yml");

        Location loc = new Location(Bukkit.getWorld(fileHandler.getString("info.worlds")), fileHandler.getInteger("info.xs"), fileHandler.getInteger("info.ys"), fileHandler.getInteger("info.zs"), fileHandler.getInteger("info.yaws"), fileHandler.getInteger("info.pitchs"));
        player.teleport(loc);

        members.remove(player);

        if (members.isEmpty()) {
            Main.instance.raidManager.cancelRaid(this);
        }
    }

    public void clearLists() {
        members.clear();
        tasks.clear();
    }
}
