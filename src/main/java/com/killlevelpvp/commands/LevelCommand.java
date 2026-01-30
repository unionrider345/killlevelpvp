package com.killlevelpvp.commands;

import com.killlevelpvp.KillLevelPvP;
import com.killlevelpvp.gui.LevelGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Command handler for /level (and /lvl alias)
 * Opens the level GUI for the player
 */
public class LevelCommand implements CommandExecutor {

    private final KillLevelPvP plugin;

    public LevelCommand(KillLevelPvP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, 
                            @NotNull String label, @NotNull String[] args) {
        
        // Only players can use this command
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        // Open the GUI
        LevelGUI.openGUI(plugin, player);
        return true;
    }
}
