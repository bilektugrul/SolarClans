package io.github.bilektugrul.solarclans.command;

import com.hakan.core.HCore;
import io.github.bilektugrul.solarclans.SolarClans;
import io.github.bilektugrul.solarclans.clan.Clan;
import io.github.bilektugrul.solarclans.clan.ClanManager;
import io.github.bilektugrul.solarclans.user.User;
import io.github.bilektugrul.solarclans.user.UserManager;
import io.github.bilektugrul.solarclans.util.Utils;
import me.despical.commandframework.Command;
import me.despical.commandframework.CommandArguments;
import me.despical.commandframework.CommandFramework;
import me.despical.commons.string.StringMatcher;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PlayerCommands extends AbstractCommand {

    private final Economy economy;

    public PlayerCommands(SolarClans plugin) {
        super(plugin);

        this.economy = plugin.getVaultManager().getEconomy();
    }

    @Command(
            name = "clan",
            aliases = "c",
            desc = "Clans main command",
            allowInfiniteArgs = true,
            senderType = Command.SenderType.BOTH
    )
    public void mainCommand(CommandArguments arguments) {
        if (!arguments.isArgumentsEmpty()) return;

        if (arguments.getSender() instanceof Player player) {
            long cost = Utils.getLong("new-clan-cost");
            player.sendMessage(Utils.getMessage("main-command", player).replace("%cost%", String.valueOf(cost)));
        }
    }

    private final Set<Player> createConfirmWaiting = new HashSet<>();

    @Command(
            name = "clan.create",
            aliases = "c.c",
            desc = "Clans main command",
            min = 1,
            max = 1,
            senderType = Command.SenderType.PLAYER
    )
    public void createCommand(CommandArguments arguments) {
        Player player = arguments.getSender();
        User user = userManager.getUser(player);

        int required = Utils.getInt("new-clan-cost");
        int min = Utils.getInt("minimum-name-length");
        int max = Utils.getInt("maximum-name-length");

        if (!economy.has(player, required)) {
            double current = economy.getBalance(player);
            player.sendMessage(Utils.getMessage("not-enough-balance", player)
                    .replace("%remaining%", String.valueOf(required - current)));
            return;
        }

        if (user.hasClan()) {
            player.sendMessage(Utils.getMessage("already-have-clan", player));
            return;
        }

        String clanName = arguments.getArgument(0);
        if (clanName.length() < min) {
            player.sendMessage(Utils.getMessage("minimum-name-length", player).replace("%length%", String.valueOf(min)));
            return;
        }

        if (clanName.length() > max) {
            player.sendMessage(Utils.getMessage("maximum-name-length", player).replace("%length%", String.valueOf(max)));
            return;
        }

        if (createConfirmWaiting.contains(player)) {
            clanManager.createClan(clanName, player);
            player.sendMessage(Utils.getMessage("created", player)
                    .replace("%name%", clanName));
            createConfirmWaiting.remove(player);
            return;
        }

        createConfirmWaiting.add(player);
        HCore.syncScheduler()
                .after(5000, TimeUnit.MILLISECONDS)
                .run(() -> createConfirmWaiting.remove(player));
        player.sendMessage(Utils.getMessage("new-clan-confirm", player).replace("%cost%", String.valueOf(required)));
    }

    @Command(
            name = "clan.invite",
            aliases = "c.inv",
            min = 1,
            max = 1,
            desc = "Clans invite command",
            senderType = Command.SenderType.PLAYER
    )
    public void inviteCommand(CommandArguments arguments) {
        Player player = arguments.getSender();
        User user = userManager.getUser(player);
        if (!user.hasClan()) {
            player.sendMessage(Utils.getMessage("not-in-a-clan", player));
            return;
        }

        if (!user.isClanOwner()) {
            player.sendMessage(Utils.getMessage("not-the-owner", player));
            return;
        }

        Player invited = Bukkit.getPlayer(arguments.getArgument(0));
        if (invited == null) {
            player.sendMessage(Utils.getMessage("player-not-found", player));
            return;
        }

        User invitedUser = userManager.getUser(invited);
        long clanID = user.getClanID();
        if (invitedUser.isInvited(clanID)) {
            player.sendMessage(Utils.getMessage("already-invited", player)
                    .replace("%invited%", invited.getName()));
            return;
        }

        if (invitedUser.hasClan()) {
            player.sendMessage(Utils.getMessage("invited-have-clan", player)
                    .replace("%invited%", invited.getName()));
            return;
        }

        Clan clan = clanManager.getClan(clanID);
        invitedUser.addInvitation(clanID);

        player.sendMessage(Utils.getMessage("sent-invite", player).replace("%invited%", invited.getName()));

        invited.sendMessage(Utils.getMessage("new-invite", invited)
                .replace("%clan%", clan.getName())
                .replace("%inviter%", player.getName()));

        HCore.syncScheduler()
                .after(20000, TimeUnit.MILLISECONDS)
                .run(() -> {
                    if (invitedUser.hasClan()) {
                        invitedUser.removeInvitation(clanID);
                    }

                    if (invitedUser.isInvited(clanID)) {
                        invitedUser.removeInvitation(clanID);
                        invited.sendMessage(Utils.getMessage("invite-expired", invited).replace("%clan%", clan.getName()));
                        player.sendMessage(Utils.getMessage("sent-invite-expired", invited).replace("%invited%", invited.getName()));
                    }}
                );
    }

    @Command(
            name = "clan.join",
            aliases = "c.j",
            min = 1,
            max = 1,
            desc = "Clans join command",
            senderType = Command.SenderType.PLAYER
    )
    public void joinCommand(CommandArguments arguments) {
        Player player = arguments.getSender();
        User user = userManager.getUser(player);

        if (user.hasClan()) {
            player.sendMessage(Utils.getMessage("already-have-clan", player));
            return;
        }

        String clanName = arguments.getArgument(0);
        Clan clan = clanManager.getClan(clanName);
        long clanID = clan.getID();
        if (!user.isInvited(clanID)) {
            player.sendMessage(Utils.getMessage("not-invited", player));
            return;
        }

        clanManager.joinClan(player, clan);
    }

    private final Set<Player> leaveConfirmWaiting = new HashSet<>();

    @Command(
            name = "clan.leave",
            aliases = "c.l",
            desc = "Clans leave command",
            senderType = Command.SenderType.PLAYER
    )
    public void leaveCommand(CommandArguments arguments) {
        Player player = arguments.getSender();
        User user = userManager.getUser(player);

        if (!user.hasClan()) {
            player.sendMessage(Utils.getMessage("not-in-a-clan", player));
            return;
        }

        if (user.isClanOwner()) {
            player.sendMessage(Utils.getMessage("own-clan", player));
            return;
        }

        if (leaveConfirmWaiting.contains(player)) {
            clanManager.leaveClan(player);
            leaveConfirmWaiting.remove(player);
            return;
        }

        leaveConfirmWaiting.add(player);
        HCore.syncScheduler()
                .after(5000, TimeUnit.MILLISECONDS)
                .run(() -> leaveConfirmWaiting.remove(player));

        player.sendMessage(Utils.getMessage("leave-confirm", player));
    }

    private final Set<Player> disbandConfirmWaiting = new HashSet<>();

    @Command(
            name = "clan.disband",
            aliases = "c.d",
            desc = "Clans disband command",
            senderType = Command.SenderType.PLAYER
    )
    public void disbandCommand(CommandArguments arguments) {
        Player player = arguments.getSender();
        User user = userManager.getUser(player);

        if (!user.hasClan()) {
            player.sendMessage(Utils.getMessage("not-in-a-clan", player));
            return;
        }

        if (!user.isClanOwner()) {
            player.sendMessage(Utils.getMessage("not-the-owner", player));
            return;
        }

        if (disbandConfirmWaiting.contains(player)) {
            clanManager.disbandClan(user.getClanID());
            disbandConfirmWaiting.remove(player);
            return;
        }

        disbandConfirmWaiting.add(player);
        HCore.syncScheduler()
                .after(5000, TimeUnit.MILLISECONDS)
                .run(() -> disbandConfirmWaiting.remove(player));

        player.sendMessage(Utils.getMessage("disband-confirm", player));
    }

}