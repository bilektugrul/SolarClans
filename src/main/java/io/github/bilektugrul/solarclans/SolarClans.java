package io.github.bilektugrul.solarclans;

import com.hakan.core.HCore;
import io.github.bilektugrul.solarclans.clan.ClanManager;
import io.github.bilektugrul.solarclans.command.AbstractCommand;
import io.github.bilektugrul.solarclans.economy.VaultManager;
import io.github.bilektugrul.solarclans.leaderboard.BalanceLeaderboard;
import io.github.bilektugrul.solarclans.leaderboard.KillLeaderboard;
import io.github.bilektugrul.solarclans.listener.PlayerListener;
import io.github.bilektugrul.solarclans.listener.VaultListener;
import io.github.bilektugrul.solarclans.placeholder.PAPIPlaceholders;
import io.github.bilektugrul.solarclans.user.UserManager;
import io.github.bilektugrul.solarclans.util.Utils;
import me.despical.commandframework.CommandFramework;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

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
            player.removeMetadata("clans-vault-open", this);
            player.removeMetadata("clans-admin-vault-open", this);
            player.closeInventory();

            userManager.loadUser(player);
        }

        commandFramework = new CommandFramework(this);
        AbstractCommand.registerCommands(this);

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new VaultListener(this), this);
        new PAPIPlaceholders(this).register();

        HCore.asyncScheduler()
                .every(Utils.getInt("save-interval"), TimeUnit.MINUTES)
                .run(this::saveAndReloadLeaderboards);
    }

    @Override
    public void onDisable() {
        save();
    }

    public void saveAndReloadLeaderboards() {
        save();

        BalanceLeaderboard.reloadLeaderboard();
        KillLeaderboard.reloadLeaderboard();
    }

    public void save() {
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