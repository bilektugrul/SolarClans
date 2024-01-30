package io.github.bilektugrul.solarclans.economy;

import io.github.bilektugrul.solarclans.SolarClans;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultManager {

    private final SolarClans plugin;
    private Economy economy;

    public VaultManager(SolarClans plugin) {
        this.plugin = plugin;
        setupEconomy();
    }

    public void setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        economy = rsp.getProvider();
    }

    public Economy getEconomy() {
        return economy;
    }

}