package io.github.bilektugrul.solarclans;

import io.github.bilektugrul.solarclans.clan.ClanManager;
import io.github.bilektugrul.solarclans.command.AbstractCommand;
import io.github.bilektugrul.solarclans.command.PlayerCommands;
import io.github.bilektugrul.solarclans.economy.VaultManager;
import io.github.bilektugrul.solarclans.listener.PlayerListener;
import io.github.bilektugrul.solarclans.user.UserManager;
import me.despical.commandframework.CommandFramework;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public final class SolarClans extends JavaPlugin {

    private CommandFramework commandFramework;
    private VaultManager vaultManager;
    private ClanManager clanManager;
    private UserManager userManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        vaultManager = new VaultManager(this);
        clanManager = new ClanManager(this);
        userManager = new UserManager(this);

        clanManager.setUserManager(userManager);

        for (Player player : Bukkit.getOnlinePlayers()) {
            userManager.loadUser(player);
        }

        commandFramework = new CommandFramework(this);
        AbstractCommand.registerCommands(this);

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
    }

    @Override
    public void onDisable() {
        try {
            userManager.saveUsers();
            clanManager.saveClans();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public VaultManager getVaultManager() {
        return vaultManager;
    }

    public ClanManager getClanManager() {
        return clanManager;
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public CommandFramework getCommandFramework() {
        return commandFramework;
    }

}