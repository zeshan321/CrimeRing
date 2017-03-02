package bounty;

import com.zeshanaslam.crimering.Main;
import conversation.Conversation;
import conversation.ConversationCallback;
import conversation.ConversationObject;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class BountyManager {

    public BountyManager() {
        try (Stream<Path> paths = Files.walk(Paths.get("plugins/CrimeRing/bounties/"))) {
            paths.forEach(filePath -> {
                if (Files.isRegularFile(filePath)) {
                    File file = filePath.toFile();

                    System.out.println(file.getName());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createBounty(Player player) {
        Main.instance.conversation.startConversation(new ConversationObject(player.getUniqueId(), Conversation.ConvoType.STRING, new ConversationCallback() {
            @Override
            public void onValid(Player player, String value) {
                player.sendMessage("Passed: " + value);
            }

            @Override
            public void onInvalid(Player player, String value) {
                player.sendMessage("Failed: " + value);
            }
        }));
    }
}
