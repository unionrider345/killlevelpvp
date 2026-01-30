package com.killlevelpvp.gui;

import com.killlevelpvp.KillLevelPvP;
import com.killlevelpvp.managers.ConfigManager;
import com.killlevelpvp.utils.ColorUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI menu showing player's level, kills, and progress
 * 
 * Layout (27 slots, 3 rows):
 * 
 * Row 1: [glass][glass][glass][glass][glass][glass][glass][glass][glass]
 * Row 2: [glass][glass][LEVEL][glass][KILLS][glass][PROGRESS][glass][glass]
 * Row 3: [glass][glass][glass][glass][CLOSE][glass][glass][glass][glass]
 * 
 * Slot positions:
 * - Level (Nether Star): slot 11
 * - Kills (Iron Sword): slot 13
 * - Progress (Experience Bottle): slot 15
 * - Close (Barrier): slot 22
 */
public class LevelGUI {

    public static final String GUI_TITLE_KEY = "KillLevelPvP_LevelGUI";
    
    private static final int SLOT_LEVEL = 11;
    private static final int SLOT_KILLS = 13;
    private static final int SLOT_PROGRESS = 15;
    private static final int SLOT_CLOSE = 22;

    /**
     * Open the level GUI for a player
     */
    public static void openGUI(KillLevelPvP plugin, Player player) {
        ConfigManager config = plugin.getConfigManager();
        
        // Create inventory
        String title = config.getGuiTitle();
        Inventory gui = Bukkit.createInventory(null, 27, ColorUtils.toComponent(title));
        
        // Fill with gray glass panes
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 27; i++) {
            gui.setItem(i, filler);
        }
        
        // Get player data
        int kills = plugin.getPlayerDataManager().getKills(player.getUniqueId());
        int level = plugin.getPlayerDataManager().getLevel(player.getUniqueId());
        int maxLevel = config.getMaxLevel();
        
        // Create Level item (Nether Star)
        List<String> levelLore = new ArrayList<>();
        levelLore.add("&7You are currently");
        levelLore.add("&6Level " + level);
        levelLore.add("");
        levelLore.add(getAbilitiesDescription(level));
        
        ItemStack levelItem = createItem(
            Material.NETHER_STAR,
            config.getGuiLevelName(),
            levelLore
        );
        gui.setItem(SLOT_LEVEL, levelItem);
        
        // Create Kills item (Iron Sword)
        List<String> killsLore = new ArrayList<>();
        killsLore.add("&7Total PvP Kills:");
        killsLore.add("&c" + kills);
        
        ItemStack killsItem = createItem(
            Material.IRON_SWORD,
            config.getGuiKillsName(),
            killsLore
        );
        gui.setItem(SLOT_KILLS, killsItem);
        
        // Create Progress item (Experience Bottle)
        List<String> progressLore = new ArrayList<>();
        if (level >= maxLevel) {
            progressLore.add(config.getGuiMaxLevel());
            progressLore.add("&7Congratulations!");
        } else {
            int nextLevel = level + 1;
            int killsNeeded = config.getKillsForLevel(nextLevel);
            int killsRemaining = killsNeeded - kills;
            
            progressLore.add("&7Kills: &f" + kills + "/" + killsNeeded);
            progressLore.add("");
            progressLore.add(ColorUtils.colorize(
                config.getGuiNextLevel()
                    .replace("{level}", String.valueOf(nextLevel))
                    .replace("{kills}", String.valueOf(killsRemaining))
            ));
            
            // Progress bar
            int progress = (int) ((double) kills / killsNeeded * 10);
            StringBuilder bar = new StringBuilder("&a");
            for (int i = 0; i < 10; i++) {
                if (i < progress) {
                    bar.append("▌");
                } else {
                    if (i == progress) bar.append("&7");
                    bar.append("▌");
                }
            }
            progressLore.add(bar.toString());
        }
        
        ItemStack progressItem = createItem(
            Material.EXPERIENCE_BOTTLE,
            config.getGuiProgressName(),
            progressLore
        );
        gui.setItem(SLOT_PROGRESS, progressItem);
        
        // Create Close button (Barrier)
        List<String> closeLore = new ArrayList<>();
        closeLore.add("&7Click to close this menu");
        
        ItemStack closeItem = createItem(
            Material.BARRIER,
            config.getGuiCloseName(),
            closeLore
        );
        gui.setItem(SLOT_CLOSE, closeItem);
        
        // Open the GUI
        player.openInventory(gui);
    }

    /**
     * Create an ItemStack with name and lore
     */
    private static ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.displayName(ColorUtils.toComponent(name));
            
            if (lore != null && !lore.isEmpty()) {
                List<Component> coloredLore = new ArrayList<>();
                for (String line : lore) {
                    coloredLore.add(ColorUtils.toComponent(line));
                }
                meta.lore(coloredLore);
            }
            
            item.setItemMeta(meta);
        }
        
        return item;
    }

    /**
     * Get a description of abilities for a given level
     */
    private static String getAbilitiesDescription(int level) {
        StringBuilder abilities = new StringBuilder("&7Abilities: ");
        
        if (level < 2) {
            abilities.append("&8None");
        } else {
            List<String> abilityList = new ArrayList<>();
            
            if (level >= 7) {
                abilityList.add("&bSpeed II");
            } else if (level >= 2) {
                abilityList.add("&bSpeed I");
            }
            
            if (level >= 4) {
                abilityList.add("&cStrength I");
            }
            
            if (level >= 5) {
                abilityList.add("&eDouble Jump");
            }
            
            int extraHearts = 0;
            if (level >= 8) extraHearts = 3;
            else if (level >= 6) extraHearts = 2;
            else if (level >= 3) extraHearts = 1;
            
            if (extraHearts > 0) {
                abilityList.add("&d+" + extraHearts + " Heart" + (extraHearts > 1 ? "s" : ""));
            }
            
            abilities.append(String.join("&7, ", abilityList));
        }
        
        return abilities.toString();
    }
}
