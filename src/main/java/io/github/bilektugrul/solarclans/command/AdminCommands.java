package io.github.bilektugrul.solarclans.command;

import io.github.bilektugrul.solarclans.SolarClans;
import io.github.bilektugrul.solarclans.util.Utils;
import me.despical.commandframework.Command;
import me.despical.commandframework.CommandArguments;
import org.bukkit.command.CommandSender;

public class AdminCommands extends AbstractCommand {

    public AdminCommands(SolarClans plugin) {
        super(plugin);
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
            aliases = {"c.rl", "c.reloadleaderboard", "clans.rl", "clans.reloadleaderboard"},
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
            aliases = {"c.save", "c.reload", "clans.save"},
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

}