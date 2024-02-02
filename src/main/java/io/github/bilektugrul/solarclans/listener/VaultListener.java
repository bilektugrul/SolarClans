package io.github.bilektugrul.solarclans.listener;

import io.github.bilektugrul.solarclans.SolarClans;
import io.github.bilektugrul.solarclans.clan.Clan;
import io.github.bilektugrul.solarclans.user.User;
import io.github.bilektugrul.solarclans.user.UserManager;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;

public class VaultListener implements Listener {

    private final SolarClans plugin;
    private final UserManager userManager;

    public VaultListener(SolarClans plugin) {
        this.plugin = plugin;
        this.userManager = plugin.getUserManager();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();
        User user = userManager.getUser(player);

        if (!user.hasClan()) return;
        if (!(player.hasMetadata("clans-vault-open") || player.hasMetadata("clans-admin-vault-open"))) return;

        player.removeMetadata("clans-vault-open", plugin);

        Clan clan = user.getClan();
        if (player.hasMetadata("clans-admin-vault-open")) {
            clan = (Clan) player.getMetadata("clans-admin-vault-open").get(0).value();
            player.removeMetadata("clans-admin-vault-open", plugin);
        }

        YamlConfiguration data = clan.getData();
        Inventory inventory = e.getInventory();
        if (inventory.getContents().length == 0) {
            data.set("vault", null);
            return;
        }

        if (data.getConfigurationSection("vault") != null) {
            for (String slot : data.getConfigurationSection("vault").getKeys(false)) {
                int slotInt = Integer.parseInt(slot);

                if (inventory.getItem(slotInt) == null) data.set("vault." + slot, null);
            }
        }

        int slot = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item == null) {
                slot++;
                continue;
            }

            data.set("vault." + slot, item);
            slot++;
        }

        if (clan.isDisbanded()) {
            try {
                clan.save();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        if (!player.hasMetadata("clans-vault-open") || !player.hasMetadata("clans-admin-vault-open")) return;

        User user = userManager.getUser(player);
        Clan clan = user.getClan();
        if (player.hasMetadata("clans-admin-vault-open")) {
            clan = (Clan) player.getMetadata("clans-admin-vault-open").get(0).value();
        }

        Clan finalClan = clan;
        plugin.getServer().getScheduler().runTask(plugin, () -> finalClan.getData().set("vault." + e.getSlot(), null));
    }

}