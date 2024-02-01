package io.github.bilektugrul.solarclans.command;

import io.github.bilektugrul.solarclans.SolarClans;
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
            desc = "Clans kick command",
            senderType = Command.SenderType.PLAYER
    )
    public void vaultCommand(CommandArguments arguments) {
        Player player = arguments.getSender();
        User user = userManager.getUser(player);

        if (!user.hasClan()) {
            player.sendMessage(Utils.getMessage("not-in-a-clan", player));
            return;
        }

        Inventory inventory = plugin.getServer().createInventory(null, 6 * 9, Utils.getMessage("vault-chest-name", player));
        player.openInventory(inventory);
        player.setMetadata("clans-vault-open", new FixedMetadataValue(plugin, true));
        YamlConfiguration data = user.getClan().getData();

        if (data.getConfigurationSection("vault") != null) {
            for (String slot : data.getConfigurationSection("vault").getKeys(false)) {
                int slotInt = Integer.parseInt(slot);

                ItemStack item = data.getItemStack("vault." + slot);
                inventory.setItem(slotInt, item);
            }
        }
    }

}