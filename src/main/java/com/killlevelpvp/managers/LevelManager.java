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

public class LevelManager {

    private final KillLevelPvP plugin;
    private static final String HEALTH_MODIFIER_KEY = "killlevelpvp_health";
    private final Set<UUID> doubleJumpEnabled = new HashSet<>();

    public LevelManager(KillLevelPvP plugin) {
        this.plugin = plugin;
    }

    public void applyAbilities(Player player) {
        int level = plugin.getPlayerDataManager().getLevel(player.getUniqueId());
        removeAbilities(player);
        
        if (level >= 2) {
            int speedLevel = level >= 7 ? 1 : 0;
            applyPermanentPotion(player, PotionEffectType.SPEED, speedLevel);
        }
        
        if (level >= 3) {
            int extraHearts = 0;
            if (level >= 8) extraHearts = 3;
            else if (level >= 6) extraHearts = 2;
            else extraHearts = 1;
            applyExtraHealth(player, extraHearts * 2);
        }
        
        if (level >= 4) {
            applyPermanentPotion(player, PotionEffectType.STRENGTH, 0);
        }
        
        if (level >= 5) {
            enableDoubleJump(player);
        } else {
            disableDoubleJump(player);
        }
    }

    public void removeAbilities(Player player) {
        player.removePotionEffect(PotionEffectType.SPEED);
        player.removePotionEffect(PotionEffectType.STRENGTH);
        removeExtraHealth(player);
        disableDoubleJump(player);
    }

    private void applyPermanentPotion(Player player, PotionEffectType type, int amplifier) {
        player.removePotionEffect(type);
        player.addPotionEffect(new PotionEffect(type, PotionEffect.INFINITE_DURATION, amplifier, false, false, true));
    }

    private void applyExtraHealth(Player player, double extraHealth) {
        AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attribute == null) return;
        removeExtraHealth(player);
        AttributeModifier modifier = new AttributeModifier(HEALTH_MODIFIER_KEY, extraHealth, AttributeModifier.Operation.ADD_NUMBER);
        attribute.addModifier(modifier);
        double newMax = attribute.getValue();
        if (player.getHealth() > newMax) {
            player.setHealth(newMax);
        }
    }

    private void removeExtraHealth(Player player) {
        AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (
