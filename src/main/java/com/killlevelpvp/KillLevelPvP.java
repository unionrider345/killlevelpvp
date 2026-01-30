package com.killlevelpvp;

import com.killlevelpvp.commands.LevelCommand;
import com.killlevelpvp.commands.ReloadCommand;
import com.killlevelpvp.listeners.CombatListener;
import com.killlevelpvp.listeners.DoubleJumpListener;
import com.killlevelpvp.listeners.GUIListener;
import com.killlevelpvp.listeners.PlayerJoinListener;
import com.killlevelpvp.managers.ConfigManager;
import com.killlevelpvp.managers.LevelManager;
import com.killlevelpvp.managers.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * KillLevelPvP - A PvP kill-based leveling system with 8 levels
 * Each level grants permanent abilities based on player kills
 */
public class KillLevelPvP extends JavaPlugin {

    private static KillLevelPvP instance;
    private ConfigManager configManager;
    private PlayerDataManager playerDataManager;
    private LevelManager levelManager;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize managers
        this.configManager = new ConfigManager(this);
        this.playerDataManager = new PlayerDataManager(this);
        this.levelManager = new LevelManager(this);

        // Load data
        configManager.loadConfig();
        playerDataManager.loadData();

        // Register commands
        getCommand("level").setExecutor(new LevelCommand(this));
        getCommand("killlevelpvp").setExecutor(new ReloadCommand(this));

        // Register listeners
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new DoubleJumpListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        // Apply abilities to online players (for reload support)
        for (Player player : Bukkit.getOnlinePlayers()) {
            levelManager.applyAbilities(player);
            levelManager.updateTabList(player);
        }

        getLogger().info("KillLevelPvP has been enabled!");
    }

    @Override
    public void onDisable() {
        // Save all player data
        if (playerDataManager != null) {
            playerDataManager.saveData();
        }

        // Remove abilities from all online players
        if (levelManager != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                levelManager.removeAbilities(player);
            }
        }

        getLogger().info("KillLevelPvP has been disabled!");
    }

    public static KillLevelPvP getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public LevelManager getLevelManager() {
        return levelManager;
    }

    /**
     * Reload the plugin configuration and messages
     */
    public void reload() {
        configManager.loadConfig();
        
        // Re-apply abilities to all online players with new config
        for (Player player : Bukkit.getOnlinePlayers()) {
            levelManager.applyAbilities(player);
            levelManager.updateTabList(player);
        }
    }
}
