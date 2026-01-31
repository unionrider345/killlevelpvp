package com.killlevelpvp.managers;

import com.killlevelpvp.KillLevelPvP;
import com.killlevelpvp.utils.ColorUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Manages player abilities based on their level
 * 
 * LEVEL ABILITIES:
 * Level 1: 0 kills -> None
 * Level 2: 1 kill -> Speed I (permanent)
 * Level 3: 3 kills -> +1 max heart
 * Level 4: 5 kills -> Strength I (permanent)
 * Level 5: 8 kills -> Double Jump (5s cooldown)
 * Level 6: 12 kills -> +1 max heart (total +2)
 * Level 7: 16 kills -> Speed II (permanent)
 * Level 8: 20 kills -> +1 max heart (total +3)
 */
public class LevelManager {

    private final KillLevelPvP plugin;
    
    // Attribute modifier keys for health
    private static final String HEALTH_MODIFIER_KEY = "killlevelpvp_health";
    
    // Track players with double jump enabled
    private final Set<UUID> doubleJumpEnabled = new HashSet<>();

    public LevelManager(KillLevelPvP plugin) {
        this.plugin = plugin;
    }

    /**
     * Apply all abilities for a player based on their current level
     */
    public void applyAbilities(Player player) {
        int level = plugin.getPlayerDataManager().getLevel(player.getUniqueId());
        
        // First remove all existing abilities
        removeAbilities(player);
        
        // Apply abilities based on level
        // Level 2+: Speed
        if (level >= 2) {
            int speedLevel = level >= 7 ? 1 : 0; // Speed II at level 7+, Speed I otherwise
            applyPermanentPotion(player, PotionEffectType.SPEED, speedLevel);
        }
        
        // Level 3+: Extra hearts
        if (level >= 3) {
            int extraHearts = 0;
            if (level >= 8) extraHearts = 3;
            else if (level >= 6) extraHearts = 2;
            else extraHearts = 1;
            
            applyExtraHealth(player, extraHearts * 2); // 2 health points per heart
        }
        
        // Level 4+: Strength
        if (level >= 4) {
            applyPermanentPotion(player, PotionEffectType.STRENGTH, 0); // Strength I
        }
        
        // Level 5+: Enable double jump
        if (level >= 5) {
            enableDoubleJump(player);
        } else {
            disableDoubleJump(player);
        }
    }

    /**
     * Remove all abilities from a player
     */
    public void removeAbilities(Player player) {
        // Remove potion effects
        player.removePotionEffect(PotionEffectType.SPEED);
        player.removePotionEffect(PotionEffectType.STRENGTH);
        
        // Remove extra health
        removeExtraHealth(player);
        
        // Disable double jump
        disableDoubleJump(player);
    }

    /**
     * Apply a permanent potion effect
     */
    private void applyPermanentPotion(Player player, PotionEffectType type, int amplifier) {
        // Remove existing effect first to prevent stacking issues
        player.removePotionEffect(type);
        
        // Apply new effect with very long duration (essentially permanent)
        player.addPotionEffect(new PotionEffect(
            type,
            PotionEffect.INFINITE_DURATION,
            amplifier,
            false, // ambient
            false, // particles
            true   // icon
        ));
    }

    /**
     * Apply extra health to a player
     */
    private void applyExtraHealth(Player player, double extraHealth) {
        AttributeInstance attribute = player.getAttribute(Attribute.MAX_HEALTH);
        if (attribute == null) return;
        
        // Remove any existing modifier first
        removeExtraHealth(player);
        
        // Create and add new modifier
        AttributeModifier modifier = new AttributeModifier(
            HEALTH_MODIFIER_KEY,
            extraHealth,
            AttributeModifier.Operation.ADD_NUMBER
        );
        attribute.addModifier(modifier);
        
        // Ensure current health doesn't exceed new max
        double newMax = attribute.getValue();
        if (player.getHealth() > newMax) {
            player.setHealth(newMax);
        }
    }

