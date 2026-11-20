package com.nerithmc.system;

import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;

public class ItemDespawnListener implements Listener {

    private final NerithMcSystem2 plugin;

    public ItemDespawnListener(NerithMcSystem2 plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemSpawn(ItemSpawnEvent e) {
        if (!plugin.getConfig().getBoolean("item-despawn.enabled", true)) return;
        long ticks = Math.max(1, plugin.getConfig().getInt("item-despawn.seconds", 90)) * 20L;
        Item item = e.getEntity();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (item.isValid()) item.remove();
        }, ticks);
    }
}
