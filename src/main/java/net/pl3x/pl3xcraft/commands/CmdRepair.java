package net.pl3x.pl3xcraft.commands;

import net.pl3x.pl3xcraft.configuration.Config;
import net.pl3x.pl3xcraft.configuration.Lang;
import net.pl3x.pl3xcraft.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.inventory.ItemStack;

public class CmdRepair implements TabExecutor {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length < 1 && !args[0].equalsIgnoreCase("all")){
            return Bukkit.getOnlinePlayers().stream()
                    .filter(player -> player.getName().startsWith(args[args.length - 1].toLowerCase()))
                    .map(HumanEntity::getName)
                    .collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)){
            Lang.send(sender, Lang.PLAYER_COMMAND);
            return true;
        }

        Player target = args.length > 0 && !args[0].equalsIgnoreCase("all") ? org.bukkit.Bukkit.getPlayer(args[0]) : (Player) sender;

        if (!sender.hasPermission("command.repair" + (target != sender ? ".other" : "")) ) {
            Lang.send(sender, Lang.COMMAND_NO_PERMISSION);
            return true;
        }

        if (!Config.ALLOW_REPAIR){
            Lang.send(sender, Lang.REPAIR_NOT_ALLOWED);
            return true;
        }

        if (args.length > 0 && args[0].equals(target) ){ //  /repair Dwd_madmac
            if (target.hasPermission("command.repair.exempt")){
                Lang.send(sender, Lang.PLAYER_EXEMPT
                        .replace("{command}", cmd.getName())
                        .replace("{player}", target.getDisplayName()));
                return true;
            }

            if (!target.isOnline()){
                Lang.send(sender, Lang.PLAYER_NOT_ONLINE);
                return true;
            }

            if (args.length > 1 && args[1].equalsIgnoreCase("all")){ //  /repair DwD_MadMac All
                ItemUtil.repairAllItems(target);
                Lang.send(sender, Lang.REPAIR_ALL_ITEMS_OTHER
                        .replace("{getPlayer}", target.getDisplayName())
                        .replace("{possessive}", target.getDisplayName().toLowerCase().endsWith("s") ? "'" : "'s"));
                return true;
            }

            ItemUtil.repairItem(target);

            String itemsDamaged = "";
            if (!itemsDamaged.equals("")){
                Lang.send(sender, Lang.ITEMS_REPAIRED
                        .replace("{getItem}", itemsDamaged) );
                Lang.send(target, Lang.ITEMS_REPAIRED_OTHER
                        .replace("{getPlayer}", sender.getName())
                        .replace("{possessive}", target.getDisplayName().toLowerCase().endsWith("s") ? "'" : "'s")
                        .replace("{getItem}", itemsDamaged));
                return true;
            }

            Lang.send(sender, Lang.NO_REPAIR_NEEDED);
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("all")){
            ItemUtil.repairAllItems(target);
            Lang.send(sender, Lang.REPAIR_ALL_ITEMS);
            return true;
        }

        ItemUtil.repairItem(target);
        ItemStack inHand = (!Config.REPAIR_MAIN_HAND) ? target.getInventory().getItemInOffHand() : target.getInventory().getItemInMainHand();
        Lang.send(sender, Lang.ITEMS_REPAIRED
                .replace("{getItem}", ItemUtil.getItemName(inHand)) );

        return true;
    }
}
