package com.killlevelpvp.listeners;

import com.killlevelpvp.KillLevelPvP;
import com.killlevelpvp.managers.LevelManager;
import com.killlevelpvp.managers.PlayerDataManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

/**
 * Handles PvP kills and deaths for the leveling system
 * 
 * Rules:
 * - Only player vs player kills count
 * - On kill: +1 kill
 * - On death: -1 kill (minimum 0)
 * - Level recalculated immediately
 * - Anti-farm: Same killer vs victim has cooldown
 */
public class CombatListener implements Listener {

    private final KillLevelPvP plugin;

    public CombatListener(KillLevelPvP plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        // Only count if killed by another player
        if (killer == null || killer.equals(victim)) {
            return;
        }

        PlayerDataManager dataManager = plugin.getPlayerDataManager();
        LevelManager levelManager = plugin.getLevelManager();

        // Check anti-farm
        if (!dataManager.shouldCountKill(killer.getUniqueId(), victim.getUniqueId())) {
            // Kill doesn't count due to farming prevention
            return;
        }

        // Process killer: +1 kill
        int killerOldLevel = dataManager.getLevel(killer.getUniqueId());
        dataManager.addKill(killer.getUniqueId());
        int killerNewLevel = dataManager.getLevel(killer.getUniqueId());

        // Handle level change for killer
        if (killerNewLevel != killerOldLevel) {
            levelManager.handleLevelChange(killer, killerOldLevel, killerNewLevel);
        }

        // Process victim: -1 kill
        int victimOldLevel = dataManager.getLevel(victim.getUniqueId());
        dataManager.removeKill(victim.getUniqueId());
        int victimNewLevel = dataManager.getLevel(victim.getUniqueId());

        // Handle level change for victim (abilities will be applied on respawn)
        if (victimNewLevel != victimOldLevel) {
            // Schedule the notification and ability update for after respawn
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (victim.isOnline()) {
                    levelManager.handleLevelChange(victim, victimOldLevel, victimNewLevel);
                }
            }, 2L); // Short delay to ensure respawn has processed
        }
    }
}
