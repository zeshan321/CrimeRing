package raids;

import com.zeshanaslam.crimering.Main;
import org.bukkit.entity.Player;

public class InviteObject {

    public int counter;
    public PartyObject party;
    public Player player;

    public InviteObject(PartyObject party, Player player) {
        this.counter = 30;
        this.party = party;
        this.player = player;
    }

    public void update() {
        this.counter -= 1;
        if (this.counter <= 0) {
            Main.instance.raidManager.invites.remove(this);
        }
    }
}
