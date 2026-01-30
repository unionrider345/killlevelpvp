package com.killlevelpvp.managers;

import com.killlevelpvp.KillLevelPvP;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages plugin configuration including level thresholds, sounds, and messages
 */
public class ConfigManager {

    private final KillLevelPvP plugin;
    private FileConfiguration config;
    private FileConfiguration messages;

    // Level thresholds (level -> kills required)
    private final Map<Integer, Integer> levelThresholds = new HashMap<>();

    // Sounds
    private Sound levelUpSound;
    private float levelUpVolume;
    private float levelUpPitch;

    // Double jump
    private int doubleJumpCooldown;

    // Tab list format
    private String tabListFormat;

    // Anti-farm
    private int antiFarmCooldown;

    // Messages
    private String levelUpTitle;
    private String levelUpSubtitle;
    private String levelDownSubtitle;
    private String maxLevelBroadcast;
    private String guiTitle;
    private String guiLevelName;
    private String guiKillsName;
    private String guiProgressName;
    private String guiCloseName;
    private String guiMaxLevel;
    private String guiNextLevel;

    public ConfigManager(KillLevelPvP plugin) {
        this.plugin = plugin;
    }

    /**
     * Load or reload all configuration files
     */
    public void loadConfig() {
        // Save default config if it doesn't exist
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();

        // Load messages.yml
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);

        // Load level thresholds
        levelThresholds.clear();
        levelThresholds.put(1, config.getInt("levels.1.kills", 0));
        levelThresholds.put(2, config.getInt("levels.2.kills", 1));
        levelThresholds.put(3, config.getInt("levels.3.kills", 3));
        levelThresholds.put(4, config.getInt("levels.4.kills", 5));
        levelThresholds.put(5, config.getInt("levels.5.kills", 8));
        levelThresholds.put(6, config.getInt("levels.6.kills", 12));
        levelThresholds.put(7, config.getInt("levels.7.kills", 16));
        levelThresholds.put(8, config.getInt("levels.8.kills", 20));

        // Load sounds
        try {
            levelUpSound = Sound.valueOf(config.getString("sounds.level-up.sound", "ENTITY_PLAYER_LEVELUP"));
        } catch (IllegalArgumentException e) {
            levelUpSound = Sound.ENTITY_PLAYER_LEVELUP;
            plugin.getLogger().warning("Invalid level-up sound in config, using default.");
        }
        levelUpVolume = (float) config.getDouble("sounds.level-up.volume", 1.0);
        levelUpPitch = (float) config.getDouble("sounds.level-up.pitch", 1.0);

        // Load double jump settings
        doubleJumpCooldown = config.getInt("double-jump.cooldown", 5);

        // Load tab list format
        tabListFormat = config.getString("tab-list.format", "&7[&eLv {level}&7] &f{name}");

        // Load anti-farm settings
        antiFarmCooldown = config.getInt("anti-farm.cooldown", 60);

        // Load messages
        levelUpTitle = messages.getString("level-up.title", "&a&lLEVEL UP!");
        levelUpSubtitle = messages.getString("level-up.subtitle", "&eLevel {level} Unlocked");
        levelDownSubtitle = messages.getString("level-down.subtitle", "&cLevel {level}");
        maxLevelBroadcast = messages.getString("max-level.broadcast", "&6&l{player} &ehas reached &6&lMAX LEVEL&e!");
        guiTitle = messages.getString("gui.title", "Your Level");
        guiLevelName = messages.getString("gui.level-name", "&6&lYour Level");
        guiKillsName = messages.getString("gui.kills-name", "&c&lYour Kills");
        guiProgressName = messages.getString("gui.progress-name", "&b&lProgress");
        guiCloseName = messages.getString("gui.close-name", "&c&lClose");
        guiMaxLevel = messages.getString("gui.max-level", "&a&lMAX LEVEL");
        guiNextLevel = messages.getString("gui.next-level", "&7Next: Level {level} in {kills} kill(s)");
    }

    /**
     * Get the kills required for a specific level
     */
    public int getKillsForLevel(int level) {
        return levelThresholds.getOrDefault(level, Integer.MAX_VALUE);
    }

    /**
     * Calculate the level based on kills
     */
    public int calculateLevel(int kills) {
        int level = 1;
        for (int i = 8; i >= 1; i--) {
            if (kills >= levelThresholds.get(i)) {
                level = i;
                break;
            }
        }
        return level;
    }

    // Getters for sounds
    public Sound getLevelUpSound() { return levelUpSound; }
    public float getLevelUpVolume() { return levelUpVolume; }
    public float getLevelUpPitch() { return levelUpPitch; }

    // Getters for settings
    public int getDoubleJumpCooldown() { return doubleJumpCooldown; }
    public String getTabListFormat() { return tabListFormat; }
    public int getAntiFarmCooldown() { return antiFarmCooldown; }

    // Getters for messages
    public String getLevelUpTitle() { return levelUpTitle; }
    public String getLevelUpSubtitle() { return levelUpSubtitle; }
    public String getLevelDownSubtitle() { return levelDownSubtitle; }
    public String getMaxLevelBroadcast() { return maxLevelBroadcast; }
    public String getGuiTitle() { return guiTitle; }
    public String getGuiLevelName() { return guiLevelName; }
    public String getGuiKillsName() { return guiKillsName; }
    public String getGuiProgressName() { return guiProgressName; }
    public String getGuiCloseName() { return guiCloseName; }
    public String getGuiMaxLevel() { return guiMaxLevel; }
    public String getGuiNextLevel() { return guiNextLevel; }

    /**
     * Get the maximum level (always 8)
     */
    public int getMaxLevel() {
        return 8;
    }
}
