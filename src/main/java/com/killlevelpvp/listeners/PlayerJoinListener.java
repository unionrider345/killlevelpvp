package com.killlevelpvp.listeners;

import com.killlevelpvp.KillLevelPvP;
import com.killlevelpvp.managers.LevelManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

/**
 * Handles player join, quit, and respawn events
 * Ensures abilities are properly applied/removed
 */
public class PlayerJoinListener implements Listener {

    private final KillLevelPvP plugin;

    public PlayerJoinListener(KillLevelPvP plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        LevelManager levelManager = plugin.getLevelManager();
        
        // Apply abilities based on stored kills
        // Small delay to ensure player is fully loaded
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                levelManager.applyAbilities(player);
                levelManager.updateTabList(player);
            }
        }, 5L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Remove abilities (cleanup)
        plugin.getLevelManager().removeAbilities(player);
        
        // Note: Data is already saved on every change, no need to save here
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        LevelManager levelManager = plugin.getLevelManager();
        
        // Re-apply abilities after respawn
        // Small delay to ensure respawn is complete
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                levelManager.applyAbilities(player);
            }
        }, 2L);
    }
}
