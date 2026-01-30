package com.killlevelpvp.listeners;

import com.killlevelpvp.KillLevelPvP;
import com.killlevelpvp.managers.LevelManager;
import com.killlevelpvp.utils.ColorUtils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles double jump ability for Level 5+ players
 * 
 * Rules:
 * - Works only in survival/adventure mode
 * - Disabled in water, lava, and while gliding (elytra)
 * - Configurable cooldown (default 5 seconds)
 * - Uses PlayerToggleFlight for detection
 * - Prevents creative flight abuse
 */
public class DoubleJumpListener implements Listener {

    private final KillLevelPvP plugin;
    
    // Track cooldowns: UUID -> last use timestamp
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    
    // Track if player is currently using elytra
    private final Map<UUID, Boolean> glidingPlayers = new HashMap<>();

    public DoubleJumpListener(KillLevelPvP plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        LevelManager levelManager = plugin.getLevelManager();
        
        // Only handle if player has double jump enabled
        if (!levelManager.hasDoubleJump(player.getUniqueId())) {
            return;
        }
        
        // Don't interfere with creative/spectator mode
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }
        
        // Cancel the flight toggle - we're using it for double jump
        event.setCancelled(true);
        
        // Check if player is in invalid state
        if (!canDoubleJump(player)) {
            return;
        }
        
        // Check cooldown
        if (isOnCooldown(player)) {
            return;
        }
        
        // Perform double jump
        performDoubleJump(player);
    }

    /**
     * Check if player can perform a double jump
     */
    private boolean canDoubleJump(Player player) {
        // Check game mode
        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE) {
            return false;
        }
        
        // Check if in water
        Block block = player.getLocation().getBlock();
        Material material = block.getType();
        if (material == Material.WATER || material == Material.LAVA) {
            return false;
        }
        
        // Check if gliding with elytra
        if (player.isGliding()) {
            return false;
        }
        
        return true;
    }

    /**
     * Check if player is on cooldown
     */
    private boolean isOnCooldown(Player player) {
        UUID uuid = player.getUniqueId();
        Long lastUse = cooldowns.get(uuid);
        
        if (lastUse == null) {
            return false;
        }
        
        long cooldownMs = plugin.getConfigManager().getDoubleJumpCooldown() * 1000L;
        long now = System.currentTimeMillis();
        
        if (now - lastUse < cooldownMs) {
            // Still on cooldown - show remaining time
            long remaining = (cooldownMs - (now - lastUse)) / 1000 + 1;
            player.sendActionBar(ColorUtils.toComponent("&cDouble Jump cooldown: " + remaining + "s"));
            return true;
        }
        
        return false;
    }

    /**
     * Perform the double jump
     */
    private void performDoubleJump(Player player) {
        UUID uuid = player.getUniqueId();
        
        // Set cooldown
        cooldowns.put(uuid, System.currentTimeMillis());
        
        // Get player's current velocity direction
        Vector direction = player.getLocation().getDirection();
        
        // Create jump velocity (upward with slight forward momentum)
        Vector jumpVelocity = new Vector(
            direction.getX() * 0.5,  // Forward momentum
            0.8,                      // Upward force
            direction.getZ() * 0.5   // Forward momentum
        );
        
        // Apply velocity
        player.setVelocity(jumpVelocity);
        
        // Play sound
        player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 1.2f);
        
        // Disable flight temporarily (will be re-enabled when landing)
        player.setAllowFlight(false);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        LevelManager levelManager = plugin.getLevelManager();
        
        // Only handle if player has double jump
        if (!levelManager.hasDoubleJump(player.getUniqueId())) {
            return;
        }
        
        // Don't interfere with creative/spectator
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }
        
        // Check if player is on ground and flight is disabled
        if (isOnGround(player) && !player.getAllowFlight()) {
            // Re-enable flight for next double jump
            player.setAllowFlight(true);
        }
        
        // Update gliding state
        glidingPlayers.put(player.getUniqueId(), player.isGliding());
    }

    /**
     * Check if player is on ground (including being in water/lava)
     */
    private boolean isOnGround(Player player) {
        // Check Bukkit's on ground flag
        if (player.isOnGround()) {
            return true;
        }
        
        // Also check if in liquid (can jump again from liquid)
        Material material = player.getLocation().getBlock().getType();
        if (material == Material.WATER || material == Material.LAVA) {
            return true;
        }
        
        return false;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        LevelManager levelManager = plugin.getLevelManager();
        
        // Only handle if player has double jump
        if (!levelManager.hasDoubleJump(player.getUniqueId())) {
            return;
        }
        
        GameMode newMode = event.getNewGameMode();
        
        // Disable double jump flight when entering creative/spectator
        if (newMode == GameMode.CREATIVE || newMode == GameMode.SPECTATOR) {
            // Let the game handle flight normally
            return;
        }
        
        // Re-enable double jump when returning to survival/adventure
        if (newMode == GameMode.SURVIVAL || newMode == GameMode.ADVENTURE) {
            // Re-enable flight for double jump on next tick
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline() && levelManager.hasDoubleJump(player.getUniqueId())) {
                    player.setAllowFlight(true);
                }
            }, 1L);
        }
    }

    /**
     * Clean up cooldown for a player (called when they leave)
     */
    public void clearCooldown(UUID uuid) {
        cooldowns.remove(uuid);
        glidingPlayers.remove(uuid);
    }
}
