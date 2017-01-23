package raids;

import com.zeshanaslam.crimering.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PartyCommands implements Listener, CommandExecutor {

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

            PartyAPI partyAPI = new PartyAPI();
            if (args[0].equalsIgnoreCase("create")) {

                if (partyAPI.getParty(player) != null) {
                    player.sendMessage(ChatColor.RED + "[Party] " + ChatColor.GOLD + "You are already in a party!");
                    return false;
                }

                plugin.raidManager.parties.add(new PartyObject(UUID.randomUUID().toString(), player));
                player.sendMessage(ChatColor.RED + "[Party] " + ChatColor.GOLD + "You have created a party!");
            }

            if (args[0].equalsIgnoreCase("invite")) {
                if (args.length != 2) {
                    sender.sendMessage(ChatColor.RED + "[Party] " + ChatColor.GOLD + "/party invite <name>");
                    return false;
                }

                PartyObject partyObject = partyAPI.getParty(player);
                if (partyObject == null) {
                    player.sendMessage(ChatColor.RED + "[Party] " + ChatColor.GOLD + "You are not in a party!");
                    return false;
                }

                if (!partyAPI.isOwner(player)) {
                    player.sendMessage(ChatColor.RED + "[Party] " + ChatColor.GOLD + "You are not the owner of this party!");
                    return false;
                }

                Player invitePlayer = Bukkit.getPlayer(args[1]);
                if (invitePlayer == null) {
                    player.sendMessage(ChatColor.RED + "[Party] " + ChatColor.GOLD + "Unable to find " + args[1] + "!");
                    return false;
                }

                if (partyAPI.getParty(invitePlayer) != null) {
                    player.sendMessage(ChatColor.RED + "[Party] " + ChatColor.GOLD + "" + invitePlayer.getName() + " is already in a party!");
                    return false;
                }

                if (invitePlayer.getName().equals(player.getName())) {
                    player.sendMessage(ChatColor.RED + "[Party] " + ChatColor.GOLD + "You can't invite yourself!");
                    return false;
                }

                if (plugin.raidManager.isInRaid(player)) {
                    player.sendMessage(ChatColor.RED + "[Party] " + ChatColor.GOLD + "You can't invite a player while in queue or in a raid!!");
                    return false;
                }

                InviteObject inviteObject = partyAPI.getInvite(player);
                if (inviteObject != null) {
                    plugin.raidManager.invites.remove(inviteObject);
                }

                partyAPI.removeInvites(invitePlayer);
                plugin.raidManager.invites.add(new InviteObject(partyObject, invitePlayer));
                partyObject.sendMessage(ChatColor.RED + "[Party] " + ChatColor.GOLD + invitePlayer.getName() + " has been invited to your party!");

                invitePlayer.sendMessage(ChatColor.RED + "[Party] " + ChatColor.GOLD + "You have been invited to " + player.getName() + "'s party! Type " + ChatColor.GREEN + "'/party accept' " + ChatColor.GOLD + "to accept.");
            }

            if (args[0].equalsIgnoreCase("accept")) {
                if (partyAPI.getParty(player) != null) {
                    player.sendMessage(ChatColor.RED + "[Party] " + ChatColor.GOLD + "You are already in a party!");
                    return false;
                }

                InviteObject invite = partyAPI.getInvite(player);
                if (invite == null) {
                    player.sendMessage(ChatColor.RED + "[Party] " + ChatColor.GOLD + "You have not been invited to a party!");
                    return false;
                }

                if (plugin.raidManager.isInRaid(invite.party.getOwner())) {
                    plugin.raidManager.invites.remove(invite);

                    player.sendMessage(ChatColor.RED + "You can't join a party while the party is in a raid!");
                    return false;
                }

                invite.party.addMember(player);
                invite.party.sendMessage(ChatColor.RED + "[Party] " + ChatColor.GOLD + player.getName() + " has joined the party!");

                plugin.raidManager.parties.remove(invite);
            }

            if (args[0].equalsIgnoreCase("leave")) {
                PartyObject partyObject = partyAPI.getParty(player);

                if (partyObject == null) {
                    player.sendMessage(ChatColor.RED + "[Party] " + ChatColor.GOLD + "You are not in a party!");
                    return false;
                }

                if (plugin.raidManager.isInRaid(player)) {
                    if (partyAPI.isOwner(player)) {
                        if (partyObject.getMembers().isEmpty()) {
                            plugin.raidManager.cancelRaid(plugin.raidManager.getRaid(player));
                        }
                    }

                    RaidObject raidObject = plugin.raidManager.getRaid(player);
                    raidObject.removeMember(player);
                }


                partyObject.sendMessage(ChatColor.RED + "[Party] " + ChatColor.GOLD + player.getName() + " has left the party.");
                partyObject.removeMember(player);
            }

            if (args[0].equalsIgnoreCase("kick")) {
                if (args.length != 2) {
                    sender.sendMessage(ChatColor.RED + "[Party] " + ChatColor.GOLD + "/party kick <name>");
                    return false;
                }

                PartyObject partyObject = partyAPI.getParty(player);
                if (partyObject == null) {
                    player.sendMessage(ChatColor.RED + "[Party] " + ChatColor.GOLD + "You are not in a party!");
                    return false;
                }

                if (!partyAPI.isOwner(player)) {
                    player.sendMessage(ChatColor.RED + "[Party] " + ChatColor.GOLD + "You are not the owner of this party!");
                    return false;
                }

                Player invitePlayer = Bukkit.getPlayer(args[1]);
                if (invitePlayer == null) {
                    player.sendMessage(ChatColor.RED + "[Party] " + ChatColor.GOLD + "Unable to find " + args[1] + "!");
                    return false;
                }

                if (partyObject != partyAPI.getParty(invitePlayer)) {
                    player.sendMessage(ChatColor.RED + "[Party] " + ChatColor.GOLD + invitePlayer.getName() + " is not in your party!");
                    return false;
                }

                partyObject.sendMessage(ChatColor.RED + "[Party] " + ChatColor.GOLD + invitePlayer.getName() + " has been kicked from the party.");
                partyObject.removeMember(invitePlayer);
            }

            if (args[0].equalsIgnoreCase("list")) {
                PartyObject partyObject = partyAPI.getParty(player);
                if (partyObject == null) {
                    player.sendMessage(ChatColor.RED + "[Party] " + ChatColor.GOLD + "You are not in a party!");
                    return false;
                }

                player.sendMessage("Party Members (" + partyObject.size() + "): " + ChatColor.GREEN + partyObject.membersList());
            }
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PartyAPI partyAPI = new PartyAPI();
        PartyObject partyObject = partyAPI.getParty(player);

        if (partyObject == null) {
            return;
        }


        if (plugin.raidManager.isInRaid(player)) {
            if (partyAPI.isOwner(player)) {
                if (partyObject.getMembers().isEmpty()) {
                    plugin.raidManager.cancelRaid(plugin.raidManager.getRaid(player));
                }
            }

            RaidObject raidObject = plugin.raidManager.getRaid(player);
            raidObject.removeMember(player);
        }

        partyObject.sendMessage(ChatColor.RED + "[Party] " + ChatColor.GOLD + player.getName() + " has left the party.");
        partyObject.removeMember(player);
    }
}