    /**
     * Remove extra health from a player
     */
    private void removeExtraHealth(Player player) {
        AttributeInstance attribute = player.getAttribute(Attribute.MAX_HEALTH);
        if (attribute == null) return;
        
        // Remove our modifier
        for (AttributeModifier modifier : attribute.getModifiers()) {
            if (modifier.getName().equals(HEALTH_MODIFIER_KEY)) {
                attribute.removeModifier(modifier);
            }
        }
        
        // Ensure health is at least base and doesn't exceed new max
        double newMax = attribute.getValue();
        if (newMax < 20.0) {
            // Reset to base if somehow below
            attribute.setBaseValue(20.0);
            newMax = 20.0;
        }
        
        // Clamp current health
        if (player.getHealth() > newMax) {
            player.setHealth(newMax);
        }
    }

    /**
     * Enable double jump for a player
     */
    public void enableDoubleJump(Player player) {
        doubleJumpEnabled.add(player.getUniqueId());
        
        // Only allow flight in survival/adventure
        if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
            player.setAllowFlight(true);
        }
    }

    /**
     * Disable double jump for a player
     */
    public void disableDoubleJump(Player player) {
        doubleJumpEnabled.remove(player.getUniqueId());
        
        // Only disable if not in creative/spectator
        if (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
            player.setAllowFlight(false);
            player.setFlying(false);
        }
    }

    /**
     * Check if a player has double jump enabled
     */
    public boolean hasDoubleJump(UUID uuid) {
        return doubleJumpEnabled.contains(uuid);
    }

    /**
     * Handle level change with appropriate feedback
     */
    public void handleLevelChange(Player player, int oldLevel, int newLevel) {
        if (newLevel > oldLevel) {
            // Level UP
            sendLevelUpNotification(player, newLevel);
            
            // Broadcast if reached max level
            if (newLevel == plugin.getConfigManager().getMaxLevel()) {
                broadcastMaxLevel(player);
            }
        } else if (newLevel < oldLevel) {
            // Level DOWN
            sendLevelDownNotification(player, newLevel);
        }
        
        // Apply new abilities
        applyAbilities(player);
        
        // Update tab list
        updateTabList(player);
    }

    /**
     * Send level up notification to player
     */
    private void sendLevelUpNotification(Player player, int level) {
        ConfigManager config = plugin.getConfigManager();
        
        // Send title
        String titleText = ColorUtils.colorize(config.getLevelUpTitle());
        String subtitleText = ColorUtils.colorize(
            config.getLevelUpSubtitle().replace("{level}", String.valueOf(level))
        );
        
        Title title = Title.title(
            ColorUtils.toComponent(titleText),
            ColorUtils.toComponent(subtitleText),
            Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(2), Duration.ofMillis(500))
        );
        player.showTitle(title);
        
        // Play sound
        player.playSound(
            player.getLocation(),
            config.getLevelUpSound(),
            config.getLevelUpVolume(),
            config.getLevelUpPitch()
        );
    }

    /**
     * Send level down notification to player
     */
    private void sendLevelDownNotification(Player player, int level) {
        ConfigManager config = plugin.getConfigManager();
        
        // Send subtitle only (no title)
        String subtitleText = ColorUtils.colorize(
            config.getLevelDownSubtitle().replace("{level}", String.valueOf(level))
        );
        
        Title title = Title.title(
            Component.empty(),
            ColorUtils.toComponent(subtitleText),
            Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(2), Duration.ofMillis(500))
        );
        player.showTitle(title);
    }

    /**
     * Broadcast when a player reaches max level
     */
    private void broadcastMaxLevel(Player player) {
        String message = ColorUtils.colorize(
            plugin.getConfigManager().getMaxLevelBroadcast()
                .replace("{player}", player.getName())
        );
        
        Bukkit.broadcast(ColorUtils.toComponent(message));
    }

    /**
     * Update the tab list for a player
     */
    public void updateTabList(Player player) {
        int level = plugin.getPlayerDataManager().getLevel(player.getUniqueId());
        String format = plugin.getConfigManager().getTabListFormat()
            .replace("{level}", String.valueOf(level))
            .replace("{name}", player.getName());
        
        player.playerListName(ColorUtils.toComponent(ColorUtils.colorize(format)));
    }
}
