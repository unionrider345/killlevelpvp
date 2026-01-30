package com.killlevelpvp.commands;

import com.killlevelpvp.KillLevelPvP;
import com.killlevelpvp.utils.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Command handler for /killlevelpvp reload
 * Reloads the plugin configuration
 */
public class ReloadCommand implements CommandExecutor, TabCompleter {

    private final KillLevelPvP plugin;

    public ReloadCommand(KillLevelPvP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, 
                            @NotNull String label, @NotNull String[] args) {
        
        // Check for reload subcommand
        if (args.length == 0) {
            sender.sendMessage(ColorUtils.toComponent("&eKillLevelPvP &7- &fUse &e/killlevelpvp reload &fto reload config."));
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            // Check permission
            if (!sender.hasPermission("killlevelpvp.admin")) {
                sender.sendMessage(ColorUtils.toComponent("&cYou don't have permission to do this."));
                return true;
            }

            // Reload the plugin
            plugin.reload();
            sender.sendMessage(ColorUtils.toComponent("&aKillLevelPvP configuration reloaded!"));
            return true;
        }

        sender.sendMessage(ColorUtils.toComponent("&cUnknown subcommand. Use &e/killlevelpvp reload"));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, 
                                                 @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            if (sender.hasPermission("killlevelpvp.admin")) {
                if ("reload".startsWith(args[0].toLowerCase())) {
                    completions.add("reload");
                }
            }
        }
        
        return completions;
    }
}
