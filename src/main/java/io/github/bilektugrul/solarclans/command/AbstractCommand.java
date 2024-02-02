package io.github.bilektugrul.solarclans.command;

import io.github.bilektugrul.solarclans.SolarClans;
import io.github.bilektugrul.solarclans.clan.ClanManager;
import io.github.bilektugrul.solarclans.user.UserManager;
import me.despical.commandframework.CommandFramework;
import net.milkbowl.vault.economy.Economy;

public abstract class AbstractCommand {

    protected final SolarClans plugin;
    protected final ClanManager clanManager;
    protected final UserManager userManager;
    protected final CommandFramework commandFramework;
    protected final Economy economy;


    public AbstractCommand(SolarClans plugin) {
        this.plugin = plugin;
        this.clanManager = plugin.getClanManager();
        this.userManager = plugin.getUserManager();
        this.economy = plugin.getVaultManager().getEconomy();

        this.commandFramework = plugin.getCommandFramework();
        this.commandFramework.registerCommands(this);
    }

    public static void registerCommands(SolarClans plugin) {
        new PlayerCommands(plugin);
        new AdminCommands(plugin);
        new VaultCommand(plugin);
    }
}