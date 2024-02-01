package io.github.bilektugrul.solarclans.command;

import io.github.bilektugrul.solarclans.SolarClans;
import io.github.bilektugrul.solarclans.clan.ClanManager;
import io.github.bilektugrul.solarclans.user.UserManager;
import me.despical.commandframework.CommandFramework;
import me.despical.commons.string.StringMatcher;

import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractCommand {

    protected final SolarClans plugin;
    protected final ClanManager clanManager;
    protected final UserManager userManager;
    protected final CommandFramework commandFramework;

    public AbstractCommand(SolarClans plugin) {
        this.plugin = plugin;
        this.clanManager = plugin.getClanManager();
        this.userManager = plugin.getUserManager();

        this.commandFramework = plugin.getCommandFramework();
        this.commandFramework.registerCommands(this);

        this.commandFramework.setMatchFunction(arguments -> {
            if (arguments.isArgumentsEmpty()) return false;

            String label = arguments.getLabel(), arg = arguments.getArgument(0);

            List<StringMatcher.Match> matches = StringMatcher.match(arg, commandFramework.getCommands().stream().map(cmd -> cmd.name().replace(label + ".", "")).collect(Collectors.toList()));

            if (!matches.isEmpty()) {
                arguments.sendMessage("§cDid you mean §f%command%§c?".replace("%command%", label + " " + matches.get(0).getMatch()));
                return true;
            }

            return false;
        });
    }

    public static void registerCommands(SolarClans plugin) {
        new PlayerCommands(plugin);
        new AdminCommands(plugin);
        new VaultCommand(plugin);
    }
}