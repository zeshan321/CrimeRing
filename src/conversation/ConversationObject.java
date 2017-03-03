package conversation;

import com.zeshanaslam.crimering.Main;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

public class ConversationObject {

    public UUID player;
    public Conversation.ConvoType type;
    public ConversationCallback callback;
    public int taskID;

    public long timeoutTicks = 300;

    public ConversationObject(UUID player, Conversation.ConvoType type, ConversationCallback callback) {
        this.player = player;
        this.type = type;
        this.callback = callback;

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (Bukkit.getPlayer(player) != null)
                Main.instance.conversation.endConversation(Bukkit.getPlayer(player));
            }
        }.runTaskLater(Main.instance, timeoutTicks);

        taskID = task.getTaskId();
    }
}
