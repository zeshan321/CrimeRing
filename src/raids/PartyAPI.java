package raids;

import com.zeshanaslam.crimering.Main;
import org.bukkit.entity.Player;

public class PartyAPI {
    public PartyObject getParty(Player player) {
        for (int a = 0; a < Main.instance.raidManager.parties.size(); a++) {
            if ((Main.instance.raidManager.parties.get(a)).hasMember(player)) {
                return Main.instance.raidManager.parties.get(a);
            }
        }
        return null;
    }

    public boolean isOwner(Player player) {
        for (int a = 0; a < Main.instance.raidManager.parties.size(); a++) {
            if ((Main.instance.raidManager.parties.get(a)).getOwner() == player) {
                return true;
            }
        }
        return false;
    }

    public boolean nameInUse(String name) {
        for (int a = 0; a < Main.instance.raidManager.parties.size(); a++) {
            if ((Main.instance.raidManager.parties.get(a)).getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public InviteObject getInvite(Player player) {
        for (int a = 0; a < Main.instance.raidManager.invites.size(); a++) {
            if ((Main.instance.raidManager.invites.get(a)).player == player) {
                return Main.instance.raidManager.invites.get(a);
            }
        }
        return null;
    }
}
