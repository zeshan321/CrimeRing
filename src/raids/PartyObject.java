package raids;

import com.zeshanaslam.crimering.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PartyObject {
    private ArrayList<Player> members = new ArrayList();
    private Player owner;
    private String name;

    public PartyObject(String name, Player owner) {
        this.members.add(owner);

        this.name = name;
        this.owner = owner;
    }

    public boolean hasMember(Player player) {
        for (Player member : this.members) {
            if (member == player) {
                return true;
            }
        }
        return false;
    }

    public Player getOwner() {
        return this.owner;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }

    public String getName() {
        return this.name;
    }

    public void sendMessage(String msg) {
        for (Player member : this.members) {
            member.sendMessage(msg);
        }
    }

    public void addMember(Player player) {
        this.members.add(player);
    }

    public void removeMember(Player player) {
        this.members.remove(player);
    }

    public int size() {
        return members.size();
    }

    public Player nextPlayer() {
        return members.get(0);
    }

    public List<Player> getMembers() {
        return members;
    }

    public String membersList() {
        String list = "";
        Iterator iterator = members.iterator();

        while (iterator.hasNext()) {
            if (!list.endsWith(", ")) {
                if (!list.equals(""))
                    list += ", ";
            }

            Player player = (Player) iterator.next();

            list += player.getName();
        }
        return list;
    }

    public void update() {
        for (int a = 0; a < this.members.size(); a++) {
            if ((Bukkit.getPlayer((this.members.get(a)).getName()) == null) &&
                    (this.members.get(a) != this.owner)) {
                removeMember(this.members.get(a));
                sendMessage(ChatColor.GOLD + (this.members.get(a)).getName() + " has been kicked out of the party!");
            }
        }
        if (Bukkit.getPlayer(this.owner.getName()) == null) {
            sendMessage(ChatColor.GOLD + "Your party has been disbanded!");
            Main.instance.raidManager.parties.remove(this);
        }
    }
}
