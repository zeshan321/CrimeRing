package conversation;

import java.util.UUID;

public class ConversationObject {

    public UUID player;
    public Conversation.ConvoType type;
    public ConversationCallback callback;

    public ConversationObject(UUID player, Conversation.ConvoType type, ConversationCallback callback) {
        this.player = player;
        this.type = type;
        this.callback = callback;
    }
}
