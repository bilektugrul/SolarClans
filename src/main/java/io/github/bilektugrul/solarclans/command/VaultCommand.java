package io.github.bilektugrul.solarclans.command;

import io.github.bilektugrul.solarclans.SolarClans;
import io.github.bilektugrul.solarclans.clan.Clan;
import io.github.bilektugrul.solarclans.user.User;
import io.github.bilektugrul.solarclans.util.Utils;
import me.despical.commandframework.Command;
import me.despical.commandframework.CommandArguments;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

public class VaultCommand extends AbstractCommand {

    public VaultCommand(SolarClans plugin) {
        super(plugin);
    }

    @Command(
            name = "clan.vault",
            aliases = {"c.vault", "c.v", "clans.v", "clans.vault"},
            desc = "Clans vault command",
            senderType = Command.SenderType.PLAYER
    )
    public void vaultCommand(CommandArguments arguments) {
        Player player = arguments.getSender();
        User user = userManager.getUser(player);

        if (!user.hasClan()) {
            player.sendMessage(Utils.getMessage("not-in-a-clan", player));
            return;
        }

        Clan clan = user.getClan();
        Inventory inventory = clan.getVaultInventory();
        if (inventory == null) {
            inventory = plugin.getServer().createInventory(null, Utils.getInt("vault-size"), Utils.getMessage("vault-chest-name", player)
                    .replace("%clan%", clan.getName()));
            clan.setVaultInventory(inventory);
        }

        player.openInventory(inventory);
        player.setMetadata("clans-vault-open", new FixedMetadataValue(plugin, true));
        YamlConfiguration data = clan.getData();

        if (data.getConfigurationSection("vault") != null) {
            for (String slot : data.getConfigurationSection("vault").getKeys(false)) {
                int slotInt = Integer.parseInt(slot);

                ItemStack item = data.getItemStack("vault." + slot);
                inventory.setItem(slotInt, item);
            }
        }
    }

    @Command(
            name = "clan.admin.vault",
            aliases = {"c.admin.vault", "c.admin.v", "clans.admin.v", "clans.admin.vault"},
            allowInfiniteArgs = true,
            desc = "Clans admin vault command",
            senderType = Command.SenderType.PLAYER
    )
    public void vaultAdminCommand(CommandArguments arguments) {
        Player player = arguments.getSender();
        String arg = arguments.getArgument(0);

        Clan clan;
        if (arguments.getLength() == 2) {
            clan = clanManager.getDisbandedClan(arg);
        } else {
            clan = clanManager.getClan(arg);
        }

        if (clan == null) {
            player.sendMessage(Utils.getMessage("clan-not-available", player));
            return;
        }

        Inventory inventory = clan.getVaultInventory();
        if (inventory == null) {
            inventory = plugin.getServer().createInventory(null, Utils.getInt("vault-size"), Utils.getMessage("vault-chest-name", player)
                    .replace("%clan%", clan.getName()));
            clan.setVaultInventory(inventory);
        }

        player.openInventory(inventory);
        player.setMetadata("clans-admin-vault-open", new FixedMetadataValue(plugin, clan));
        YamlConfiguration data = clan.getData();

        if (data.getConfigurationSection("vault") != null) {
            for (String slot : data.getConfigurationSection("vault").getKeys(false)) {
                int slotInt = Integer.parseInt(slot);

                ItemStack item = data.getItemStack("vault." + slot);
                inventory.setItem(slotInt, item);
            }
        }
    }

}