package net.pl3x.pl3xcraft.commands;

import net.pl3x.pl3xcraft.Pl3xCraft;
import net.pl3x.pl3xcraft.configuration.Lang;
import net.pl3x.pl3xcraft.configuration.PlayerConfig;
import net.pl3x.pl3xcraft.hook.Vault;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class CmdSetHome implements TabExecutor {
    private final Pl3xCraft plugin;

    public CmdSetHome(Pl3xCraft plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            if (args.length == 1) {
                return PlayerConfig.getConfig(plugin, (Player) sender).getMatchingHomeNames(args[0]);
            }
            if (args.length == 2) {
                return Bukkit.getOnlinePlayers().stream()
                        .filter(player -> player.getName().toLowerCase().startsWith(args[1].toLowerCase()))
                        .map(Player::getName).collect(Collectors.toList());
            }
        }
        return null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            Lang.send(sender, Lang.PLAYER_COMMAND);
            return true;
        }

        if (!sender.hasPermission("command.sethome")) {
            Lang.send(sender, Lang.COMMAND_NO_PERMISSION);
            return true;
        }

        Player player = (Player) sender;
        PlayerConfig config;
        String home = (args.length > 0) ? args[0] : "home";
        if (home.equalsIgnoreCase("bed")) {
            Lang.send(sender, Lang.INVALID_HOME_NAME);
            return true;
        }

        int limit;
        if (args.length > 1) {
            if (!sender.hasPermission("command.sethome.other")) {
                Lang.send(sender, Lang.COMMAND_NO_PERMISSION);
                return true;
            }

            //noinspection deprecation (fucking bukkit)
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            if (target == null) {
                Lang.send(sender, Lang.PLAYER_NOT_FOUND);
                return true;
            }

            if (Vault.hasPermission(target, "command.sethome.exempt")) {
                Lang.send(sender, Lang.HOME_SET_EXEMPT);
                return true;
            }

            config = PlayerConfig.getConfig(plugin, target);

            if (config == null) {
                Lang.send(sender, Lang.PLAYER_NOT_FOUND);
                return true;
            }

            limit = config.getHomesLimit();
        } else {
            config = PlayerConfig.getConfig(plugin, player);
            limit = config.getHomesLimit();
        }

        int count = config.getCount();
        if (limit >= 0 && count >= limit) {
            Lang.send(sender, Lang.HOME_SET_MAX
                    .replace("{limit}", Integer.toString(limit)));
            return true;
        }

        config.setHome(home, player.getLocation());
        Lang.send(sender, Lang.HOME_SET
                .replace("{home}", home));
        return true;
    }
}
