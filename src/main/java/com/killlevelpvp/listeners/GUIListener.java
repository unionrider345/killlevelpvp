package com.killlevelpvp.listeners;

import com.killlevelpvp.KillLevelPvP;
import com.killlevelpvp.gui.LevelGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

/**
 * Handles GUI interactions for the level menu
 * Makes the GUI read-only (cancels all clicks, drags, shift-clicks)
 */
public class GUIListener implements Listener {

    private final KillLevelPvP plugin;

    public GUIListener(KillLevelPvP plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        // Check if it's our GUI
        if (!isLevelGUI(event.getInventory())) {
            return;
        }
        
        // Cancel all clicks - GUI is read-only
        event.setCancelled(true);
        
        // Check if clicked in our inventory (not player inventory)
        if (event.getClickedInventory() == null || 
            event.getClickedInventory() != event.getView().getTopInventory()) {
            return;
        }
        
        // Handle close button (slot 22)
        if (event.getSlot() == 22) {
            if (event.getWhoClicked() instanceof Player player) {
                player.closeInventory();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        // Check if it's our GUI
        if (!isLevelGUI(event.getInventory())) {
            return;
        }
        
        // Cancel all drags - GUI is read-only
        event.setCancelled(true);
    }

    /**
     * Check if an inventory is the Level GUI
     */
    private boolean isLevelGUI(Inventory inventory) {
        if (inventory == null) {
            return false;
        }
        
        // Check by title - we use our config title
        String expectedTitle = plugin.getConfigManager().getGuiTitle();
        
        // Get the inventory title from the view
        // Paper uses Adventure components, so we need to check the plain text
        String inventoryTitle = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
            .plainText()
            .serialize(inventory.getHolder() == null ? 
                net.kyori.adventure.text.Component.empty() : 
                net.kyori.adventure.text.Component.empty());
        
        // Since we can't easily get the title from the inventory itself in Paper,
        // we'll check if the inventory has 27 slots and specific items
        if (inventory.getSize() != 27) {
            return false;
        }
        
        // Check for our signature items (Nether Star at slot 11, Barrier at slot 22)
        if (inventory.getItem(11) != null && 
            inventory.getItem(11).getType() == org.bukkit.Material.NETHER_STAR &&
            inventory.getItem(22) != null &&
            inventory.getItem(22).getType() == org.bukkit.Material.BARRIER) {
            return true;
        }
        
        return false;
    }
}
