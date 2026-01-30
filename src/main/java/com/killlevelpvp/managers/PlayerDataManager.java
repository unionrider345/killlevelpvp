package com.killlevelpvp.managers;

import com.killlevelpvp.KillLevelPvP;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages player data persistence (kills stored by UUID)
 */
public class PlayerDataManager {

    private final KillLevelPvP plugin;
    private final File dataFile;
    private YamlConfiguration dataConfig;

    // In-memory storage: UUID -> kills
    private final Map<UUID, Integer> playerKills = new HashMap<>();

    // Anti-farm tracking: "killer:victim" -> last kill timestamp
    private final Map<String, Long> recentKills = new HashMap<>();

    public PlayerDataManager(KillLevelPvP plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "playerdata.yml");
    }

    /**
     * Load player data from file
     */
    public void loadData() {
        if (!dataFile.exists()) {
            try {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create playerdata.yml: " + e.getMessage());
            }
        }

        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        // Load all player data
        playerKills.clear();
        if (dataConfig.contains("players")) {
            for (String uuidString : dataConfig.getConfigurationSection("players").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidString);
                    int kills = dataConfig.getInt("players." + uuidString + ".kills", 0);
                    playerKills.put(uuid, kills);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID in playerdata.yml: " + uuidString);
                }
            }
        }

        plugin.getLogger().info("Loaded data for " + playerKills.size() + " players.");
    }

    /**
     * Save all player data to file
     */
    public void saveData() {
        if (dataConfig == null) {
            dataConfig = new YamlConfiguration();
        }

        // Clear and re-save all data
        dataConfig.set("players", null);
        for (Map.Entry<UUID, Integer> entry : playerKills.entrySet()) {
            dataConfig.set("players." + entry.getKey().toString() + ".kills", entry.getValue());
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save playerdata.yml: " + e.getMessage());
        }
    }

    /**
     * Get kills for a player
     */
    public int getKills(UUID uuid) {
        return playerKills.getOrDefault(uuid, 0);
    }

    /**
     * Set kills for a player
     */
    public void setKills(UUID uuid, int kills) {
        playerKills.put(uuid, Math.max(0, kills));
        // Auto-save on change for safety
        saveData();
    }

    /**
     * Add kills to a player
     */
    public void addKill(UUID uuid) {
        int currentKills = getKills(uuid);
        setKills(uuid, currentKills + 1);
    }

    /**
     * Remove a kill from a player (minimum 0)
     */
    public void removeKill(UUID uuid) {
        int currentKills = getKills(uuid);
        setKills(uuid, Math.max(0, currentKills - 1));
    }

    /**
     * Check if a kill should count (anti-farm)
     * Returns true if the kill should count, false if it's farming
     */
    public boolean shouldCountKill(UUID killer, UUID victim) {
        String key = killer.toString() + ":" + victim.toString();
        long now = System.currentTimeMillis();
        int cooldownMs = plugin.getConfigManager().getAntiFarmCooldown() * 1000;

        Long lastKill = recentKills.get(key);
        if (lastKill != null && (now - lastKill) < cooldownMs) {
            return false; // Still in cooldown, don't count
        }

        // Record this kill
        recentKills.put(key, now);

        // Clean up old entries periodically
        cleanupRecentKills();

        return true;
    }

    /**
     * Clean up old anti-farm entries
     */
    private void cleanupRecentKills() {
        long now = System.currentTimeMillis();
        int cooldownMs = plugin.getConfigManager().getAntiFarmCooldown() * 1000 * 2; // Keep for 2x cooldown

        recentKills.entrySet().removeIf(entry -> (now - entry.getValue()) > cooldownMs);
    }

    /**
     * Get the level for a player based on their kills
     */
    public int getLevel(UUID uuid) {
        return plugin.getConfigManager().calculateLevel(getKills(uuid));
    }
}
