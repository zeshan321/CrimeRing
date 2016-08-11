package raids;

import com.zeshanaslam.crimering.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PartyCommands implements CommandExecutor {

    private final Main plugin;

    public PartyCommands(Main plugin) {
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if (commandLabel.equalsIgnoreCase("party")) {
            if (args.length == 0) {
                sender.sendMessage(ChatColor.GOLD + "/party create");
                sender.sendMessage(ChatColor.GOLD + "/party invite <name>");
                sender.sendMessage(ChatColor.GOLD + "/party kick <name>");
                sender.sendMessage(ChatColor.GOLD + "/party accept");
                sender.sendMessage(ChatColor.GOLD + "/party leave");
                sender.sendMessage(ChatColor.GOLD + "/party list");
                return false;
            }

            Player player = (Player) sender;
            if (args[0].equalsIgnoreCase("create")) {

                if (getParty(player) != null) {
                    player.sendMessage(ChatColor.RED + "[Party] " + ChatColor.GOLD + "You are already in a party!");
                    return false;
                }

                Main.instance.raidManager.parties.add(new PartyObject(UUID.randomUUID().toString(), player));
                player.sendMessage(ChatColor.RED + "[Party] " + ChatColor.GOLD + "You have created party!");
            }

            if (args[0].equalsIgnoreCase("invite")) {
                if (args.length != 2) {
                    sender.sendMessage(ChatColor.RED + "[Party] " + ChatColor.GOLD + "/party invite <name>");
                    return false;
                }

                PartyObject partyObject = getParty(player);
                if (partyObject == null) {
                    player.sendMessage(ChatColor.RED + "[Party] " + ChatColor.GOLD + "You are not in a party!");
                    return false;
                }

                if (!isOwner(player)) {
                    player.sendMessage(ChatColor.RED + "[Party] " + ChatColor.GOLD + "You are not the owner of this party!");
                    return false;
                }

                Player invitePlayer = Bukkit.getPlayer(args[1]);
                if (invitePlayer == null) {
                    player.sendMessage(ChatColor.RED + "[Party] " + ChatColor.GOLD + "Unable to find " + args[1] + "!");
                    return false;
                }

                if (getParty(invitePlayer) != null) {
                    player.sendMessage(ChatColor.RED + "[Party] " + ChatColor.GOLD + "" + invitePlayer.getName() + " is already in a party!");
                    return false;
                }

                if (invitePlayer.getName().equals(player.getName())) {
                    player.sendMessage(ChatColor.RED + "[Party] " + ChatColor.GOLD + "You can't invite yourself!");
                    return false;
                }

                Main.instance.raidManager.invites.add(new InviteObject(partyObject, invitePlayer));
                partyObject.sendMessage(ChatColor.RED + "[Party] " + ChatColor.GOLD + invitePlayer.getName() + " has been invited to your party!");

                invitePlayer.sendMessage(ChatColor.RED + "[Party] " + ChatColor.GOLD + "You have been invited to " + player.getName() + "'s party! Type " + ChatColor.GREEN + "'/party accept' " + ChatColor.GOLD + "to accept.");
            }

            if (args[0].equalsIgnoreCase("accept")) {
                if (getParty(player) != null) {
                    player.sendMessage(ChatColor.RED + "[Party] " + ChatColor.GOLD + "You are already in a party!");
                    return false;
                }

                InviteObject invite = getInvite(player);
                if (invite == null) {
                    player.sendMessage(ChatColor.RED + "[Party] " + ChatColor.GOLD + "You have not been invited to a party!");
                    return false;
                }

                invite.party.addMember(player);
                invite.party.sendMessage(ChatColor.RED + "[Party] " + ChatColor.GOLD + player.getName() + " has joined the party!");

                Main.instance.raidManager.parties.remove(invite);
            }

            if (args[0].equalsIgnoreCase("leave")) {
                PartyObject partyObject = getParty(player);

                if (partyObject == null) {
                    player.sendMessage(ChatColor.RED + "[Party] " + ChatColor.GOLD + "You are not in a party!");
                    return false;
                }

                if (isOwner(player)) {
                    partyObject.sendMessage(ChatColor.RED + "[Party] " + ChatColor.GOLD + "The party has been disbanded by " + player.getName());

                    for (int a = 0; a < Main.instance.raidManager.invites.size(); a++) {
                        if ((Main.instance.raidManager.invites.get(a)).party == getParty(player)) {
                            Main.instance.raidManager.invites.remove(a);
                        }
                    }

                    Main.instance.raidManager.parties.remove(getParty(player));
                    return false;
                }

                partyObject.sendMessage(ChatColor.RED + "[Party] " + ChatColor.GOLD + player.getName() + " has left the party.");
                partyObject.removeMember(player);
            }

            if (args[0].equalsIgnoreCase("kick")) {
                if (args.length != 2) {
                    sender.sendMessage(ChatColor.RED + "[Party] " + ChatColor.GOLD + "/party kick <name>");
                    return false;
                }

                PartyObject partyObject = getParty(player);
                if (partyObject == null) {
                    player.sendMessage(ChatColor.RED + "[Party] " + ChatColor.GOLD + "You are not in a party!");
                    return false;
                }

                if (!isOwner(player)) {
                    player.sendMessage(ChatColor.RED + "[Party] " + ChatColor.GOLD + "You are not the owner of this party!");
                    return false;
                }

                Player invitePlayer = Bukkit.getPlayer(args[1]);
                if (invitePlayer == null) {
                    player.sendMessage(ChatColor.RED + "[Party] " + ChatColor.GOLD + "Unable to find " + args[1] + "!");
                    return false;
                }

                if (partyObject != getParty(invitePlayer)) {
                    player.sendMessage(ChatColor.RED + "[Party] " + ChatColor.GOLD + invitePlayer.getName() + " is not in your party!");
                    return false;
                }

                partyObject.sendMessage(ChatColor.RED + "[Party] " + ChatColor.GOLD + invitePlayer.getName() + " has been kicked from the party.");
                partyObject.removeMember(invitePlayer);
            }

            if (args[0].equalsIgnoreCase("list")) {
                PartyObject partyObject = getParty(player);
                if (partyObject == null) {
                    player.sendMessage(ChatColor.RED + "[Party] " + ChatColor.GOLD + "You are not in a party!");
                    return false;
                }

                player.sendMessage("Party Members (" + partyObject.size() + "): " + ChatColor.GREEN + partyObject.membersList());
            }
        }
        return false;
    }

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
