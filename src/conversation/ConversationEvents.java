package conversation;

import com.zeshanaslam.crimering.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ConversationEvents implements Listener {

    private final Main plugin;

    public ConversationEvents(Main plugin) {
        this.plugin = plugin;
    }


    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (plugin.conversation.conversations.containsKey(player.getUniqueId())) {
            plugin.conversation.endConversation(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (plugin.conversation.conversations.containsKey(player.getUniqueId())) {
            event.setCancelled(true);

            String message = event.getMessage();
            ConversationObject object = plugin.conversation.conversations.get(player.getUniqueId());

            // End task
            Bukkit.getScheduler().cancelTask(object.taskID);

            player.sendMessage(message);
            switch(object.type) {
                case INT:
                    if (message.matches("[0-9]+")) {
                        object.callback.onValid(player, message);
                    } else {
                        object.callback.onInvalid(player, message);
                    }
                    break;

                case DOUBLE:
                    if (message.matches("[0-9]{1,13}(\\\\.[0-9]*)?")) {
                        object.callback.onValid(player, message);
                    } else {
                        object.callback.onInvalid(player, message);
                    }
                    break;

                case STRING:
                    object.callback.onValid(player, message);
                    break;

                case NUMBER:
                    if (message.matches("[0-9]+") || message.matches("[0-9]{1,13}(\\\\.[0-9]*)?")) {
                        object.callback.onValid(player, message);
                    } else {
                        object.callback.onInvalid(player, message);
                    }
                    break;

                case BOOLEAN:
                    if (plugin.conversation.isBoolean(message.toLowerCase())) {
                        object.callback.onValid(player, message.toLowerCase());
                    } else {
                        object.callback.onInvalid(player, message);
                    }
                    break;
            }
        }
    }
}
