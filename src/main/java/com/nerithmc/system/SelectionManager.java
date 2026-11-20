package com.nerithmc.system;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SelectionManager implements Listener {

    private final NerithMcSystem2 plugin;
    private final NamespacedKey wandKey;
    private final Map<UUID, Location> pos1 = new HashMap<>();
    private final Map<UUID, Location> pos2 = new HashMap<>();

    public SelectionManager(NerithMcSystem2 plugin) {
        this.plugin = plugin;
        this.wandKey = new NamespacedKey(plugin, "selection_wand");
    }

    public ItemStack createWand() {
        ItemStack item = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Util.comp("&6&lSelection Wand"));
        meta.lore(java.util.List.of(
                Util.comp("&7Left click: &fpos1"),
                Util.comp("&7Right click: &fpos2")));
        meta.getPersistentDataContainer().set(wandKey, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    private boolean isWand(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(wandKey, PersistentDataType.BYTE);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        Block block = e.getClickedBlock();
        if (block == null || !isWand(e.getItem())) return;
        Player p = e.getPlayer();
        if (!p.hasPermission("nerithmc.admin")) return;

        Action action = e.getAction();
        if (action == Action.LEFT_CLICK_BLOCK) {
            pos1.put(p.getUniqueId(), block.getLocation());
            plugin.send(p, "pos1-set", "%x%", "" + block.getX(), "%y%", "" + block.getY(), "%z%", "" + block.getZ());
        } else if (action == Action.RIGHT_CLICK_BLOCK) {
            pos2.put(p.getUniqueId(), block.getLocation());
            plugin.send(p, "pos2-set", "%x%", "" + block.getX(), "%y%", "" + block.getY(), "%z%", "" + block.getZ());
        } else {
            return;
        }
        e.setCancelled(true);
        e.setUseInteractedBlock(Event.Result.DENY);
    }

    /** Returns the player's selection or null if incomplete. */
    public Region getRegion(Player p) {
        Location a = pos1.get(p.getUniqueId());
        Location b = pos2.get(p.getUniqueId());
        if (a == null || b == null || a.getWorld() == null || !a.getWorld().equals(b.getWorld())) return null;
        return Region.of(a, b);
    }
}
