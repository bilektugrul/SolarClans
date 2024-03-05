package io.github.bilektugrul.solarclans.command;

import io.github.bilektugrul.solarclans.SolarClans;
import io.github.bilektugrul.solarclans.clan.Clan;
import io.github.bilektugrul.solarclans.user.User;
import io.github.bilektugrul.solarclans.util.Utils;
import me.despical.commandframework.Command;
import me.despical.commandframework.CommandArguments;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AdminCommands extends AbstractCommand {

    public AdminCommands(SolarClans plugin) {
        super(plugin);
    }

    @Command(
            name = "clan.admin",
            aliases = {"clan.admin.help", "c.admin.help", "clans.admin.help", "c.admin", "clans.admin"},
            desc = "Clans main command"
    )
    public void mainCommand(CommandArguments arguments) {
        CommandSender sender = arguments.getSender();

        if (sender.hasPermission("clans.admin")) {
            sender.sendMessage(Utils.getMessage("admin-help-command", sender));
        }
    }

    @Command(
            name = "clan.reload",
            aliases = {"c.r", "c.reload", "clans.reload", "clans.r"},
            desc = "Clans reload command",
            senderType = Command.SenderType.BOTH
    )
    public void reloadCommand(CommandArguments arguments) {
        CommandSender sender = arguments.getSender();;

        if (!sender.hasPermission("clans.admin")) {
            sender.sendMessage(Utils.getMessage("no-permission", sender));
            return;
        }

        plugin.reloadConfig();
        sender.sendMessage(Utils.getMessage("reloaded", sender));
    }

    @Command(
            name = "clan.reloadleaderboard",
            aliases = {"clan.rl", "c.rl", "c.reloadleaderboard", "clans.rl", "clans.reloadleaderboard"},
            desc = "Clans reload leaderboard command",
            senderType = Command.SenderType.BOTH
    )
    public void reloadLeaderboardCommand(CommandArguments arguments) {
        CommandSender sender = arguments.getSender();;

        if (!sender.hasPermission("clans.admin")) {
            sender.sendMessage(Utils.getMessage("no-permission", sender));
            return;
        }

        plugin.saveAndReloadLeaderboards();
        sender.sendMessage(Utils.getMessage("reloaded-leaderboard", sender));
    }

    @Command(
            name = "clan.save",
            aliases = {"c.save", "clans.save"},
            desc = "Clans reload command",
            senderType = Command.SenderType.BOTH
    )
    public void saveCommand(CommandArguments arguments) {
        CommandSender sender = arguments.getSender();;

        if (!sender.hasPermission("clans.admin")) {
            sender.sendMessage(Utils.getMessage("no-permission", sender));
            return;
        }

        plugin.save();
        sender.sendMessage(Utils.getMessage("saved", sender));
    }

    @Command(
            name = "clan.admin.addmember",
            aliases = {"c.admin.addmember", "clans.admin.addmember"},
            min = 2,
            max = 2,
            desc = "Clans reload command"
    )
    public void addMemberCommand(CommandArguments arguments) {
        CommandSender sender = arguments.getSender();

        if (!sender.hasPermission("clans.admin")) {
            sender.sendMessage(Utils.getMessage("no-permission", sender));
        }

        Player playerToAdd = Bukkit.getPlayer(arguments.getArgument(0));
        if (playerToAdd == null) {
            sender.sendMessage(Utils.getMessage("player-not-found", sender));
            return;
        }

        User user = userManager.getUser(playerToAdd);
        if (user.hasClan()) {
            sender.sendMessage(Utils.getMessage("user-has-clan", sender));
            return;
        }

        String clanName = arguments.getArgument(1);
        Clan clan = clanManager.getClan(clanName);
        if (clan == null) {
            sender.sendMessage(Utils.getMessage("clan-not-available", sender));
            return;
        }

        user.setClanID(clan.getID());
        clan.addMembers(user.getName());

        String name = user.getName();
        clanName = clan.getName();

        playerToAdd.sendMessage(Utils.getMessage("admin-added-clan", playerToAdd)
                .replace("%clan%", clanName));
        clan.sendMessage(Utils.getMessage("admin-added-new-member", null)
                .replace("%member%", name));
        clan.updateOnlineMembers();

        sender.sendMessage(Utils.getMessage("admin-added-clan-done", sender)
                .replace("%clan%", clanName)
                .replace("%member%", name));
    }

    @Command(
            name = "clan.admin.kickmember",
            aliases = {"c.admin.km", "c.admin.kickmember", "clans.admin.km", "clans.admin.kickmember"},
            min = 2,
            max = 2,
            desc = "Clans reload command"
    )
    public void kickMemberCommand(CommandArguments arguments) {
        CommandSender sender = arguments.getSender();

        if (!sender.hasPermission("clans.admin")) {
            sender.sendMessage(Utils.getMessage("no-permission", sender));
        }

        Player playerToKick = Bukkit.getPlayer(arguments.getArgument(0));
        if (playerToKick == null) {
            sender.sendMessage(Utils.getMessage("player-not-found", sender));
            return;
        }

        User user = userManager.getUser(playerToKick);
        if (!user.hasClan()) {
            sender.sendMessage(Utils.getMessage("user-not-in-clan", sender));
            return;
        }

        Clan clan = user.getClan();

        user.setClanID(-1);
        clan.getMembers().remove(user.getName());
        clan.updateOnlineMembers();

        String name = user.getName();
        String clanName = user.getName();

        if (playerToKick.hasMetadata("clans-vault-open")) {
            playerToKick.closeInventory();
            playerToKick.removeMetadata("clans-vault-open", plugin);
        }

        playerToKick.sendMessage(Utils.getMessage("admin-kick-clan", playerToKick)
                .replace("%clan%", clanName));
        clan.sendMessage(Utils.getMessage("admin-kicked-member", null)
                .replace("%member%", name));

        sender.sendMessage(Utils.getMessage("admin-kicked", sender)
                .replace("%member%", name)
                .replace("%clan%", clanName));
    }

    @Command(
            name = "clan.admin.setclanleader",
            aliases = {"c.admin.setleader", "c.admin.sl", "clans.admin.sl", "clans.admin.setleader"},
            min = 1,
            max = 1,
            desc = "Clans reload command"
    )
    public void adminSetLeaderCommand(CommandArguments arguments) {
        CommandSender sender = arguments.getSender();

        if (!sender.hasPermission("clans.admin")) {
            sender.sendMessage(Utils.getMessage("no-permission", sender));
        }

        Player newLeader = Bukkit.getPlayer(arguments.getArgument(0));
        if (newLeader == null) {
            sender.sendMessage(Utils.getMessage("player-not-found", sender));
            return;
        }

        User user = userManager.getUser(newLeader);
        if (!user.hasClan()) {
            sender.sendMessage(Utils.getMessage("user-not-in-clan", sender));
            return;
        }

        Clan clan = user.getClan();
        clanManager.changeLeader(user.getClan().getID(), newLeader, true);
        String name = newLeader.getName();

        newLeader.sendMessage(Utils.getMessage("admin-set-leader", newLeader));
        clan.sendMessage(Utils.getMessage("admin-changed-your-leader", null)
                .replace("%member%", name));
        sender.sendMessage(Utils.getMessage("admin-set-leader-done", sender)
                .replace("%clan%", clan.getName())
                .replace("%member%", name));
    }

    @Command(
            name = "clan.admin.disband",
            aliases = {"c.admin.disband", "c.admin.d", "clans.admin.d", "clans.admin.disband"},
            min = 1,
            max = 1,
            desc = "Clans reload command"
    )
    public void adminDisbandCommand(CommandArguments arguments) {
        CommandSender sender = arguments.getSender();

        if (!sender.hasPermission("clans.admin")) {
            sender.sendMessage(Utils.getMessage("no-permission", sender));
        }

        String clanName = arguments.getArgument(0);
        Clan clan = clanManager.getClan(clanName);
        if (clan == null) {
            sender.sendMessage(Utils.getMessage("clan-not-available", sender));
            return;
        }

        clanManager.disbandClan(clan, true);
        clan.sendMessage(Utils.getMessage("admin-disband", null));
        sender.sendMessage(Utils.getMessage("admin-disbanded", sender)
                .replace("%clan%", clan.getName()));
    }

    @Command(
            name = "clan.admin.rename",
            aliases = {"c.admin.rename", "c.admin.rename", "clans.admin.rename", "clans.admin.rename"},
            min = 2,
            max = 2,
            desc = "Clans reload command"
    )
    public void adminRenameCommand(CommandArguments arguments) {
        CommandSender sender = arguments.getSender();

        if (!sender.hasPermission("clans.admin")) {
            sender.sendMessage(Utils.getMessage("no-permission", sender));
        }

        String clanName = arguments.getArgument(0);
        Clan clan = clanManager.getClan(clanName);
        if (clan == null) {
            sender.sendMessage(Utils.getMessage("clan-not-available", sender));
            return;
        }

        clanName = clan.getName();
        String newName = arguments.getArgument(1);

        clan.setName(newName);
        clan.sendMessage(Utils.getMessage("admin-rename", null)
                .replace("%newname%", newName));
        sender.sendMessage(Utils.getMessage("admin-renamed", sender)
                .replace("%clan%", clanName)
                .replace("%newname%", newName));
    }

    @Command(
            name = "clan.admin.info",
            aliases = {"c.admin.info", "c.admin.i", "clans.admin.i", "clans.admin.info"},
            desc = "Clans info command",
            min = 1,
            max = 1,
            senderType = Command.SenderType.PLAYER
    )
    public void adminInfoCommand(CommandArguments arguments) {
        CommandSender sender = arguments.getSender();
        String clanName = arguments.getArgument(0);
        Clan clan = clanManager.getClan(clanName);

        if (clan == null) {
            sender.sendMessage(Utils.getMessage("clan-not-available", sender));
            return;
        }

        String infoMessage = Utils.getMessage("clan-info", sender)
                .replace("%name%", clan.getName())
                .replace("%creator%", clan.getCreator())
                .replace("%owner%", clan.getOwner())
                .replace("%date%", clan.getCreationDate())
                .replace("%size%", String.valueOf(clan.getMembers().size()));

        long totalBalance = 0;

        for (String member : clan.getMembers()) {
            totalBalance += (long) economy.getBalance(Bukkit.getOfflinePlayer(member));
        }

        StringBuilder members = new StringBuilder();
        for (String member : clan.getMembers()) {
            members.append(member).append("\n");
        }

        infoMessage = infoMessage
                .replace("%members%", members)
                .replace("%kills%", String.valueOf(clan.getKills()))
                .replace("%balance%", Utils.moneyWithCommas(totalBalance));

        sender.sendMessage(infoMessage);
    }

}