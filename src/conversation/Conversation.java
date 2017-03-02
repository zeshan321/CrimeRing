package conversation;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class Conversation {

    public HashMap<UUID, ConversationObject> conversations = new HashMap<>();

    public enum ConvoType {
        INT, DOUBLE, STRING
    }

    public void startConversation(ConversationObject object) {
        conversations.put(object.player, object);
    }

    public void endConversation(Player player) {
        conversations.remove(player.getUniqueId());
    }
}
