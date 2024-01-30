package io.github.bilektugrul.solarclans.command;

import io.github.bilektugrul.solarclans.SolarClans;
import io.github.bilektugrul.solarclans.leaderboard.BalanceLeaderboard;
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
            aliases = "c.r",
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
            aliases = "c.rl",
            desc = "Clans reload command",
            senderType = Command.SenderType.BOTH
    )
    public void reloadLeaderboardCommand(CommandArguments arguments) {
        CommandSender sender = arguments.getSender();;

        if (!sender.hasPermission("clans.admin")) {
            sender.sendMessage(Utils.getMessage("no-permission", sender));
            return;
        }

        BalanceLeaderboard.reloadLeaderboard();
        sender.sendMessage(Utils.getMessage("reloaded-leaderboard", sender));
    }

}