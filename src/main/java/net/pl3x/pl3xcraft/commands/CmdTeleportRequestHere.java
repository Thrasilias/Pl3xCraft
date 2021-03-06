package net.pl3x.pl3xcraft.commands;

import net.pl3x.pl3xcraft.Pl3xCraft;
import net.pl3x.pl3xcraft.configuration.Lang;
import net.pl3x.pl3xcraft.configuration.PlayerConfig;
import net.pl3x.pl3xcraft.request.TpaHereRequest;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class CmdTeleportRequestHere implements TabExecutor {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            String name = args[0].trim().toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .filter(player -> player.getName().toLowerCase().startsWith(name))
                    .map(Player::getName).collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            Lang.send(sender, Lang.PLAYER_COMMAND);
            return true;
        }

        if (!sender.hasPermission("command.teleportrequesthere")) {
            Lang.send(sender, Lang.COMMAND_NO_PERMISSION);
            return true;
        }

        if (args.length < 1) {
            Lang.send(sender, Lang.NO_PLAYER_SPECIFIED);
            return false;
        }

        //noinspection deprecation
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            Lang.send(sender, Lang.PLAYER_NOT_ONLINE);
            return true;
        }

        // check for pending requests
        PlayerConfig targetConfig = PlayerConfig.getConfig(target);
        if (targetConfig.getRequest() != null) {
            Lang.send(sender, Lang.TARGET_HAS_PENDING_REQUEST
                    .replace("{target}", target.getName()));
            return true;
        }

        // check for toggles and overrides
        if (target.hasPermission("command.teleporttoggle") &&
                !sender.hasPermission("command.teleport.override") &&
                !targetConfig.allowTeleports()) {
            Lang.send(sender, Lang.TELEPORT_TOGGLED_OFF);
            return true;
        }

        // Create new request
        targetConfig.setRequest(new TpaHereRequest(Pl3xCraft.getInstance(), (Player) sender, target));

        Lang.send(sender, Lang.TELEPORT_REQUESTHERE_REQUESTER
                .replace("{target}", target.getName()));
        return true;
    }
}
