package com.nerithmc.system;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GateRewardListener implements Listener {

    private static final class GuiHolder implements InventoryHolder {
        private Inventory inventory;

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }

    private final NerithMcSystem2 plugin;

    public GateRewardListener(NerithMcSystem2 plugin) {
        this.plugin = plugin;
    }

    public void openGui(Player p) {
        GuiHolder holder = new GuiHolder();
        Inventory inv = Bukkit.createInventory(holder, 54, Component.text("Gate Reward Items"));
        holder.inventory = inv;
        for (ItemStack item : plugin.gate().getRewardItems()) {
            inv.addItem(item);
        }
        p.openInventory(inv);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!(e.getInventory().getHolder() instanceof GuiHolder)) return;
        List<ItemStack> items = new ArrayList<>();
        for (ItemStack item : e.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) items.add(item.clone());
        }
        plugin.gate().setRewardItems(items);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        if (!plugin.gate().isReward(e.getBlock())) return;
        List<ItemStack> items = plugin.gate().getRewardItems();
        if (items.isEmpty()) return;

        e.setDropItems(false);
        Player p = e.getPlayer();
        for (ItemStack item : items) {
            p.getInventory().addItem(item).values()
                    .forEach(left -> p.getWorld().dropItemNaturally(p.getLocation(), left));
        }
    }
}
