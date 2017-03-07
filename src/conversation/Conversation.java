package conversation;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class Conversation {

    public HashMap<UUID, ConversationObject> conversations = new HashMap<>();

    public enum ConvoType {
        INT, DOUBLE, STRING, NUMBER, BOOLEAN
    }

    public void startConversation(ConversationObject object) {
        conversations.put(object.player, object);
    }

    public void endConversation(Player player) {
        conversations.remove(player.getUniqueId());
    }

    public void changeType(Player player, ConvoType type) {
        ConversationObject object = conversations.get(player.getUniqueId());

        endConversation(player);
        object.type = type;

        startConversation(object);
    }

    public void newStage(Player player) {
        ConversationObject object = conversations.get(player.getUniqueId());

        endConversation(player);
        object.stage = object.stage + 1;

        startConversation(object);
    }

    public int getStage(Player player) {
        ConversationObject object = conversations.get(player.getUniqueId());

        return object.stage;
    }

    public boolean getBoolean(String message) {
        return message.equals("y") || message.equals("yes") || message.equals("true") || message.equals("ye") || message.equals("yee");
    }

    public boolean isBoolean(String message) {
        return message.equals("y") || message.equals("n") || message.equals("yes") || message.equals("no") || message.equals("true") || message.equals("false") || message.equals("nah") || message.equals("ye") || message.equals("yee");

    }
}
