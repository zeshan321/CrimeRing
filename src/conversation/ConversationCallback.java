package conversation;

import com.zeshanaslam.crimering.Main;
import org.bukkit.entity.Player;

public abstract class ConversationCallback {

    public abstract void onValid(Player player, String value);

    public abstract void onInvalid(Player player, String value);

    public Conversation instance() {
        return Main.instance.conversation;
    }
}
